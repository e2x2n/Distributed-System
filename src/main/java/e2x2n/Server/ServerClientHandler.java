package e2x2n.Server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private static Map<String, ServerClientHandler> serverClientHandlers = new HashMap<>();

    public ServerClientHandler(Socket socket) {
        this.socket = socket;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        try {
            while (true) {
                out.println("Enter your username: ");
                username = in.readLine();
                synchronized (serverClientHandlers) {
                    if (!serverClientHandlers.containsKey(username)) {
                        serverClientHandlers.put(username, this);
                        out.println("Welcome, " + username);
                        break;
                    } else {
                        out.println("Username already taken. Please choose another one.");
                    }
                }
            }
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("/msg ")) {
                    String[] parts = message.split(" ", 3);
                    String recipient = parts[1];
                    String privateMessage = username + ": " + parts[2];
                    sendPrivateMessage(recipient, privateMessage);
                } else {
                    ChatServer.broadcastMessage(username + ": " + message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                synchronized (serverClientHandlers) {
                    serverClientHandlers.remove(username);
                }
                System.out.println("Client disconnected");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendPrivateMessage(String recipient, String message) {
        synchronized (serverClientHandlers) {
            ServerClientHandler recipientHandler = serverClientHandlers.get(recipient);
            if (recipientHandler != null) {
                recipientHandler.sendMessage("[Private] " + message);
            } else {
                sendMessage("User " + recipient + " not found");
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
