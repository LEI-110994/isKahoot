package iskahoot.net;

public class JoinResponse extends Message {
    private final boolean success;
    private final String message;

    public JoinResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
