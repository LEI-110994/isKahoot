package iskahoot.client;

import iskahoot.client.gui.GameGUI;
import iskahoot.net.*;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private final String serverIp;
    private final int serverPort;
    private final String gameCode;
    private final String teamName;
    private final String username;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private GameGUI gui;
    private boolean running = true;

    public Client(String serverIp, int serverPort, String gameCode, String teamName, String username) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.gameCode = gameCode;
        this.teamName = teamName;
        this.username = username;
    }

    public void start() {
        try {
            socket = new Socket(serverIp, serverPort);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Send Join Request
            out.writeObject(new JoinRequest(username, teamName, gameCode));
            out.flush();

            // Wait for response
            Object response = in.readObject();
            if (response instanceof JoinResponse) {
                JoinResponse jr = (JoinResponse) response;
                if (!jr.isSuccess()) {
                    System.err.println("Failed to join: " + jr.getMessage());
                    JOptionPane.showMessageDialog(null, "Failed to join: " + jr.getMessage());
                    return;
                }
            } else {
                System.err.println("Unexpected response from server.");
                return;
            }

            // Init GUI
            SwingUtilities.invokeAndWait(() -> {
                gui = new GameGUI(username);
                gui.setVisible(true);
                gui.setOnAnswerSelected(this::sendAnswer);
            });

            // Message Loop
            while (running) {
                Object msg = in.readObject();
                handleMessage(msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (gui != null) {
                JOptionPane.showMessageDialog(gui, "Connection lost: " + e.getMessage());
            }
        } finally {
            try { if (socket != null) socket.close(); } catch (IOException e) {}
        }
    }

    private void handleMessage(Object msg) {
        if (msg instanceof GameStartMessage) {
            // Game started
            System.out.println("Game Started!");
        } else if (msg instanceof QuestionMessage) {
            QuestionMessage qm = (QuestionMessage) msg;
            gui.displayQuestion(qm.getQuestion());
            startTimer(30); // 30s timer
        } else if (msg instanceof ScoreBoardMessage) {
            ScoreBoardMessage sbm = (ScoreBoardMessage) msg;
            gui.displayScoreboard(sbm.getScoreBoard());
            if (sbm.isFinal()) {
                gui.showGameEnd(sbm.getScoreBoard());
                running = false;
            } else {
                // Round end, show feedback? 
                // We don't have exact correctness info here unless we track it or server sends it.
                // The ScoreBoard has updated scores.
                // GUI can infer logic or just show scoreboard.
                gui.showAnswerFeedback(false, -1); // Reset or show generic
            }
        }
    }

    private Timer timer;
    private void startTimer(int seconds) {
        if (timer != null) timer.stop();
        final int[] timeLeft = {seconds};
        timer = new Timer(1000, e -> {
            if (gui != null) gui.updateTimer(timeLeft[0]);
            if (timeLeft[0] > 0) {
                timeLeft[0]--;
            } else {
                ((Timer)e.getSource()).stop();
            }
        });
        timer.start();
    }

    private void sendAnswer(int index) {
        try {
            out.writeObject(new AnswerMessage(index));
            out.flush();
            out.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Usage: java iskahoot.client.Client IP PORT Jogo Equipa Username
        if (args.length != 5) {
            System.out.println("Usage: java iskahoot.client.Client <IP> <PORT> <GameCode> <TeamName> <Username>");
            // For testing purposes, we can default or prompt
            // return;
        }

        String ip = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 12345;
        String game = args.length > 2 ? args[2] : "1000";
        String team = args.length > 3 ? args[3] : "TeamA";
        String user = args.length > 4 ? args[4] : "User" + System.currentTimeMillis() % 1000;

        new Client(ip, port, game, team, user).start();
    }
}
