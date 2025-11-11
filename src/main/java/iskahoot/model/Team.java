package iskahoot.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a team in the IsKahoot game
 */
public class Team implements Serializable {
    private final String teamCode;
    private final CopyOnWriteArrayList<Player> players;
    
    public Team(String teamCode) {
        this.teamCode = teamCode;
        this.players = new CopyOnWriteArrayList<>();
    }
    
    public boolean addPlayer(Player player) {
        return players.addIfAbsent(player);
    }
    
    public int getPlayerCount() {
        return players.size();
    }
    
    public boolean isFull(int maxPlayersPerTeam) {
        return players.size() >= maxPlayersPerTeam;
    }
    
    public int getScore() {
        return players.stream().mapToInt(Player::getScore).sum();
    }
    
    // Getters and setters
    public String getTeamCode() {
        return teamCode;
    }
    
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Team ").append(teamCode).append(" - Score: ").append(getScore()).append("\n");
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