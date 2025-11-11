package iskahoot.server;

import iskahoot.model.Player;
import iskahoot.model.Question;
import iskahoot.model.ScoreBoard;

import java.util.*;

/**
 * Represents the state of a game session.
 * For the initial phases, this is a simplified, non-concurrent version.
 */
public class GameState {
    private final String gameCode;
    private final int totalQuestions;
    
    // Game progress
    private int currentQuestionIndex;
    private Question currentQuestion;
    private boolean gameStarted;
    private boolean gameEnded;
    
    // Players
    private final Map<String, Player> players; // username -> Player
    
    // Current round data
    private final Map<String, Integer> currentAnswers; // username -> answerIndex
    private final Set<String> answeredPlayers;
    private boolean roundActive;
    
    // Questions
    private List<Question> questions;
    private final Random random;
    
    public GameState(String gameCode, int totalQuestions) {
        this.gameCode = gameCode;
        this.totalQuestions = totalQuestions;
        
        this.currentQuestionIndex = 0;
        this.gameStarted = false;
        this.gameEnded = false;
        this.roundActive = false;
        
        this.players = new HashMap<>();
        this.currentAnswers = new HashMap<>();
        this.answeredPlayers = new HashSet<>();
        
        this.questions = new ArrayList<>();
        this.random = new Random();
    }
    
    // Player management
    public boolean addPlayer(String username) {
        if (gameStarted || players.containsKey(username)) {
            return false;
        }
        Player player = new Player(username);
        players.put(username, player);
        return true;
    }
    
    public void startGame() {
        if (gameStarted) {
            return;
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
    
    public void startRound() {
        if (!gameStarted || roundActive) {
            return;
        }
        
        currentAnswers.clear();
        answeredPlayers.clear();
        roundActive = true;
    }
    
    public boolean submitAnswer(String username, int answerIndex) {
        if (!roundActive || !players.containsKey(username)) {
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
    
    public void endRound() {
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
        
        for (Map.Entry<String, Integer> entry : currentAnswers.entrySet()) {
            String username = entry.getKey();
            int answer = entry.getValue();
            
            Player player = players.get(username);
            if (player != null) {
                player.incrementQuestionsAnswered();
                if (answer == correctAnswer) {
                    player.addScore(basePoints);
                }
            }
        }
    }
    
    public boolean nextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex >= questions.size()) {
            gameEnded = true;
            return false;
        }
        return true;
    }
    
    public ScoreBoard getScoreBoard() {
        List<Player> sortedPlayers = new ArrayList<>(players.values());
        sortedPlayers.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));
        
        return new ScoreBoard(sortedPlayers, currentQuestionIndex, questions.size());
    }
    
    // Getters
    public String getGameCode() { return gameCode; }
    public boolean isGameStarted() { return gameStarted; }
    public boolean isGameEnded() { return gameEnded; }
    public boolean isRoundActive() { return roundActive; }
    public int getCurrentQuestionNumber() { return currentQuestionIndex + 1; }
    public int getTotalQuestions() { return this.questions.size(); }
    public Collection<Player> getPlayers() { return players.values(); }
    
    // Question management
    public void setQuestions(List<Question> questions) {
        List<Question> shuffledQuestions = new ArrayList<>(questions);
        Collections.shuffle(shuffledQuestions, random);
        if (shuffledQuestions.size() > totalQuestions) {
            this.questions = new ArrayList<>(shuffledQuestions.subList(0, totalQuestions));
        } else {
            this.questions = new ArrayList<>(shuffledQuestions);
        }
    }
}