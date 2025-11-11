package iskahoot.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import iskahoot.model.Question;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading questions from JSON files using Gson
 */
public class QuestionLoader {
    private static final Gson gson = new Gson();
    
    /**
     * Load questions from a JSON file
     * @param filePath Path to the JSON file
     * @return List of Question objects
     * @throws IOException if file cannot be read
     */
    public static List<Question> loadQuestionsFromFile(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            return loadQuestionsFromReader(reader);
        }
    }
    
    /**
     * Load questions from a Reader (useful for resources)
     * @param reader Reader containing JSON data
     * @return List of Question objects
     * @throws IOException if data cannot be read
     */
    public static List<Question> loadQuestionsFromReader(Reader reader) throws IOException {
        JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        return parseQuestions(jsonObject);
    }
    
    /**
     * Load questions from JSON string
     * @param jsonString JSON string containing questions
     * @return List of Question objects
     */
    public static List<Question> loadQuestionsFromString(String jsonString) {
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        return parseQuestions(jsonObject);
    }
    
    private static List<Question> parseQuestions(JsonObject jsonObject) {
        List<Question> questions = new ArrayList<>();
        
        // Handle both "questions" array directly or nested in "quizzes"
        JsonArray questionsArray;
        if (jsonObject.has("questions")) {
            questionsArray = jsonObject.getAsJsonArray("questions");
        } else if (jsonObject.has("quizzes")) {
            // Handle the format with quizzes array
            JsonArray quizzesArray = jsonObject.getAsJsonArray("quizzes");
            if (quizzesArray.size() > 0) {
                JsonObject firstQuiz = quizzesArray.get(0).getAsJsonObject();
                questionsArray = firstQuiz.getAsJsonArray("questions");
            } else {
                return questions; // Empty list
            }
        } else {
            throw new IllegalArgumentException("JSON must contain 'questions' or 'quizzes' array");
        }
        
        // Parse each question
        for (int i = 0; i < questionsArray.size(); i++) {
            JsonObject questionObj = questionsArray.get(i).getAsJsonObject();
            Question question = parseQuestion(questionObj);
            questions.add(question);
        }
        
        return questions;
    }
    
    private static Question parseQuestion(JsonObject questionObj) {
        String questionText = questionObj.get("question").getAsString();
        int points = questionObj.get("points").getAsInt();
        int correct = questionObj.get("correct").getAsInt();
        
        JsonArray optionsArray = questionObj.getAsJsonArray("options");
        String[] options = new String[optionsArray.size()];
        for (int i = 0; i < optionsArray.size(); i++) {
            options[i] = optionsArray.get(i).getAsString();
        }
        
        return new Question(questionText, points, correct, options);
    }
    
    /**
     * Save questions to a JSON file
     * @param questions List of questions to save
     * @param filePath Path where to save the file
     * @throws IOException if file cannot be written
     */
    public static void saveQuestionsToFile(List<Question> questions, String filePath) throws IOException {
        JsonObject root = new JsonObject();
        JsonArray questionsArray = new JsonArray();
        
        for (Question question : questions) {
            JsonObject questionObj = new JsonObject();
            questionObj.addProperty("question", question.getQuestion());
            questionObj.addProperty("points", question.getPoints());
            questionObj.addProperty("correct", question.getCorrect());
            
            JsonArray optionsArray = new JsonArray();
            for (String option : question.getOptions()) {
                optionsArray.add(option);
            }
            questionObj.add("options", optionsArray);
            
            questionsArray.add(questionObj);
        }
        
        root.add("questions", questionsArray);
        
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(root, writer);
        }
    }
    
    /**
     * Create a sample questions file for testing
     * @param filePath Path where to create the sample file
     * @throws IOException if file cannot be created
     */
    public static void createSampleQuestionsFile(String filePath) throws IOException {
        List<Question> sampleQuestions = createSampleQuestions();
        saveQuestionsToFile(sampleQuestions, filePath);
    }
    
    private static List<Question> createSampleQuestions() {
        List<Question> questions = new ArrayList<>();
        
        questions.add(new Question(
            "O que é uma thread?",
            5,
            3,
            new String[]{"Processo", "Aplicação", "Programa", "Processo Ligeiro"}
        ));
        
        questions.add(new Question(
            "Qual destas operações é um método bloqueante?",
            3,
            1,
            new String[]{"join()", "sleep(<millis>)", "interrupted()", "wait()"}
        ));
        
        questions.add(new Question(
            "Qual é o principal problema da concorrência?",
            4,
            2,
            new String[]{"Performance", "Race Conditions", "Memória", "CPU"}
        ));
        
        questions.add(new Question(
            "O que é um semáforo?",
            3,
            0,
            new String[]{"Estrutura de sincronização", "Tipo de thread", "Algoritmo", "Protocolo"}
        ));
        
        questions.add(new Question(
            "Qual método é usado para sincronizar threads em Java?",
            2,
            1,
            new String[]{"sync()", "synchronized", "lock()", "wait()"}
        ));
        
        return questions;
    }
}