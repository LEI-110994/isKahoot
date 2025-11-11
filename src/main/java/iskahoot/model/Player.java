package iskahoot.model;

import java.io.Serializable;

/**
 * Represents a player in the IsKahoot game
 */
public class Player implements Serializable {
    private final String username;
    private final String teamCode;
    private int score;
    private int questionsAnswered;
    private int correctAnswers;
    
    public Player(String username, String teamCode) {
        this.username = username;
        this.teamCode = teamCode;
        this.score = 0;
        this.questionsAnswered = 0;
        this.correctAnswers = 0;
    }
    
    public void addScore(int points) {
        this.score += points;
        this.correctAnswers++;
    }
    
    public void incrementQuestionsAnswered() {
        this.questionsAnswered++;
    }
    
    // Getters and setters
    public String getUsername() {
        return username;
    }
    
    public String getTeamCode() {
        return teamCode;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public int getQuestionsAnswered() {
        return questionsAnswered;
    }
    
    public int getCorrectAnswers() {
        return correctAnswers;
    }
    
    public double getAccuracy() {
        if (questionsAnswered == 0) {
            return 0.0;
        }
        return (double) correctAnswers / questionsAnswered * 100.0;
    }
    
    @Override
    public String toString() {
        return String.format("%s (Team: %s) - Score: %d, Accuracy: %.1f%%", 
                           username, teamCode, score, getAccuracy());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Player player = (Player) obj;
        return username.equals(player.username);
    }
    
    @Override
    public int hashCode() {
        return username.hashCode();
    }
}