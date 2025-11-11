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
        } catch (Exception e) {
            // Use default look and feel
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                showGUIDemo();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error starting GUI demo: " + e.getMessage(), 
                    "Demo Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private static void showGUIDemo() throws IOException {
        System.out.println("Starting IsKahoot GUI Demo...");
        
        // Create mock client that signals when answer is submitted
        MockClient mockClient = new MockClient();
        
        // Create and show GUI
        GameGUI gui = new GameGUI(mockClient);
        gui.setVisible(true);
        
        // Load questions for demo
        List<Question> questions = QuestionLoader.loadQuestionsFromFile("resources/questions.json");
        
        // Demo sequence - advances only when user submits answer or timeout occurs
        final int[] currentQuestionIndex = {0};
        final boolean[] answerSubmitted = {false};
        final Timer[] countdownTimer = {null};
        
        // Function to show the next question - declared first as array to allow self-reference
        final Runnable[] showNextQuestion = {null};
        
        showNextQuestion[0] = () -> {
            currentQuestionIndex[0]++;
            if (currentQuestionIndex[0] < questions.size()) {
                answerSubmitted[0] = false;
                gui.displayQuestion(questions.get(currentQuestionIndex[0]));
                countdownTimer[0] = startCountdownDemo(gui, 25);
                
                // Show scoreboard after timeout if no answer submitted
                Timer timeoutTimer = new Timer(26000, e -> {
                    if (!answerSubmitted[0]) {
                        if (countdownTimer[0] != null && countdownTimer[0].isRunning()) {
                            countdownTimer[0].stop();
                        }
                        showSampleScoreboard(gui, currentQuestionIndex[0] + 1);
                        
                        // Auto-advance after 2 seconds
                        Timer autoAdvanceTimer = new Timer(2000, e2 -> {
                            showNextQuestion[0].run();
                        });
                        autoAdvanceTimer.setRepeats(false);
                        autoAdvanceTimer.start();
                    }
                });
                timeoutTimer.setRepeats(false);
                timeoutTimer.start();
            } else {
                // End demo
                showFinalScoreboard(gui);
            }
        };
        
        // Set up mock client callback to track answer submission
        mockClient.setOnAnswerSubmitted(() -> {
            answerSubmitted[0] = true;
            // Stop countdown timer
            if (countdownTimer[0] != null && countdownTimer[0].isRunning()) {
                countdownTimer[0].stop();
            }
            // Move to next question after a brief delay
            Timer delayTimer = new Timer(1500, e -> {
                showNextQuestion[0].run();
            });
            delayTimer.setRepeats(false);
            delayTimer.start();
        });
        
        // Show first question
        if (!questions.isEmpty()) {
            gui.displayQuestion(questions.get(currentQuestionIndex[0]));
            countdownTimer[0] = startCountdownDemo(gui, 30);
            
            // Show scoreboard after timeout if no answer submitted
            Timer timeoutTimer = new Timer(31000, e -> {
                if (!answerSubmitted[0]) {
                    if (countdownTimer[0] != null && countdownTimer[0].isRunning()) {
                        countdownTimer[0].stop();
                    }
                    showSampleScoreboard(gui, currentQuestionIndex[0] + 1);
                    
                    // Auto-advance after 2 seconds
                    Timer autoAdvanceTimer = new Timer(2000, e2 -> {
                        showNextQuestion[0].run();
                    });
                    autoAdvanceTimer.setRepeats(false);
                    autoAdvanceTimer.start();
                }
            });
            timeoutTimer.setRepeats(false);
            timeoutTimer.start();
        }
        
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
    
    private static void showSampleScoreboard(GameGUI gui, int questionNumber) {
        // Create sample game state for scoreboard
        GameState demoGame = new GameState("DEMO", 3, 2, 5);
        
        // Add sample teams and players
        demoGame.addPlayer("Alice", "TEAM1");
        demoGame.addPlayer("Bob", "TEAM1");
        demoGame.addPlayer("Charlie", "TEAM2");
        demoGame.addPlayer("Diana", "TEAM2");
        demoGame.addPlayer("Eve", "TEAM3");
        demoGame.addPlayer("Frank", "TEAM3");
        
        // Simulate some scoring
        for (Player player : demoGame.getPlayers()) {
            int randomScore = (int) (Math.random() * 20) + 5;
            player.setScore(randomScore);
            
            String teamCode = player.getTeamCode();
            Team team = demoGame.getTeams().stream()
                .filter(t -> t.getTeamCode().equals(teamCode))
                .findFirst().orElse(null);
            if (team != null) {
                team.setScore(team.getScore() + randomScore);
            }
        }
        
        ScoreBoard scoreBoard = demoGame.getScoreBoard();
        gui.displayScoreboard(scoreBoard);
    }
    
    private static void showFinalScoreboard(GameGUI gui) {
        // Create final scoreboard
        GameState finalGame = new GameState("DEMO", 3, 2, 5);
        
        finalGame.addPlayer("Alice", "TEAM1");
        finalGame.addPlayer("Bob", "TEAM1");
        finalGame.addPlayer("Charlie", "TEAM2");
        finalGame.addPlayer("Diana", "TEAM2");
        finalGame.addPlayer("Eve", "TEAM3");
        finalGame.addPlayer("Frank", "TEAM3");
        
        // Set final scores
        String[] teams = {"TEAM1", "TEAM2", "TEAM3"};
        int[] teamScores = {85, 92, 78};
        
        for (int i = 0; i < teams.length; i++) {
            final String teamCode = teams[i];
            final int score = teamScores[i];
            Team team = finalGame.getTeams().stream()
                .filter(t -> t.getTeamCode().equals(teamCode))
                .findFirst().orElse(null);
            if (team != null) {
                team.setScore(score);
            }
        }
        
        ScoreBoard finalScores = finalGame.getScoreBoard();
        gui.showGameEnd(finalScores);
    }
    
    // Mock client class for GUI demo
    private static class MockClient implements ClientInterface {
        private Runnable onAnswerSubmitted;
        
        public void setOnAnswerSubmitted(Runnable callback) {
            this.onAnswerSubmitted = callback;
        }
        
        @Override
        public void sendAnswer(int answerIndex) {
            System.out.println("Demo: Answer " + answerIndex + " selected!");
            if (onAnswerSubmitted != null) {
                onAnswerSubmitted.run();
            }
        }
    }
}