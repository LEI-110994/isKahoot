package iskahoot.client.gui;

import iskahoot.client.ClientInterface;
import iskahoot.model.Question;
import iskahoot.model.ScoreBoard;

import javax.swing.*;
import java.awt.*;

public class GameGUI extends JFrame {
    private final ClientInterface client;
    private final String playerName;
    private final String teamName;

    private JLabel questionLabel;
    private JButton[] answerButtons;
    private JTextArea scoreArea;
    private JLabel timerLabel;
    private JLabel statusLabel;
    private JLabel playerInfoLabel;

    private boolean canAnswer = false;
    private Question currentQuestion;

    public GameGUI(ClientInterface client, String playerName, String teamName) {
        this.client = client;
        this.playerName = playerName;
        this.teamName = teamName;
        initializeComponents();
    }

    private void initializeComponents() {
        setTitle("IsKahoot");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        playerInfoLabel = new JLabel(String.format("Player: %s | Team: %s", playerName, teamName));
        mainPanel.add(playerInfoLabel, BorderLayout.NORTH);

        // Center panel for question and answers
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

        // Right panel for timer and scoreboard
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        timerLabel = new JLabel("Time: --", SwingConstants.CENTER);
        rightPanel.add(timerLabel, BorderLayout.NORTH);

        scoreArea = new JTextArea("Scoreboard will be shown here.");
        scoreArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(scoreArea);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        // Status bar
        statusLabel = new JLabel("Connected.");
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void selectAnswer(int answerIndex) {
        canAnswer = false;
        for (JButton button : answerButtons) {
            button.setEnabled(false);
        }
        client.sendAnswer(answerIndex);
        statusLabel.setText("Answer " + (char)('A' + answerIndex) + " selected. Waiting for next round...");
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
            StringBuilder sb = new StringBuilder();
            sb.append("--- SCOREBOARD ---\n");
            sb.append("Question ").append(scoreBoard.getCurrentQuestion()).append("/").append(scoreBoard.getTotalQuestions()).append("\n\n");
            for (var team : scoreBoard.getTeams()) {
                sb.append(String.format("Team %s: %d pts\n", team.getTeamCode(), team.getScore()));
                for (var player : team.getPlayers()) {
                    sb.append(String.format("  - %s: %d pts\n", player.getUsername(), player.getScore()));
                }
            }
            scoreArea.setText(sb.toString());
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
            if (finalScores != null && !finalScores.getTeams().isEmpty()) {
                var winningTeam = finalScores.getWinningTeam();
                winnerMessage = String.format("Winner: Team %s with %d points!", winningTeam.getTeamCode(), winningTeam.getScore());
            } else {
                winnerMessage = "Thanks for playing!";
            }
            statusLabel.setText(winnerMessage);
            JOptionPane.showMessageDialog(this, winnerMessage, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public void handleServerMessage(Object message) {
        SwingUtilities.invokeLater(() -> {
            if (message instanceof Question) {
                displayQuestion((Question) message);
            } else if (message instanceof ScoreBoard) {
                displayScoreboard((ScoreBoard) message);
            } else if (message instanceof Integer) {
                updateTimer((Integer) message);
            } else if (message instanceof String) {
                String msg = (String) message;
                if (msg.startsWith("FEEDBACK:")) {
                    String[] parts = msg.split(":");
                    boolean isCorrect = Boolean.parseBoolean(parts[1]);
                    int correctIndex = Integer.parseInt(parts[2]);
                    showAnswerFeedback(isCorrect, correctIndex);
                } else if (msg.equals("GAME_END")) {
                    // This part needs to be connected to the client logic 
                    // to receive the final scoreboard.
                    // For now, we can't show the final scores without that object.
                    showGameEnd(null); 
                }
            }
        });
    }
}