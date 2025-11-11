package iskahoot.client;

import iskahoot.client.gui.GameGUI;
import java.io.*;
import java.net.*;

/**
 * Main client class for IsKahoot game
 * Usage: java KahootClient {IP PORT Game Team Username}
 */
public class KahootClient implements ClientInterface {
    private String serverIP;
    private int serverPort;
    private String gameCode;
    private String teamCode;
    private String username;
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private GameGUI gui;
    
    public KahootClient(String serverIP, int serverPort, String gameCode, String teamCode, String username) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.gameCode = gameCode;
        this.teamCode = teamCode;
        this.username = username;
    }
    
    public void start() {
        try {
            // Connect to server
            socket = new Socket(serverIP, serverPort);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            // Send connection info
            sendConnectionInfo();
            
            // Initialize GUI
            gui = new GameGUI(this);
            gui.setVisible(true);
            
            // Start listening for server messages
            listenForMessages();
            
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }
    
    private void sendConnectionInfo() throws IOException {
        out.writeObject("CONNECT");
        out.writeObject(gameCode);
        out.writeObject(teamCode);
        out.writeObject(username);
        out.flush();
    }
    
    private void listenForMessages() {
        new Thread(() -> {
            try {
                while (true) {
                    Object message = in.readObject();
                    handleServerMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Connection lost: " + e.getMessage());
            }
        }).start();
    }
    
    private void handleServerMessage(Object message) {
        // Handle different types of messages from server
        if (gui != null) {
            gui.handleServerMessage(message);
        }
    }
    
    public void sendAnswer(int answerIndex) {
        try {
            out.writeObject("ANSWER");
            out.writeObject(answerIndex);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending answer: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("Usage: java KahootClient {IP PORT Game Team Username}");
            return;
        }
        
        String ip = args[0];
        int port = Integer.parseInt(args[1]);
        String game = args[2];
        String team = args[3];
        String username = args[4];
        
        KahootClient client = new KahootClient(ip, port, game, team, username);
        client.start();
    }
}