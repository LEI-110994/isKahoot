package iskahoot.server;

import iskahoot.model.Player;
import iskahoot.model.Question;
import iskahoot.model.ScoreBoard;
import iskahoot.net.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameState implements Runnable {
    private final String gameCode;
    private final int numTeams;
    private final int playersPerTeam;
    private final int totalQuestions;
    
    // Clients and Players
    private final Map<String, DealWithClient> clients = new ConcurrentHashMap<>();
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<String, String> playerTeams = new ConcurrentHashMap<>();
    
    // Game State
    private final List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private final AtomicBoolean gameStarted = new AtomicBoolean(false);
    private final AtomicBoolean gameEnded = new AtomicBoolean(false);
    
    // Round State
    private final Map<String, Integer> currentAnswers = new ConcurrentHashMap<>(); // username -> answer
    private final Map<String, Long> answerTimes = new ConcurrentHashMap<>(); // username -> timestamp
    private boolean isTeamQuestion = false;
    
    // Synchronization
    private ModifiedCountdownLatch currentLatch;
    private CustomBarrier currentBarrier;
    private static final int QUESTION_TIME_LIMIT_SEC = 30;
    
    public GameState(String gameCode, int numTeams, int playersPerTeam, int totalQuestions) {
        this.gameCode = gameCode;
        this.numTeams = numTeams;
        this.playersPerTeam = playersPerTeam;
        this.totalQuestions = totalQuestions;
    }
    
    public boolean addPlayer(String username, DealWithClient client) {
        // Default team assignment if not specified
        int currentPlayers = clients.size();
        String assignedTeam = "Team" + ((currentPlayers / playersPerTeam) + 1);
        return addPlayer(username, assignedTeam, client);
    }

    public synchronized boolean addPlayer(String username, String teamName, DealWithClient client) {
        if (gameStarted.get() || clients.containsKey(username)) {
            return false;
        }
        clients.put(username, client);
        Player newPlayer = new Player(username);
        newPlayer.setTeamName(teamName);
        players.put(username, newPlayer);
        playerTeams.put(username, teamName);
        return true;
    }

    public boolean isGameStarted() {
        return gameStarted.get();
    }

    public void setQuestions(List<Question> availableQuestions) {
        List<Question> shuffled = new ArrayList<>(availableQuestions);
        Collections.shuffle(shuffled);
        this.questions.clear();
        this.questions.addAll(shuffled.subList(0, Math.min(shuffled.size(), totalQuestions)));
    }
    
    public Collection<Player> getPlayers() {
        return players.values();
    }
    
    public String getGameCode() {
        return gameCode;
    }
    
    // Game Loop Thread
    @Override
    public void run() {
        if (gameStarted.getAndSet(true)) return;
        
        broadcast(new GameStartMessage(questions.size()));
        
        // Wait a bit before starting
        synchronized (this) {
            try { wait(2000); } catch (InterruptedException e) {}
        }

        for (currentQuestionIndex = 0; currentQuestionIndex < questions.size(); currentQuestionIndex++) {
            Question q = questions.get(currentQuestionIndex);
            // Alternate types: Even indices = Individual, Odd = Team
            isTeamQuestion = (currentQuestionIndex % 2 != 0);
            
            // Prepare round
            currentAnswers.clear();
            answerTimes.clear();
            answerFactors.clear();
            
            int playerCount = clients.size();
            
            // Initialize synchronization primitives BEFORE broadcasting to prevent race conditions
            if (playerCount > 0) {
                if (isTeamQuestion) {
                     int parties = playerCount + 1; // Players + Server
                     currentBarrier = new CustomBarrier(parties, this::processTeamAnswers);
                     currentLatch = null;
                } else {
                     // Bonus: First 2 players get 2x points
                     currentLatch = new ModifiedCountdownLatch(2, 2, QUESTION_TIME_LIMIT_SEC, playerCount);
                     currentBarrier = null;
                }
            } else {
                currentBarrier = null;
                currentLatch = null;
            }
            
            QuestionMessage qMsg = new QuestionMessage(q, currentQuestionIndex + 1, questions.size());
            broadcast(qMsg);
            
            // Wait for answers
            if (playerCount > 0) {
                if (isTeamQuestion) {
                    if (currentBarrier != null) {
                        try {
                            currentBarrier.await(QUESTION_TIME_LIMIT_SEC * 1000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (currentLatch != null) {
                        try {
                            currentLatch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // For individual round, we process after latch releases (timeout or all answered)
                        processIndividualAnswers();
                    }
                }
            }
            
            // Round finished
            ScoreBoard sb = new ScoreBoard(new ArrayList<>(players.values()), currentQuestionIndex + 1, questions.size());
            broadcast(new ScoreBoardMessage(sb, false));
            
            // Wait before next question
            synchronized (this) {
                try { wait(5000); } catch (InterruptedException e) {}
            }
        }
        
        gameEnded.set(true);
        ScoreBoard finalSb = new ScoreBoard(new ArrayList<>(players.values()), questions.size(), questions.size());
        broadcast(new ScoreBoardMessage(finalSb, true));
    }

    // Called by DealWithClient
    public void submitAnswer(String username, int answerIndex) {
        if (currentAnswers.containsKey(username)) return; // Only one answer per round
        if (gameEnded.get()) return; // Don't accept answers after game ends

        currentAnswers.put(username, answerIndex);
        answerTimes.put(username, System.currentTimeMillis());

        if (isTeamQuestion) {
            if (currentBarrier != null) {
                try {
                    currentBarrier.await(QUESTION_TIME_LIMIT_SEC * 1000L);
                } catch (InterruptedException e) { /* ignored */ }
            }
        } else {
            if (currentLatch != null) {
                int factor = currentLatch.countdown();
                answerFactors.put(username, factor);
            }
        }
    }
    
    private final Map<String, Integer> answerFactors = new ConcurrentHashMap<>();

    private void processIndividualAnswers() {
        Question q = questions.get(currentQuestionIndex);
        int correct = q.getCorrect();
        
        for (Map.Entry<String, Integer> entry : currentAnswers.entrySet()) {
            String user = entry.getKey();
            int ans = entry.getValue();
            
            if (ans == correct) {
                int factor = answerFactors.getOrDefault(user, 1);
                Player p = players.get(user);
                if (p != null) p.addScore(q.getPoints() * factor);
            }
            Player p = players.get(user);
            if (p != null) p.incrementQuestionsAnswered();
        }
    }
    
    private void processTeamAnswers() {
        Question q = questions.get(currentQuestionIndex);
        int correct = q.getCorrect();
        
        Map<String, List<String>> teamMembersMap = new HashMap<>();
        for (String user : players.keySet()) {
            String team = playerTeams.get(user);
            teamMembersMap.computeIfAbsent(team, k -> new ArrayList<>()).add(user);
        }
        
        for (String team : teamMembersMap.keySet()) {
            List<String> members = teamMembersMap.get(team);
            boolean allCorrect = true;
            boolean anyCorrect = false;
            boolean allAnswered = true; // All members of THIS team answered

            for (String member : members) {
                if (!currentAnswers.containsKey(member)) {
                    allAnswered = false;
                    allCorrect = false;
                } else {
                    int ans = currentAnswers.get(member);
                    if (ans == correct) {
                        anyCorrect = true;
                    } else {
                        allCorrect = false;
                    }
                }
                Player p = players.get(member);
                if (p != null) p.incrementQuestionsAnswered();
            }
            
            int points = 0;
            if (allCorrect && allAnswered) { // All team members answered and all were correct
                points = q.getPoints() * 2;
            } else if (anyCorrect) { // At least one was correct, and not all were correct
                points = q.getPoints();
            }
            
            if (points > 0) {
                for (String member : members) {
                    Player p = players.get(member);
                    if (p != null) p.addScore(points);
                }
            }
        }
    }

    private void broadcast(Message msg) {
        for (DealWithClient client : clients.values()) {
            client.send(msg);
        }
    }
}
