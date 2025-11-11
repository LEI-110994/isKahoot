package iskahoot.client.gui;

import iskahoot.client.ClientInterface;
import iskahoot.model.Question;
import iskahoot.model.ScoreBoard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * Enhanced GUI for the IsKahoot client application with modern design
 */
public class GameGUI extends JFrame {
    private final ClientInterface client;
    
    // Color scheme - Kahoot-inspired colors
    private static final Color KAHOOT_BLUE = new Color(70, 23, 143);
    private static final Color KAHOOT_RED = new Color(230, 41, 55);
    private static final Color KAHOOT_YELLOW = new Color(255, 187, 0);
    private static final Color KAHOOT_GREEN = new Color(102, 168, 15);
    private static final Color KAHOOT_ORANGE = new Color(255, 117, 26);
    private static final Color BACKGROUND_DARK = new Color(46, 49, 146);
    private static final Color BACKGROUND_LIGHT = new Color(248, 249, 250);
    private static final Color TEXT_DARK = new Color(51, 51, 51);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    
    // Answer button colors
    private static final Color[] ANSWER_COLORS = {
        KAHOOT_RED, KAHOOT_BLUE, KAHOOT_YELLOW, KAHOOT_GREEN
    };
    
    // Main panels
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel questionPanel;
    private JPanel answerPanel;
    private JPanel timerPanel;
    private JPanel scorePanel;
    private JPanel statusPanel;
    
    // Components
    private JLabel titleLabel;
    private JLabel questionLabel;
    private JButton[] answerButtons;
    private JLabel timerLabel;
    private JLabel timerTextLabel;
    private JTextArea scoreArea;
    private JLabel statusLabel;
    private JProgressBar timerProgressBar;
    
    // Game state
    private boolean canAnswer = false;
    private Timer pulseTimer;
    private int currentTimerValue = 30;
    
    public GameGUI(ClientInterface client) {
        this.client = client;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupAnimations();
    }
    
