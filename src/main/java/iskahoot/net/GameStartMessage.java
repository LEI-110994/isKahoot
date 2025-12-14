package iskahoot.net;

public class GameStartMessage extends Message {
    private final int totalQuestions;

    public GameStartMessage(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getTotalQuestions() { return totalQuestions; }
}
