package iskahoot.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import iskahoot.model.Question;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading and saving questions from/to JSON files using Gson.
 */
public class QuestionLoader {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Wrapper classes to match possible JSON structures
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
     * It tries to parse two possible formats.
     * @param reader Reader containing JSON data.
     * @return List of Question objects.
     */
    public static List<Question> loadQuestionsFromReader(Reader reader) {
        StringBuilder sb = new StringBuilder();
        try {
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read from reader", e);
        }
        String json = sb.toString();

        // Parse as {"questions": [...]}
        QuestionsWrapper wrapper = gson.fromJson(json, QuestionsWrapper.class);
        if (wrapper != null && wrapper.questions != null) {
            return wrapper.questions;
        }

        return new ArrayList<>();
    }

    /**
     * Load questions from a JSON string.
     * @param jsonString JSON string containing questions.
     * @return List of Question objects.
     */
    public static List<Question> loadQuestionsFromString(String jsonString) {
        try (Reader reader = new java.io.StringReader(jsonString)) {
            return loadQuestionsFromReader(reader);
        } catch (IOException e) {
            // Should not happen with StringReader
            return new ArrayList<>();
        }
    }

    /**
     * Save questions to a JSON file in the simple {"questions": [...]} format.
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