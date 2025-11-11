package iskahoot.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the scoreboard for the IsKahoot game
 */
public class ScoreBoard implements Serializable {
    private final List<Team> teams;
    private final int currentQuestion;
    private final int totalQuestions;
    private final long timestamp;
    
    public ScoreBoard(List<Team> teams, int currentQuestion, int totalQuestions) {
        this.teams = new ArrayList<>(teams);
        this.currentQuestion = currentQuestion;
        this.totalQuestions = totalQuestions;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getFormattedScores() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SCOREBOARD ===\n");
        sb.append(String.format("Question %d of %d\n\n", currentQuestion, totalQuestions));
        
        int position = 1;
        for (Team team : teams) {
            sb.append(String.format("%d. Team %s - %d points\n", 
                                  position++, team.getTeamCode(), team.getScore()));
            
            // Show individual player scores
            for (Player player : team.getPlayers()) {
                sb.append(String.format("   %s: %d points\n", 
                                      player.getUsername(), player.getScore()));
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    public String getCompactScores() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Q%d/%d | ", currentQuestion, totalQuestions));
        
        int position = 1;
        for (Team team : teams) {
            if (position > 1) sb.append(" | ");
            sb.append(String.format("%d.%s(%d)", position++, team.getTeamCode(), team.getScore()));
        }
        
        return sb.toString();
    }
    
    // Getters
    public List<Team> getTeams() {
        return new ArrayList<>(teams);
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
    
    public Team getWinningTeam() {
        if (teams.isEmpty()) {
            return null;
        }
        // Assuming teams are sorted by score in descending order
        return teams.get(0);
    }
}