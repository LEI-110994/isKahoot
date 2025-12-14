package iskahoot.net;

public class AnswerMessage extends Message {
    private final int answerIndex;

    public AnswerMessage(int answerIndex) {
        this.answerIndex = answerIndex;
    }

    public int getAnswerIndex() { return answerIndex; }
}