    private void initializeComponents() {
        setTitle("IsKahoot - Interactive Quiz Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Set application icon (if available)
        try {
            setIconImage(createKahootIcon());
        } catch (Exception e) {
            // Icon not available, continue without it
        }
        
        // Main panel with gradient background
        mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, BACKGROUND_DARK,
                    0, getHeight(), KAHOOT_BLUE
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        // Header panel
        createHeaderPanel();
        
        // Question panel
        createQuestionPanel();
        
        // Answer panel
        createAnswerPanel();
        
        // Timer panel
        createTimerPanel();
        
        // Score panel
        createScorePanel();
        
        // Status panel
        createStatusPanel();
    }
    
    private Image createKahootIcon() {
        // Create a simple colored icon
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(KAHOOT_BLUE);
        g2d.fillOval(2, 2, 28, 28);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("K", 11, 21);
        g2d.dispose();
        return icon;
    }
    
    private void createHeaderPanel() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 30, 10, 30));
        
        titleLabel = new JLabel("IsKahoot", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
    }
    
    private void createQuestionPanel() {
        questionPanel = new JPanel(new BorderLayout());
        questionPanel.setOpaque(false);
        questionPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        // Create card-like panel for question
        JPanel questionCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded rectangle background
                g2d.setColor(CARD_BACKGROUND);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Draw subtle shadow
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillRoundRect(2, 2, getWidth(), getHeight(), 20, 20);
                g2d.setColor(CARD_BACKGROUND);
                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 20, 20);
            }
        };
        questionCard.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        questionLabel = new JLabel("Waiting for game to start...", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 20));
        questionLabel.setForeground(TEXT_DARK);
        
        questionCard.add(questionLabel, BorderLayout.CENTER);
        questionPanel.add(questionCard, BorderLayout.CENTER);
    }
    
    private void createAnswerPanel() {
        answerPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        answerPanel.setOpaque(false);
        answerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        answerButtons = new JButton[4];
        String[] answerLabels = {"A", "B", "C", "D"};
        
        for (int i = 0; i < 4; i++) {
            final int index = i;
            answerButtons[i] = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Draw button background
                    if (getModel().isPressed()) {
                        g2d.setColor(ANSWER_COLORS[index].darker());
                    } else if (getModel().isRollover() && isEnabled()) {
                        g2d.setColor(ANSWER_COLORS[index].brighter());
                    } else {
                        g2d.setColor(isEnabled() ? ANSWER_COLORS[index] : Color.GRAY);
                    }
                    
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    
                    // Draw text
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(getFont());
                    FontMetrics fm = g2d.getFontMetrics();
                    
                    // Draw answer letter
                    String letter = answerLabels[index];
                    int letterWidth = fm.stringWidth(letter);
                    g2d.setFont(new Font("Arial", Font.BOLD, 24));
                    g2d.drawString(letter, 20, 35);
                    
                    // Draw answer text
                    g2d.setFont(new Font("Arial", Font.PLAIN, 16));
                    String text = getText();
                    if (text != null && !text.isEmpty() && !text.startsWith("Option")) {
                        // Remove HTML tags for display
                        text = text.replaceAll("<[^>]*>", "");
                        if (text.length() > 30) {
                            text = text.substring(0, 27) + "...";
                        }
                        g2d.drawString(text, 60, 35);
                    }
                }
            };
            
            answerButtons[i].setPreferredSize(new Dimension(200, 80));
            answerButtons[i].setFont(new Font("Arial", Font.BOLD, 16));
            answerButtons[i].setForeground(Color.WHITE);
            answerButtons[i].setFocusPainted(false);
            answerButtons[i].setBorderPainted(false);
            answerButtons[i].setContentAreaFilled(false);
            answerButtons[i].setEnabled(false);
            answerButtons[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            answerPanel.add(answerButtons[i]);
        }
    }
    
    private void createTimerPanel() {
        timerPanel = new JPanel();
        timerPanel.setLayout(new BoxLayout(timerPanel, BoxLayout.Y_AXIS));
        timerPanel.setOpaque(false);
        timerPanel.setBorder(new EmptyBorder(10, 30, 10, 30));
        
        // Timer text
        timerTextLabel = new JLabel("Time Remaining", SwingConstants.CENTER);
        timerTextLabel.setFont(new Font("Arial", Font.BOLD, 14));
        timerTextLabel.setForeground(Color.WHITE);
        timerTextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Timer value
        timerLabel = new JLabel("--", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 48));
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Progress bar
        timerProgressBar = new JProgressBar(0, 30);
        timerProgressBar.setValue(30);
        timerProgressBar.setStringPainted(false);
        timerProgressBar.setPreferredSize(new Dimension(200, 10));
        timerProgressBar.setMaximumSize(new Dimension(200, 10));
        timerProgressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        timerPanel.add(timerTextLabel);
        timerPanel.add(Box.createVerticalStrut(5));
        timerPanel.add(timerLabel);
        timerPanel.add(Box.createVerticalStrut(10));
        timerPanel.add(timerProgressBar);
    }
    
    private void createScorePanel() {
        scorePanel = new JPanel(new BorderLayout());
        scorePanel.setOpaque(false);
        scorePanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        // Create card for scoreboard
        JPanel scoreCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BACKGROUND);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        scoreCard.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel scoreTitle = new JLabel("Scoreboard", SwingConstants.CENTER);
        scoreTitle.setFont(new Font("Arial", Font.BOLD, 18));
        scoreTitle.setForeground(TEXT_DARK);
        scoreTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        scoreArea = new JTextArea(6, 30);
        scoreArea.setEditable(false);
        scoreArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        scoreArea.setBackground(BACKGROUND_LIGHT);
        scoreArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(scoreArea);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BACKGROUND_LIGHT);
        
        scoreCard.add(scoreTitle, BorderLayout.NORTH);
        scoreCard.add(scrollPane, BorderLayout.CENTER);
        scorePanel.add(scoreCard, BorderLayout.CENTER);
    }
    
    private void createStatusPanel() {
        statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusPanel.setOpaque(false);
        statusPanel.setBorder(new EmptyBorder(10, 30, 20, 30));
        
        statusLabel = new JLabel("Connected - Waiting for game...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        statusLabel.setForeground(Color.WHITE);
        
        statusPanel.add(statusLabel);
    }
    
    private void setupLayout() {
        // Create main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        
        // Top section with header and question
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(headerPanel, BorderLayout.NORTH);
        topSection.add(questionPanel, BorderLayout.CENTER);
        
        // Middle section with timer and answers
        JPanel middleSection = new JPanel(new BorderLayout());
        middleSection.setOpaque(false);
        middleSection.add(timerPanel, BorderLayout.NORTH);
        middleSection.add(answerPanel, BorderLayout.CENTER);
        
        // Bottom section with status and scores
        JPanel bottomSection = new JPanel(new BorderLayout());
        bottomSection.setOpaque(false);
        bottomSection.add(statusPanel, BorderLayout.NORTH);
        bottomSection.add(scorePanel, BorderLayout.CENTER);
        
        // Add all sections to content panel
        contentPanel.add(topSection, BorderLayout.NORTH);
        contentPanel.add(middleSection, BorderLayout.CENTER);
        contentPanel.add(bottomSection, BorderLayout.SOUTH);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);
    }
    
    private void setupEventHandlers() {
        for (int i = 0; i < answerButtons.length; i++) {
            final int answerIndex = i;
            
            // Click handler
            answerButtons[i].addActionListener(e -> {
                if (canAnswer) {
                    selectAnswer(answerIndex);
                }
            });
            
            // Hover effects
            answerButtons[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (answerButtons[answerIndex].isEnabled()) {
                        answerButtons[answerIndex].repaint();
                    }
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    if (answerButtons[answerIndex].isEnabled()) {
                        answerButtons[answerIndex].repaint();
                    }
                }
            });
        }
    }
    
    private void setupAnimations() {
        // Pulse animation for timer when time is running low
        pulseTimer = new Timer(500, e -> {
            if (currentTimerValue <= 5 && currentTimerValue > 0) {
                Color currentColor = timerLabel.getForeground();
                if (currentColor.equals(KAHOOT_RED)) {
                    timerLabel.setForeground(KAHOOT_YELLOW);
                } else {
                    timerLabel.setForeground(KAHOOT_RED);
                }
            }
        });
    }
    
    private void selectAnswer(int answerIndex) {
        canAnswer = false;
        
        // Animate button selection
        animateButtonSelection(answerIndex);
        
        // Send answer to server
        client.sendAnswer(answerIndex);
        statusLabel.setText("✓ Answer sent! Waiting for results...");
        
        // Stop pulse animation
        if (pulseTimer.isRunning()) {
            pulseTimer.stop();
        }
    }
    
    private void animateButtonSelection(int selectedIndex) {
        // Disable all buttons and show selection
        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i].setEnabled(false);
            
            if (i == selectedIndex) {
                // Animate selected button
                Timer selectionTimer = new Timer(100, null);
                final int[] pulseCount = {0};
                
                selectionTimer.addActionListener(e -> {
                    if (pulseCount[0] < 6) {
                        // Create pulsing effect
                        if (pulseCount[0] % 2 == 0) {
                            answerButtons[selectedIndex].setBackground(Color.WHITE);
                        } else {
                            answerButtons[selectedIndex].setBackground(ANSWER_COLORS[selectedIndex]);
                        }
                        answerButtons[selectedIndex].repaint();
                        pulseCount[0]++;
                    } else {
                        // Final state - keep selected color
                        answerButtons[selectedIndex].setBackground(ANSWER_COLORS[selectedIndex].brighter());
                        answerButtons[selectedIndex].repaint();
                        selectionTimer.stop();
                    }
                });
                selectionTimer.start();
            } else {
                // Fade out non-selected buttons
                answerButtons[i].setBackground(Color.LIGHT_GRAY);
                answerButtons[i].repaint();
            }
        }
    }
    
    public void displayQuestion(Question question) {
        SwingUtilities.invokeLater(() -> {
            // Update question text with animation
            String questionText = question.getQuestion();
            questionLabel.setText("<html><div style='text-align: center; padding: 10px;'>" + 
                                questionText + "</div></html>");
            
            // Animate question appearance
            animateQuestionAppearance();
            
            // Update answer buttons
            String[] options = question.getOptions();
            for (int i = 0; i < answerButtons.length && i < options.length; i++) {
                answerButtons[i].setText(options[i]);
                answerButtons[i].setEnabled(true);
                answerButtons[i].setBackground(ANSWER_COLORS[i]);
                
                // Animate button appearance with delay
                final int buttonIndex = i;
                Timer buttonTimer = new Timer(200 * (i + 1), e -> {
                    animateButtonAppearance(buttonIndex);
                });
                buttonTimer.setRepeats(false);
                buttonTimer.start();
            }
            
            canAnswer = true;
            statusLabel.setText("🎯 Choose your answer!");
        });
    }
    
    private void animateQuestionAppearance() {
        // Simple fade-in effect for question
        questionPanel.setVisible(false);
        Timer fadeTimer = new Timer(50, null);
        final float[] alpha = {0.0f};
        
        fadeTimer.addActionListener(e -> {
            alpha[0] += 0.1f;
            if (alpha[0] >= 1.0f) {
                alpha[0] = 1.0f;
                fadeTimer.stop();
            }
            questionPanel.setVisible(true);
            questionPanel.repaint();
        });
        fadeTimer.start();
    }
    
    private void animateButtonAppearance(int buttonIndex) {
        // Scale animation for buttons
        answerButtons[buttonIndex].setVisible(true);
        
        Timer scaleTimer = new Timer(30, null);
        final double[] scale = {0.5};
        final Dimension originalSize = answerButtons[buttonIndex].getPreferredSize();
        
        scaleTimer.addActionListener(e -> {
            scale[0] += 0.1;
            if (scale[0] >= 1.0) {
                scale[0] = 1.0;
                scaleTimer.stop();
            }
            
            int newWidth = (int) (originalSize.width * scale[0]);
            int newHeight = (int) (originalSize.height * scale[0]);
            answerButtons[buttonIndex].setPreferredSize(new Dimension(newWidth, newHeight));
            answerButtons[buttonIndex].revalidate();
        });
        scaleTimer.start();
    }
    
    public void updateTimer(int seconds) {
        SwingUtilities.invokeLater(() -> {
            currentTimerValue = seconds;
            timerLabel.setText(String.valueOf(seconds));
            timerProgressBar.setValue(seconds);
            
            // Update colors based on time remaining
            if (seconds <= 5) {
                timerLabel.setForeground(KAHOOT_RED);
                timerProgressBar.setForeground(KAHOOT_RED);
                if (!pulseTimer.isRunning()) {
                    pulseTimer.start();
                }
            } else if (seconds <= 10) {
                timerLabel.setForeground(KAHOOT_ORANGE);
                timerProgressBar.setForeground(KAHOOT_ORANGE);
                if (pulseTimer.isRunning()) {
                    pulseTimer.stop();
                }
            } else {
                timerLabel.setForeground(KAHOOT_GREEN);
                timerProgressBar.setForeground(KAHOOT_GREEN);
                if (pulseTimer.isRunning()) {
                    pulseTimer.stop();
                }
            }
            
            // Add urgency animation for last 3 seconds
            if (seconds <= 3 && seconds > 0) {
                Timer urgencyTimer = new Timer(200, e -> {
                    Font currentFont = timerLabel.getFont();
                    if (currentFont.getSize() == 48) {
                        timerLabel.setFont(new Font("Arial", Font.BOLD, 52));
                    } else {
                        timerLabel.setFont(new Font("Arial", Font.BOLD, 48));
                    }
                });
                urgencyTimer.setRepeats(false);
                urgencyTimer.start();
            }
        });
    }
    
    public void displayScoreboard(ScoreBoard scoreBoard) {
        SwingUtilities.invokeLater(() -> {
            // Enhanced scoreboard formatting
            String formattedScores = formatScoreboardWithEmojis(scoreBoard);
            scoreArea.setText(formattedScores);
            
            // Animate scoreboard update
            animateScoreboardUpdate();
        });
    }
    
    private String formatScoreboardWithEmojis(ScoreBoard scoreBoard) {
        StringBuilder sb = new StringBuilder();
        sb.append("🏆 LEADERBOARD 🏆\n");
        sb.append("═══════════════════════════\n");
        sb.append(String.format("Question %d of %d\n\n", 
                  scoreBoard.getCurrentQuestion(), scoreBoard.getTotalQuestions()));
        
        String[] medals = {"🥇", "🥈", "🥉", "🏅"};
        int position = 1;
        
        for (var team : scoreBoard.getTeams()) {
            String medal = position <= medals.length ? medals[position - 1] : "🔸";
            sb.append(String.format("%s %d. Team %s - %d points\n", 
                      medal, position, team.getTeamCode(), team.getScore()));
            
            // Show individual player scores with indentation
            for (var player : team.getPlayers()) {
                sb.append(String.format("   👤 %s: %d pts\n", 
                          player.getUsername(), player.getScore()));
            }
            sb.append("\n");
            position++;
        }
        
        return sb.toString();
    }
    
    private void animateScoreboardUpdate() {
        // Simple highlight animation for scoreboard
        Color originalBg = scoreArea.getBackground();
        scoreArea.setBackground(KAHOOT_YELLOW.brighter());
        
        Timer highlightTimer = new Timer(300, e -> {
            scoreArea.setBackground(originalBg);
        });
        highlightTimer.setRepeats(false);
        highlightTimer.start();
    }
    
    public void showGameEnd(ScoreBoard finalScores) {
        SwingUtilities.invokeLater(() -> {
            canAnswer = false;
            
            // Stop all timers
            if (pulseTimer.isRunning()) {
                pulseTimer.stop();
            }
            
            // Disable all buttons with fade effect
            for (JButton button : answerButtons) {
                button.setEnabled(false);
                button.setBackground(Color.LIGHT_GRAY);
            }
            
            // Update UI for game end
            questionLabel.setText("<html><div style='text-align: center; color: #FF6B35;'>" +
                                "🎉 GAME OVER! 🎉</div></html>");
            statusLabel.setText("🏁 Final Results - Thanks for playing!");
            timerLabel.setText("END");
            timerLabel.setForeground(KAHOOT_BLUE);
            timerProgressBar.setValue(0);
            
            displayScoreboard(finalScores);
            
            // Show celebration dialog
            showCelebrationDialog(finalScores);
        });
    }
    
    private void showCelebrationDialog(ScoreBoard finalScores) {
        String winnerMessage;
        if (!finalScores.getTeams().isEmpty()) {
            var winningTeam = finalScores.getWinningTeam();
            winnerMessage = String.format("🏆 Congratulations to Team %s! 🏆\n" +
                                        "Final Score: %d points\n\n" +
                                        "Thanks for playing IsKahoot!",
                                        winningTeam.getTeamCode(), 
                                        winningTeam.getScore());
        } else {
            winnerMessage = "Thanks for playing IsKahoot!\nBetter luck next time!";
        }
        
        // Custom dialog with better styling
        JDialog gameEndDialog = new JDialog(this, "Game Finished!", true);
        gameEndDialog.setLayout(new BorderLayout());
        gameEndDialog.setSize(400, 200);
        gameEndDialog.setLocationRelativeTo(this);
        
        JLabel messageLabel = new JLabel("<html><div style='text-align: center; padding: 20px;'>" +
                                       winnerMessage.replace("\n", "<br>") + "</div></html>");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.setBackground(KAHOOT_BLUE);
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.addActionListener(e -> gameEndDialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(okButton);
        
        gameEndDialog.add(messageLabel, BorderLayout.CENTER);
        gameEndDialog.add(buttonPanel, BorderLayout.SOUTH);
        gameEndDialog.setVisible(true);
    }
    
    public void handleServerMessage(Object message) {
        // This will be expanded to handle different message types
        if (message instanceof Question) {
            displayQuestion((Question) message);
        } else if (message instanceof ScoreBoard) {
            displayScoreboard((ScoreBoard) message);
        } else if (message instanceof Integer) {
            updateTimer((Integer) message);
        } else if (message instanceof String) {
            String msg = (String) message;
            if (msg.equals("GAME_END")) {
                // Handle game end
            }
        }
    }
}