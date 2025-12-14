package iskahoot.net;

import iskahoot.model.ScoreBoard;

public class ScoreBoardMessage extends Message {
    private final ScoreBoard scoreBoard;
    private final boolean isFinal;

    public ScoreBoardMessage(ScoreBoard scoreBoard, boolean isFinal) {
        this.scoreBoard = scoreBoard;
        this.isFinal = isFinal;
    }

    public ScoreBoard getScoreBoard() { return scoreBoard; }
    public boolean isFinal() { return isFinal; }
}
