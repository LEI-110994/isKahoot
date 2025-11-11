package iskahoot.demo;

import iskahoot.client.ClientInterface;
import iskahoot.client.gui.GameGUI;
import iskahoot.model.*;
import iskahoot.util.QuestionLoader;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

/**
 * Demo class to test the first 3 phases of the IsKahoot project
 */
public class PhaseDemo {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                demonstratePhases();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error in demo: " + e.getMessage(), 
                    "Demo Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private static void demonstratePhases() throws IOException {
        System.out.println("=== IsKahoot Project - First 3 Phases Demo ===\n");
        
        // Phase 3: Question Loading
        demonstratePhase3();
        
        // Phase 2: GameState Structure
        demonstratePhase2();
        
        // Phase 1: GUI (will open a window)
        demonstratePhase1();
    }
    
    private static void demonstratePhase3() throws IOException {
        System.out.println("PHASE 3: Question File Processing");
        System.out.println("Loading questions from JSON file...");
        
        List<Question> questions = QuestionLoader.loadQuestionsFromFile("resources/questions.json");
        
        System.out.println("Loaded " + questions.size() + " questions:");
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            System.out.println((i + 1) + ". " + q.getQuestion() + " (" + q.getPoints() + " points)");
            for (int j = 0; j < q.getOptions().length; j++) {
                String marker = (j == q.getCorrect()) ? " ✓" : "";
                System.out.println("   " + j + ": " + q.getOptions()[j] + marker);
            }
            System.out.println();
        }
        System.out.println("Phase 3 completed successfully!\n");
    }
    
    private static void demonstratePhase2() {
        System.out.println("PHASE 2: GameState Structure");
        System.out.println("Creating game state and managing players/teams...");
        
        // Create a game
        GameState game = new GameState("DEMO123", 2, 2, 4);
        
        try {
            // Load questions
            List<Question> questions = QuestionLoader.loadQuestionsFromFile("resources/questions.json");
            game.setQuestions(questions);
            
            // Add players
            System.out.println("Adding players to teams...");
            game.addPlayer("Alice", "TEAM1");
            game.addPlayer("Bob", "TEAM1");
            game.addPlayer("Charlie", "TEAM2");
            game.addPlayer("Diana", "TEAM2");
            
            System.out.println("Players added successfully!");
            System.out.println("Can start game: " + game.canStartGame());
            
            if (game.canStartGame()) {
                game.startGame();
                System.out.println("Game started!");
                
                // Simulate a round
                Question currentQ = game.getCurrentQuestion();
                if (currentQ != null) {
                    System.out.println("Current question: " + currentQ.getQuestion());
                    
                    game.startRound();
                    
                    // Simulate answers
                    game.submitAnswer("Alice", 1);
                    game.submitAnswer("Bob", currentQ.getCorrect());
                    game.submitAnswer("Charlie", 0);
                    game.submitAnswer("Diana", currentQ.getCorrect());
                    
                    game.endRound();
                    
                    ScoreBoard scoreBoard = game.getScoreBoard();
                    System.out.println("Scoreboard after round 1:");
                    System.out.println(scoreBoard.getFormattedScores());
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error loading questions: " + e.getMessage());
        }
        
        System.out.println("Phase 2 completed successfully!\n");
    }
    
    private static void demonstratePhase1() {
        System.out.println("PHASE 1: GUI Development");
        System.out.println("Opening GUI window (this is a demo without server connection)...");
        
        // Create a mock client for GUI demo
        MockClient mockClient = new MockClient();
        GameGUI gui = new GameGUI(mockClient);
        gui.setVisible(true);
        
        // Simulate receiving a question after 2 seconds
        Timer timer = new Timer(2000, e -> {
            try {
                List<Question> questions = QuestionLoader.loadQuestionsFromFile("resources/questions.json");
                if (!questions.isEmpty()) {
                    gui.displayQuestion(questions.get(0));
                    
                    // Start countdown
                    startCountdownDemo(gui);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        timer.setRepeats(false);
        timer.start();
        
        System.out.println("Phase 1 GUI opened! Check the window that appeared.");
        System.out.println("The GUI will show a sample question after 2 seconds.\n");
        
        System.out.println("=== All 3 Phases Demonstrated Successfully! ===");
    }
    
    private static void startCountdownDemo(GameGUI gui) {
        Timer countdownTimer = new Timer(1000, null);
        final int[] timeLeft = {30};
        
        countdownTimer.addActionListener(e -> {
            gui.updateTimer(timeLeft[0]);
            timeLeft[0]--;
            
            if (timeLeft[0] < 0) {
                countdownTimer.stop();
                
                // Show sample scoreboard
                GameState demoGame = new GameState("DEMO", 2, 2, 1);
                demoGame.addPlayer("Alice", "TEAM1");
                demoGame.addPlayer("Bob", "TEAM1");
                demoGame.addPlayer("Charlie", "TEAM2");
                demoGame.addPlayer("Diana", "TEAM2");
                
                ScoreBoard scoreBoard = demoGame.getScoreBoard();
                gui.displayScoreboard(scoreBoard);
            }
        });
        
        countdownTimer.start();
    }
    
    // Mock client class for GUI demo
    private static class MockClient implements ClientInterface {
        public void sendAnswer(int answerIndex) {
            System.out.println("Mock: Answer " + answerIndex + " sent to server");
        }
    }
}