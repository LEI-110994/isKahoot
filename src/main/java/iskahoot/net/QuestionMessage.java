package iskahoot.net;

import iskahoot.model.Question;

public class QuestionMessage extends Message {
    private final Question question;
    private final int questionIndex;
    private final int totalQuestions;

    public QuestionMessage(Question question, int questionIndex, int totalQuestions) {
        this.question = question;
        this.questionIndex = questionIndex;
        this.totalQuestions = totalQuestions;
    }

    public Question getQuestion() { return question; }
    public int getQuestionIndex() { return questionIndex; }
    public int getTotalQuestions() { return totalQuestions; }
}
