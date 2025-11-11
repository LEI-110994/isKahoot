package iskahoot.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the state of a game session
 * Manages players, teams, questions, and scoring
 */
public class GameState {
    private String gameCode;
    private int maxTeams;
    private int playersPerTeam;
    private int totalQuestions;
    
    // Game progress
    private int currentQuestionIndex;
    private Question currentQuestion;
    private boolean gameStarted;
    private boolean gameEnded;
    
    // Players and teams
    private Map<String, Player> players; // username -> Player
    private Map<String, Team> teams; // teamCode -> Team
    private Map<String, String> playerToTeam; // username -> teamCode
    
    // Current round data
    private Map<String, Integer> currentAnswers; // username -> answerIndex
    private Set<String> answeredPlayers;
    private long roundStartTime;
    private boolean roundActive;
    
    // Questions
    private List<Question> questions;
    private Random random;
    
    // Synchronization components (to be implemented later)
    private Object stateLock = new Object();
    
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
        
        this.questions = new ArrayList<>();
        this.random = new Random();
    }
    
    // Player and team management
    public synchronized boolean addPlayer(String username, String teamCode) {
        if (gameStarted || players.containsKey(username)) {
            return false;
        }
        
        // Check if team exists or can be created
        Team team = teams.get(teamCode);
        if (team == null) {
            if (teams.size() >= maxTeams) {
                return false; // Too many teams
            }
            team = new Team(teamCode);
            teams.put(teamCode, team);
        }
        
        // Check if team has space
        if (team.getPlayerCount() >= playersPerTeam) {
            return false;
        }
        
        // Add player
        Player player = new Player(username, teamCode);
        players.put(username, player);
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
        selectRandomQuestions();
        currentQuestionIndex = 0;
    }
    
    private void selectRandomQuestions() {
        Collections.shuffle(questions, random);
        if (questions.size() > totalQuestions) {
            questions = questions.subList(0, totalQuestions);
        }
    }
    
    // Question management
    public synchronized Question getCurrentQuestion() {
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
        roundStartTime = System.currentTimeMillis();
        roundActive = true;
    }
    
    public synchronized boolean submitAnswer(String username, int answerIndex) {
        if (!roundActive || answeredPlayers.contains(username)) {
            return false;
        }
        
        currentAnswers.put(username, answerIndex);
        answeredPlayers.add(username);
        
        return true;
    }
    
    public synchronized boolean isRoundComplete() {
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
            if (player != null && answer == correctAnswer) {
                player.addScore(basePoints);
                
                // Add to team score
                String teamCode = playerToTeam.get(username);
                Team team = teams.get(teamCode);
                if (team != null) {
                    team.addScore(basePoints);
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
    
    public synchronized ScoreBoard getScoreBoard() {
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
        this.questions = new ArrayList<>(questions);
    }
    
    public void addQuestion(Question question) {
        this.questions.add(question);
    }
}