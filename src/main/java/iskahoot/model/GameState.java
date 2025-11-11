package iskahoot.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents the state of a game session
 * Manages players, teams, questions, and scoring
 */
public class GameState {
    private final String gameCode;
    private final int maxTeams;
    private final int playersPerTeam;
    private final int totalQuestions;
    
    // Game progress
    private volatile int currentQuestionIndex;
    private volatile Question currentQuestion;
    private volatile boolean gameStarted;
    private volatile boolean gameEnded;
    
    // Players and teams
    private final Map<String, Player> players; // username -> Player
    private final Map<String, Team> teams; // teamCode -> Team
    private final Map<String, String> playerToTeam; // username -> teamCode
    
    // Current round data
    private final Map<String, Integer> currentAnswers; // username -> answerIndex
    private final Set<String> answeredPlayers;
    private volatile boolean roundActive;
    
    // Questions
    private List<Question> questions;
    private final Random random;
    
    public GameState(String gameCode, int maxTeams, int playersPerTeam, int totalQuestions) {
        this.gameCode = gameCode;
        this.maxTeams = maxTeams;
        this.playersPerTeam = playersPerTeam;
        this.totalQuestions = totalQuestions;
        
        this.currentQuestionIndex = 0;
        this.gameStarted = false;
        this.gameEnded = false;
        this.roundActive = false;
        
        this.players = new ConcurrentHashMap<>();
        this.teams = new ConcurrentHashMap<>();
        this.playerToTeam = new ConcurrentHashMap<>();
        this.currentAnswers = new ConcurrentHashMap<>();
        this.answeredPlayers = ConcurrentHashMap.newKeySet();
        
        this.questions = new CopyOnWriteArrayList<>();
        this.random = new Random();
    }
    
    // Player and team management
    public synchronized boolean addPlayer(String username, String teamCode) {
        if (gameStarted || players.containsKey(username)) {
            return false;
        }
        
        // Check if team exists or can be created
        Team team = teams.computeIfAbsent(teamCode, k -> {
            if (teams.size() >= maxTeams) {
                return null;
            }
            return new Team(k);
        });

        if (team == null) {
            return false; // Max teams reached
        }
        
        // Check if team has space
        if (team.getPlayerCount() >= playersPerTeam) {
            return false;
        }
        
        // Add player
        Player player = new Player(username, teamCode);
        if (players.putIfAbsent(username, player) != null) {
            return false; // Player already exists
        }
        team.addPlayer(player);
        playerToTeam.put(username, teamCode);
        
        return true;
    }
    
    public synchronized boolean canStartGame() {
        if (gameStarted || teams.isEmpty()) {
            return false;
        }
        
        // Check if all teams have the required number of players
        for (Team team : teams.values()) {
            if (team.getPlayerCount() != playersPerTeam) {
                return false;
            }
        }
        
        return questions.size() >= totalQuestions;
    }
    
    public synchronized void startGame() {
        if (!canStartGame()) {
            throw new IllegalStateException("Cannot start game - requirements not met");
        }
        
        gameStarted = true;
        currentQuestionIndex = 0;
    }
    
    // Question management
    public Question getCurrentQuestion() {
        if (currentQuestionIndex < questions.size()) {
            currentQuestion = questions.get(currentQuestionIndex);
            return currentQuestion;
        }
        return null;
    }
    
    public synchronized void startRound() {
        if (!gameStarted || roundActive) {
            return;
        }
        
        currentAnswers.clear();
        answeredPlayers.clear();
        roundActive = true;
    }
    
    public boolean submitAnswer(String username, int answerIndex) {
        if (!roundActive) {
            return false;
        }
        
        if (answeredPlayers.add(username)) {
            currentAnswers.put(username, answerIndex);
            return true;
        }
        return false;
    }
    
    public boolean isRoundComplete() {
        return answeredPlayers.size() >= players.size();
    }
    
    public synchronized void endRound() {
        if (!roundActive) {
            return;
        }
        
        roundActive = false;
        processAnswers();
    }
    
    private void processAnswers() {
        if (currentQuestion == null) {
            return;
        }
        
        int correctAnswer = currentQuestion.getCorrect();
        int basePoints = currentQuestion.getPoints();
        
        // Process individual answers
        for (Map.Entry<String, Integer> entry : currentAnswers.entrySet()) {
            String username = entry.getKey();
            int answer = entry.getValue();
            
            Player player = players.get(username);
            if (player != null) {
                // Increment questions answered for all players who submitted
                player.incrementQuestionsAnswered();
                
                if (answer == correctAnswer) {
                    player.addScore(basePoints);
                }
            }
        }
    }
    
    public synchronized boolean nextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex >= questions.size()) {
            gameEnded = true;
            return false;
        }
        return true;
    }
    
    public ScoreBoard getScoreBoard() {
        List<Team> sortedTeams = new ArrayList<>(teams.values());
        sortedTeams.sort((t1, t2) -> Integer.compare(t2.getScore(), t1.getScore()));
        
        return new ScoreBoard(sortedTeams, currentQuestionIndex + 1, questions.size());
    }
    
    // Getters
    public String getGameCode() { return gameCode; }
    public boolean isGameStarted() { return gameStarted; }
    public boolean isGameEnded() { return gameEnded; }
    public boolean isRoundActive() { return roundActive; }
    public int getCurrentQuestionNumber() { return currentQuestionIndex + 1; }
    public int getTotalQuestions() { return questions.size(); }
    public Collection<Player> getPlayers() { return players.values(); }
    public Collection<Team> getTeams() { return teams.values(); }
    
    // Question management
    public void setQuestions(List<Question> questions) {
        List<Question> shuffledQuestions = new ArrayList<>(questions);
        Collections.shuffle(shuffledQuestions, random);
        if (shuffledQuestions.size() > totalQuestions) {
            this.questions = new CopyOnWriteArrayList<>(shuffledQuestions.subList(0, totalQuestions));
        } else {
            this.questions = new CopyOnWriteArrayList<>(shuffledQuestions);
        }
    }
    
    public void addQuestion(Question question) {
        this.questions.add(question);
    }
}