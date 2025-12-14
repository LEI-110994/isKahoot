package iskahoot.client.gui;

import iskahoot.model.Question;
import iskahoot.model.ScoreBoard;

import javax.swing.*;
import java.awt.*;
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
                if (correctAnswerIndex >= 0 && currentQuestion != null && correctAnswerIndex < currentQuestion.getOptions().length) {
                    statusLabel.setText("Wrong! The correct answer was: " + currentQuestion.getOptions()[correctAnswerIndex]);
                } else {
                     statusLabel.setText("Round ended.");
                }
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
            if (finalScores != null) {
                String winningTeam = finalScores.getWinningTeam();
                winnerMessage = String.format("Winning Team: %s", winningTeam);
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
}
