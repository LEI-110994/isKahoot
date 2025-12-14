package iskahoot.model;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class ScoreBoard implements Serializable {
    private final List<Player> players;
    private final int currentQuestion;
    private final int totalQuestions;
    private final long timestamp;

    private final Map<String, Integer> teamScores;
    private final Map<String, List<Player>> teamMembers;

    public ScoreBoard(List<Player> players, int currentQuestion, int totalQuestions) {
        this.players = new ArrayList<>(players);
        this.currentQuestion = currentQuestion;
        this.totalQuestions = totalQuestions;
        this.timestamp = System.currentTimeMillis();

        this.teamScores = new HashMap<>();
        this.teamMembers = new HashMap<>();
        calculateTeamScores();
    }

    private void calculateTeamScores() {
        for (Player p : players) {
            String team = p.getTeamName();
            if (team == null || team.isEmpty())
                team = "No Team";

            teamScores.put(team, teamScores.getOrDefault(team, 0) + p.getScore());
            teamMembers.computeIfAbsent(team, k -> new ArrayList<>()).add(p);
        }
    }

    public String getFormattedScores() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SCOREBOARD ===\n");
        sb.append(String.format("Question %d of %d\n\n", currentQuestion, totalQuestions));

        List<Map.Entry<String, Integer>> sortedTeams = new ArrayList<>(teamScores.entrySet());
        sortedTeams.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        int position = 1;
        for (Map.Entry<String, Integer> entry : sortedTeams) {
            String teamName = entry.getKey();
            int score = entry.getValue();

            sb.append(String.format("%d. %s - %d points\n", position++, teamName, score));

            List<Player> members = teamMembers.get(teamName);
            members.sort((p1, p2) -> p2.getScore() - p1.getScore()); // Sort members by score
            for (Player p : members) {
                sb.append(String.format("    - %s (%d)\n", p.getUsername(), p.getScore()));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public String getCompactScores() {
        return getFormattedScores();
    }

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
        if (players.isEmpty())
            return null;
        players.sort((p1, p2) -> p2.getScore() - p1.getScore());
        return players.get(0);
    }

    public String getWinningTeam() {
        if (teamScores.isEmpty())
            return "No Teams";
        return teamScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey() + " (" + entry.getValue() + " pts)")
                .orElse("Unknown");
    }
}