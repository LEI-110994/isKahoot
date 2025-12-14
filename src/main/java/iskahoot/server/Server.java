package iskahoot.server;

import iskahoot.util.QuestionLoader;
import iskahoot.model.Question;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 12345;
    private final Map<String, GameState> activeGames = new ConcurrentHashMap<>();
    private final ExecutorService clientExecutor = Executors.newCachedThreadPool();
    private volatile boolean running = true;
    private List<Question> availableQuestions;
    private final ExecutorService gameExecutor = Executors.newFixedThreadPool(5);

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        try {
            availableQuestions = QuestionLoader.loadQuestionsFromFile("resources/questions.json");
            System.out.println("Loaded " + availableQuestions.size() + " questions.");
        } catch (IOException e) {
            System.err.println("Error loading questions: " + e.getMessage());
            return;
        }

        new Thread(this::listenForConnections).start();
        System.out.println("Server started on port " + PORT);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Commands: new <playersPerTeam> <numQuestions>, start <gameCode>, games, exit");

        while (running) {
            System.out.print("> ");
            if (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                processCommand(line);
            }
        }
    }

    private void processCommand(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length == 0)
            return;

        switch (parts[0].toLowerCase()) {
            case "new":
                if (parts.length != 4) {
                    System.out.println("Usage: new <numTeams> <playersPerTeam> <numQuestions>");
                } else {
                    try {
                        int numTeams = Integer.parseInt(parts[1]);
                        int playersPerTeam = Integer.parseInt(parts[2]);
                        int numQuestions = Integer.parseInt(parts[3]);
                        createNewGame(numTeams, playersPerTeam, numQuestions);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid numbers.");
                    }
                }
                break;
            case "start":
                if (parts.length != 2) {
                    System.out.println("Usage: start <gameCode>");
                } else {
                    startGame(parts[1]);
                }
                break;
            case "games":
                listGames();
                break;
            case "exit":
                running = false;
                System.exit(0);
                break;
            default:
                System.out.println("Unknown command.");
        }
    }

    private void startGame(String gameCode) {
        GameState game = activeGames.get(gameCode);
        if (game == null) {
            System.out.println("Game not found.");
            return;
        }
        if (game.isGameStarted()) {
            System.out.println("Game already started.");
            return;
        }
        gameExecutor.submit(game);
        System.out.println("Game " + gameCode + " queued/started.");
    }

    private void createNewGame(int numTeams, int playersPerTeam, int numQuestions) {
        String gameCode = generateGameCode();
        GameState game = new GameState(gameCode, numTeams, playersPerTeam, numQuestions);
        game.setQuestions(availableQuestions);
        activeGames.put(gameCode, game);
        System.out.println("Created game " + gameCode + " for " + numTeams + " teams with " + playersPerTeam
                + " players per team and " + numQuestions + " questions.");
    }

    private String generateGameCode() {
        return String.valueOf((int) (Math.random() * 9000) + 1000);
    }

    private void listGames() {
        if (activeGames.isEmpty()) {
            System.out.println("No active games.");
        } else {
            for (GameState game : activeGames.values()) {
                System.out.printf("Game %s: %d players, %s\n",
                        game.getGameCode(),
                        game.getPlayers().size(),
                        game.isGameStarted() ? "Running" : "Waiting");
            }
        }
    }

    private void listenForConnections() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (running) {
                Socket socket = serverSocket.accept();
                DealWithClient clientTask = new DealWithClient(socket, this);
                clientExecutor.submit(clientTask);
            }
        } catch (IOException e) {
            if (running) {
                e.printStackTrace();
            }
        }
    }

    public GameState getGame(String gameCode) {
        return activeGames.get(gameCode);
    }
}
