package iskahoot.demo;

import iskahoot.client.ClientInterface;
import iskahoot.client.gui.GameGUI;
import iskahoot.model.*;
import iskahoot.util.QuestionLoader;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

/**
 * Simple GUI-only demo for showcasing the enhanced interface
 */
public class GUIDemo {
    
    public static void main(String[] args) {
        // Set system look and feel for better appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Use default look and feel
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                showGUIDemo();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, 
                    "Error starting GUI demo: " + e.getMessage(), 
                    "Demo Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private static void showGUIDemo() throws IOException {
        System.out.println("Starting IsKahoot GUI Demo...");
        
        // Ask for username
        String username = JOptionPane.showInputDialog(
            null,
            "Enter your username:",
            "IsKahoot - Player Name",
            JOptionPane.QUESTION_MESSAGE
        );
        
        // If user cancels or enters empty name, use default
        if (username == null || username.trim().isEmpty()) {
            username = "Player";
        }
        username = username.trim();
        
        // Load questions first
        List<Question> questions = QuestionLoader.loadQuestionsFromFile("resources/questions.json");
        
        // Create game state to track actual scores
        GameState gameState = new GameState("DEMO", 2, 1, questions.size());
        gameState.setQuestions(questions);
        gameState.addPlayer(username, "TEAM1");
        gameState.addPlayer("RobotPlayer", "TEAM2");
        
        // Start game after all players are added
        gameState.startGame();
        
        // Create mock client that signals when answer is submitted
        MockClient mockClient = new MockClient();
        
        // Create and show GUI with player identification
        GameGUI gui = new GameGUI(mockClient, username, "TEAM1");
        gui.setVisible(true);
        
        // Demo sequence - advances only when user submits answer or timeout occurs
        final boolean[] answerSubmitted = {false};
        final Timer[] countdownTimer = {null};
        final Timer[] timeoutTimer = {null};
        final Question[] currentQuestionRef = {null};
        
        // Function to show the next question - declared first as array to allow self-reference
        final Runnable[] showNextQuestion = {null};
        
        showNextQuestion[0] = () -> {
            answerSubmitted[0] = false;
            
            Question nextQ = gameState.getCurrentQuestion();
            if (nextQ != null) {
                currentQuestionRef[0] = nextQ;
                gameState.startRound();
                gui.displayQuestion(nextQ);
                countdownTimer[0] = startCountdownDemo(gui, 5);
                
                // Timeout after 6 seconds if no answer submitted (5 seconds countdown + 1 second for initial display)
                timeoutTimer[0] = new Timer(7000, e -> {
                    if (!answerSubmitted[0]) {
                        answerSubmitted[0] = true; // Prevent double execution
                        
                        // Stop countdown timer
                        if (countdownTimer[0] != null && countdownTimer[0].isRunning()) {
                            countdownTimer[0].stop();
                        }
                        
                        // Get player username
                        String playerUsername = gameState.getPlayers().stream()
                            .filter(p -> p.getTeamCode().equals("TEAM1"))
                            .findFirst()
                            .map(Player::getUsername)
                            .orElse("Player");
                        
                        // Robot answers if player didn't
                        int robotAnswer = Math.random() < 0.5 ? currentQuestionRef[0].getCorrect() : (currentQuestionRef[0].getCorrect() + 1) % currentQuestionRef[0].getOptions().length;
                        gameState.submitAnswer("RobotPlayer", robotAnswer);
                        gameState.endRound();
                        
                        // Show timeout feedback
                        gui.showAnswerFeedback(false, currentQuestionRef[0].getCorrect());
                        
                        // Show scoreboard
                        ScoreBoard scoreBoard = gameState.getScoreBoard();
                        gui.displayScoreboard(scoreBoard);
                        
                        // Pause to show scoreboard, then advance
                        Timer advanceTimer = new Timer(3000, ev -> {
                            if (gameState.nextQuestion()) {
                                showNextQuestion[0].run();
                            } else {
                                gui.showGameEnd(gameState.getScoreBoard());
                            }
                        });
                        advanceTimer.setRepeats(false);
                        advanceTimer.start();
                    }
                });
                timeoutTimer[0].setRepeats(false);
                timeoutTimer[0].start();
            }
        };
        
        // Set up mock client callback to track answer submission
        mockClient.setOnAnswerSubmitted(() -> {
            if (!answerSubmitted[0]) { // Prevent double execution
                answerSubmitted[0] = true;
                
                // Stop all timers
                if (countdownTimer[0] != null && countdownTimer[0].isRunning()) {
                    countdownTimer[0].stop();
                }
                if (timeoutTimer[0] != null && timeoutTimer[0].isRunning()) {
                    timeoutTimer[0].stop();
                }
                
                // Submit player's answer
                String playerUsername = gameState.getPlayers().stream()
                    .filter(p -> p.getTeamCode().equals("TEAM1"))
                    .findFirst()
                    .map(Player::getUsername)
                    .orElse("Player");
                int playerAnswer = mockClient.getLastAnswer();
                gameState.submitAnswer(playerUsername, playerAnswer);
                
                // Robot answers randomly
                int robotAnswer = Math.random() < 0.5 ? currentQuestionRef[0].getCorrect() : (currentQuestionRef[0].getCorrect() + 1) % currentQuestionRef[0].getOptions().length;
                gameState.submitAnswer("RobotPlayer", robotAnswer);
                
                // End round and calculate scores
                gameState.endRound();
                
                // Show answer feedback to GUI
                boolean isCorrect = playerAnswer == currentQuestionRef[0].getCorrect();
                gui.showAnswerFeedback(isCorrect, currentQuestionRef[0].getCorrect());
                
                // Show scoreboard after a delay
                ScoreBoard scoreBoard = gameState.getScoreBoard();
                gui.displayScoreboard(scoreBoard);
                
                // Pause to show scoreboard, then advance
                Timer advanceTimer = new Timer(3000, e -> {
                    if (gameState.nextQuestion()) {
                        showNextQuestion[0].run();
                    } else {
                        gui.showGameEnd(gameState.getScoreBoard());
                    }
                });
                advanceTimer.setRepeats(false);
                advanceTimer.start();
            }
        });
        
        // Show first question by calling showNextQuestion
        showNextQuestion[0].run();
        
        System.out.println("GUI Demo started!");
        System.out.println("Click an answer button to submit your response and move to the next question.");
        System.out.println("If no answer is submitted within the timer, the demo will auto-advance.");
    }
    
    private static Timer startCountdownDemo(GameGUI gui, int startTime) {
        Timer countdownTimer = new Timer(1000, null);
        final int[] timeLeft = {startTime};
        
        countdownTimer.addActionListener(e -> {
            gui.updateTimer(timeLeft[0]);
            timeLeft[0]--;
            
            if (timeLeft[0] < 0) {
                countdownTimer.stop();
            }
        });
        
        countdownTimer.start();
        return countdownTimer;
    }
    

    
    // Mock client class for GUI demo
    private static class MockClient implements ClientInterface {
        private Runnable onAnswerSubmitted;
        private int lastAnswer = -1;
        
        public void setOnAnswerSubmitted(Runnable callback) {
            this.onAnswerSubmitted = callback;
        }
        
        public int getLastAnswer() {
            return lastAnswer;
        }
        
        @Override
        public void sendAnswer(int answerIndex) {
            System.out.println("Demo: Answer " + answerIndex + " selected!");
            this.lastAnswer = answerIndex;
            if (onAnswerSubmitted != null) {
                onAnswerSubmitted.run();
            }
        }
    }
}