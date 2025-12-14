package iskahoot.model;

import java.io.Serializable;

public class Question implements Serializable {
    private String question;
    private int points;
    private int correct;
    private String[] options;

    public Question() {
        // Construtor vazio para o Gson ler o JSON
    }

    public Question(String question, int points, int correct, String[] options) {
        this.question = question;
        this.points = points;
        this.correct = correct;
        this.options = options;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getCorrect() {
        return correct;
    }

    public void setCorrect(int correct) {
        this.correct = correct;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public boolean isCorrectAnswer(int answerIndex) {
        return answerIndex == correct;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Question: ").append(question).append("\n");
        sb.append("Points: ").append(points).append("\n");
        sb.append("Options:\n");
        for (int i = 0; i < options.length; i++) {
            sb.append("  ").append(i).append(": ").append(options[i]);
            if (i == correct) {
                sb.append(" (CORRECT)");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}