package iskahoot.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import iskahoot.model.Question;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading and saving questions from/to JSON files using Gson.
 */
public class QuestionLoader {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Private wrapper class to match the JSON structure {"questions": [...]}
    private static class QuestionsWrapper {
        List<Question> questions;
    }

    /**
     * Load questions from a JSON file.
     * @param filePath Path to the JSON file.
     * @return List of Question objects.
     * @throws IOException if file cannot be read.
     */
    public static List<Question> loadQuestionsFromFile(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            return loadQuestionsFromReader(reader);
        }
    }

    /**
     * Load questions from a Reader (useful for resources).
     * @param reader Reader containing JSON data.
     * @return List of Question objects.
     */
    public static List<Question> loadQuestionsFromReader(Reader reader) {
        QuestionsWrapper wrapper = gson.fromJson(reader, QuestionsWrapper.class);
        if (wrapper == null || wrapper.questions == null) {
            return new ArrayList<>();
        }
        return wrapper.questions;
    }

    /**
     * Load questions from a JSON string.
     * @param jsonString JSON string containing questions.
     * @return List of Question objects.
     */
    public static List<Question> loadQuestionsFromString(String jsonString) {
        QuestionsWrapper wrapper = gson.fromJson(jsonString, QuestionsWrapper.class);
        if (wrapper == null || wrapper.questions == null) {
            return new ArrayList<>();
        }
        return wrapper.questions;
    }

    /**
     * Save questions to a JSON file.
     * @param questions List of questions to save.
     * @param filePath Path where to save the file.
     * @throws IOException if file cannot be written.
     */
    public static void saveQuestionsToFile(List<Question> questions, String filePath) throws IOException {
        QuestionsWrapper wrapper = new QuestionsWrapper();
        wrapper.questions = questions;

        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(wrapper, writer);
        }
    }
}