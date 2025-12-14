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

public class QuestionLoader {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static class QuestionsWrapper {
        List<Question> questions;
    }

    // Carregar perguntas de um JSON file
    public static List<Question> loadQuestionsFromFile(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            return loadQuestionsFromReader(reader);
        }
    }

    // Carregar perguntas de um Reader
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

        QuestionsWrapper wrapper = gson.fromJson(json, QuestionsWrapper.class);
        if (wrapper != null && wrapper.questions != null) {
            return wrapper.questions;
        }

        return new ArrayList<>();
    }

    // Carregar perguntas de uma string JSON
    public static List<Question> loadQuestionsFromString(String jsonString) {
        try (Reader reader = new java.io.StringReader(jsonString)) {
            return loadQuestionsFromReader(reader);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    // Salvar perguntas para um arquivo JSON
    public static void saveQuestionsToFile(List<Question> questions, String filePath) throws IOException {
        QuestionsWrapper wrapper = new QuestionsWrapper();
        wrapper.questions = questions;

        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(wrapper, writer);
        }
    }
}