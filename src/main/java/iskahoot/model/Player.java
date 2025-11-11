package iskahoot.model;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a player in the IsKahoot game
 */
public class Player implements Serializable {
    private final String username;
    private final String teamCode;
    private final AtomicInteger score;
    private final AtomicInteger questionsAnswered;
    private final AtomicInteger correctAnswers;
    
    public Player(String username, String teamCode) {
        this.username = username;
        this.teamCode = teamCode;
        this.score = new AtomicInteger(0);
        this.questionsAnswered = new AtomicInteger(0);
        this.correctAnswers = new AtomicInteger(0);
    }
    
    public void addScore(int points) {
        this.score.addAndGet(points);
        this.correctAnswers.incrementAndGet();
    }
    
    public void incrementQuestionsAnswered() {
        this.questionsAnswered.incrementAndGet();
    }
    
    // Getters and setters
    public String getUsername() {
        return username;
    }
    
    public String getTeamCode() {
        return teamCode;
    }
    
    public int getScore() {
        return score.get();
    }
    
    public void setScore(int score) {
        this.score.set(score);
    }
    
    public int getQuestionsAnswered() {
        return questionsAnswered.get();
    }
    
    public int getCorrectAnswers() {
        return correctAnswers.get();
    }
    
    public double getAccuracy() {
        if (questionsAnswered.get() == 0) {
            return 0.0;
        }
        return (double) correctAnswers.get() / questionsAnswered.get() * 100.0;
    }
    
    @Override
    public String toString() {
        return String.format("%s (Team: %s) - Score: %d, Accuracy: %.1f%%", 
                           username, teamCode, score.get(), getAccuracy());
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