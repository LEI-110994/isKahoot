package iskahoot.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the scoreboard for the IsKahoot game
 */
public class ScoreBoard implements Serializable {
    private final List<Player> players;
    private final int currentQuestion;
    private final int totalQuestions;
    private final long timestamp;
    
    public ScoreBoard(List<Player> players, int currentQuestion, int totalQuestions) {
        this.players = new ArrayList<>(players);
        this.currentQuestion = currentQuestion;
        this.totalQuestions = totalQuestions;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getFormattedScores() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SCOREBOARD ===\n");
        sb.append(String.format("Question %d of %d\n\n", currentQuestion, totalQuestions));
        
        int position = 1;
        for (Player player : players) {
            sb.append(String.format("%d. %s - %d points\n", 
                                  position++, player.getUsername(), player.getScore()));
        }
        
        return sb.toString();
    }
    
    public String getCompactScores() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Q%d/%d | ", currentQuestion, totalQuestions));
        
        int position = 1;
        for (Player player : players) {
            if (position > 1) sb.append(" | ");
            sb.append(String.format("%d.%s(%d)", position++, player.getUsername(), player.getScore()));
        }
        
        return sb.toString();
    }
    
    // Getters
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }
    
    public int getCurrentQuestion() {
        return currentQuestion;
    }
    
    public int getTotalQuestions() {
        return totalQuestions;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public Player getWinningPlayer() {
        if (players.isEmpty()) {
            return null;
        }
        // Assuming players are sorted by score in descending order
        return players.get(0);
    }
}