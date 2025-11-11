package iskahoot.client.gui;

import iskahoot.model.Question;
import iskahoot.model.ScoreBoard;
import iskahoot.server.GameState;
import iskahoot.util.QuestionLoader;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class GameGUI extends JFrame {
    private final String playerName;

    private JLabel questionLabel;
    private JButton[] answerButtons;
    private JTextArea scoreArea;
    private JLabel timerLabel;
    private JLabel statusLabel;
    private JLabel playerInfoLabel;

    private boolean canAnswer = false;
    private Question currentQuestion;
    private Consumer<Integer> onAnswerSelected;

    public GameGUI(String playerName) {
        this.playerName = playerName;
        initializeComponents();
    }

    private void initializeComponents() {
        setTitle("IsKahoot");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        playerInfoLabel = new JLabel(String.format("Player: %s", playerName));
        mainPanel.add(playerInfoLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        questionLabel = new JLabel("Waiting for game to start...", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        centerPanel.add(questionLabel, BorderLayout.NORTH);

        JPanel answerPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        answerButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            final int answerIndex = i;
            answerButtons[i] = new JButton("Option " + (i + 1));
            answerButtons[i].setEnabled(false);
            answerButtons[i].addActionListener(e -> {
                if (canAnswer) {
                    selectAnswer(answerIndex);
                }
            });
            answerPanel.add(answerButtons[i]);
        }
        centerPanel.add(answerPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        timerLabel = new JLabel("Time: --", SwingConstants.CENTER);
        rightPanel.add(timerLabel, BorderLayout.NORTH);

        scoreArea = new JTextArea("Scoreboard will be shown here.");
        scoreArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(scoreArea);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        statusLabel = new JLabel("Connected.");
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void selectAnswer(int answerIndex) {
        canAnswer = false;
        for (JButton button : answerButtons) {
            button.setEnabled(false);
        }
        if (onAnswerSelected != null) {
            onAnswerSelected.accept(answerIndex);
        }
        statusLabel.setText("Answer " + (char) ('A' + answerIndex) + " selected. Waiting for next round...");
    }

    public void displayQuestion(Question question) {
        SwingUtilities.invokeLater(() -> {
            this.currentQuestion = question;
            questionLabel.setText("<html><div style='text-align: center;'>" + question.getQuestion() + " (" + question.getPoints() + " pts)</div></html>");
            String[] options = question.getOptions();
            for (int i = 0; i < answerButtons.length; i++) {
                if (i < options.length) {
                    answerButtons[i].setText(options[i]);
                    answerButtons[i].setEnabled(true);
                } else {
                    answerButtons[i].setText("");
                    answerButtons[i].setEnabled(false);
                }
            }
            canAnswer = true;
            statusLabel.setText("Choose your answer!");
        });
    }

    public void updateTimer(int seconds) {
        SwingUtilities.invokeLater(() -> timerLabel.setText("Time: " + seconds));
    }

    public void displayScoreboard(ScoreBoard scoreBoard) {
        SwingUtilities.invokeLater(() -> {
            scoreArea.setText(scoreBoard.getFormattedScores());
        });
    }

    public void showAnswerFeedback(boolean isCorrect, int correctAnswerIndex) {
        SwingUtilities.invokeLater(() -> {
            if (isCorrect) {
                statusLabel.setText("Correct! You earned " + currentQuestion.getPoints() + " points.");
            } else {
                statusLabel.setText("Wrong! The correct answer was: " + currentQuestion.getOptions()[correctAnswerIndex]);
            }
        });
    }

    public void showGameEnd(ScoreBoard finalScores) {
        SwingUtilities.invokeLater(() -> {
            canAnswer = false;
            questionLabel.setText("GAME OVER!");
            for (JButton button : answerButtons) {
                button.setEnabled(false);
            }
            displayScoreboard(finalScores);

            String winnerMessage;
            if (finalScores != null && !finalScores.getPlayers().isEmpty()) {
                var winningPlayer = finalScores.getWinningPlayer();
                winnerMessage = String.format("Winner: %s with %d points!", winningPlayer.getUsername(), winningPlayer.getScore());
            } else {
                winnerMessage = "Thanks for playing!";
            }
            statusLabel.setText(winnerMessage);
            JOptionPane.showMessageDialog(this, winnerMessage, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public void setOnAnswerSelected(Consumer<Integer> onAnswerSelected) {
        this.onAnswerSelected = onAnswerSelected;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
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
        String username = JOptionPane.showInputDialog(null, "Enter your username:", "IsKahoot", JOptionPane.QUESTION_MESSAGE);
        if (username == null || username.trim().isEmpty()) {
            username = "Player1";
        }

        List<Question> questions = QuestionLoader.loadQuestionsFromFile("resources/questions.json");
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Failed to load questions or file is empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        GameState gameState = new GameState("DEMO", questions.size());
        gameState.setQuestions(questions);
        gameState.addPlayer(username);
        gameState.startGame();

        GameGUI gui = new GameGUI(username);
        gui.setVisible(true);

        startDemoGameLogic(gui, gameState, username);
    }

    private static void startDemoGameLogic(GameGUI gui, GameState gameState, String username) {
        final boolean[] answerSubmitted = {false};
        final Timer[] countdownTimer = {null};
        final Timer[] timeoutTimer = {null};
        final Question[] currentQuestionRef = {null};
        final Runnable[] showNextQuestion = {null};

        showNextQuestion[0] = () -> {
            answerSubmitted[0] = false;
            Question nextQ = gameState.getCurrentQuestion();

            if (nextQ != null) {
                currentQuestionRef[0] = nextQ;
                gameState.startRound();
                gui.displayQuestion(nextQ);
                countdownTimer[0] = startCountdownDemo(gui, 30);

                timeoutTimer[0] = new Timer(31000, e -> {
                    if (!answerSubmitted[0]) {
                        answerSubmitted[0] = true;
                        if (countdownTimer[0] != null) countdownTimer[0].stop();
                        
                        gameState.endRound();
                        gui.showAnswerFeedback(false, currentQuestionRef[0].getCorrect());
                        gui.displayScoreboard(gameState.getScoreBoard());

                        new Timer(3000, ev -> {
                            if (gameState.nextQuestion()) {
                                showNextQuestion[0].run();
                            } else {
                                gui.showGameEnd(gameState.getScoreBoard());
                            }
                        }) {{ setRepeats(false); start(); }};
                    }
                });
                timeoutTimer[0].setRepeats(false);
                timeoutTimer[0].start();
            } else {
                gui.showGameEnd(gameState.getScoreBoard());
            }
        };

        gui.setOnAnswerSelected(playerAnswer -> {
            if (!answerSubmitted[0]) {
                answerSubmitted[0] = true;
                if (countdownTimer[0] != null) countdownTimer[0].stop();
                if (timeoutTimer[0] != null) timeoutTimer[0].stop();

                gameState.submitAnswer(username, playerAnswer);
                gameState.endRound();

                boolean isCorrect = playerAnswer == currentQuestionRef[0].getCorrect();
                gui.showAnswerFeedback(isCorrect, currentQuestionRef[0].getCorrect());
                gui.displayScoreboard(gameState.getScoreBoard());

                new Timer(3000, e -> {
                    if (gameState.nextQuestion()) {
                        showNextQuestion[0].run();
                    } else {
                        gui.showGameEnd(gameState.getScoreBoard());
                    }
                }) {{ setRepeats(false); start(); }};
            }
        });

        showNextQuestion[0].run();
    }

    private static Timer startCountdownDemo(GameGUI gui, int startTime) {
        final int[] timeLeft = {startTime};
        Timer countdownTimer = new Timer(1000, e -> {
            gui.updateTimer(timeLeft[0]);
            if (timeLeft[0] > 0) {
                timeLeft[0]--;
            } else {
                ((Timer) e.getSource()).stop();
            }
        });
        countdownTimer.start();
        return countdownTimer;
    }
}