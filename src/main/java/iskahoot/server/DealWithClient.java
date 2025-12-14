package iskahoot.server;

import iskahoot.net.*;
import iskahoot.model.Player;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class DealWithClient implements Runnable {
    private final Socket socket;
    private final Server server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private GameState game;
    private Player player;
    private boolean running = true;

    public DealWithClient(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            Object msg = in.readObject();
            if (msg instanceof JoinRequest) {
                handleJoin((JoinRequest) msg);
            } else {
                send(new JoinResponse(false, "Invalid initial message."));
                close();
                return;
            }

            while (running && !socket.isClosed()) {
                try {
                    Object obj = in.readObject();
                    if (obj instanceof AnswerMessage) {
                        handleAnswer((AnswerMessage) obj);
                    }
                } catch (IOException e) {
                    running = false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    private void handleJoin(JoinRequest req) throws IOException {
        GameState g = server.getGame(req.getGameCode());
        if (g == null) {
            send(new JoinResponse(false, "Game not found."));
            return;
        }

        if (g.isGameStarted()) {
            send(new JoinResponse(false, "Game already started."));
            return;
        }

        boolean added = g.addPlayer(req.getUsername(), req.getTeamName(), this);
        if (added) {
            this.game = g;
            this.player = new Player(req.getUsername());
            send(new JoinResponse(true, "Joined game " + req.getGameCode()));
            System.out.println("Player " + req.getUsername() + " joined game " + req.getGameCode());
        } else {
            send(new JoinResponse(false, "Could not join game (Duplicate name?)."));
        }
    }

    private void handleAnswer(AnswerMessage msg) {
        if (game != null) {
            game.submitAnswer(player.getUsername(), msg.getAnswerIndex());
        }
    }

    public synchronized void send(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset();
        } catch (IOException e) {
            running = false;
        }
    }

    private void close() {
        running = false;
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
        }
    }
}
