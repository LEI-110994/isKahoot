package iskahoot.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a team in the IsKahoot game
 */
public class Team implements Serializable {
    private final String teamCode;
    private List<Player> players;
    private int score;
    
    public Team(String teamCode) {
        this.teamCode = teamCode;
        this.players = new ArrayList<>();
        this.score = 0;
    }
    
    public boolean addPlayer(Player player) {
        if (!players.contains(player)) {
            players.add(player);
            return true;
        }
        return false;
    }
    
    public void addScore(int points) {
        this.score += points;
    }
    
    public int getPlayerCount() {
        return players.size();
    }
    
    public boolean isFull(int maxPlayersPerTeam) {
        return players.size() >= maxPlayersPerTeam;
    }
    
    public int getTotalIndividualScore() {
        return players.stream().mapToInt(Player::getScore).sum();
    }
    
    // Getters and setters
    public String getTeamCode() {
        return teamCode;
    }
    
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Team ").append(teamCode).append(" - Score: ").append(score).append("\n");
        for (Player player : players) {
            sb.append("  ").append(player.toString()).append("\n");
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Team team = (Team) obj;
        return teamCode.equals(team.teamCode);
    }
    
    @Override
    public int hashCode() {
        return teamCode.hashCode();
    }
}