package iskahoot.net;

public class JoinRequest extends Message {
    private final String username;
    private final String teamName;
    private final String gameCode;

    public JoinRequest(String username, String teamName, String gameCode) {
        this.username = username;
        this.teamName = teamName;
        this.gameCode = gameCode;
    }

    public String getUsername() { return username; }
    public String getTeamName() { return teamName; }
    public String getGameCode() { return gameCode; }
}
