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
                String[] parts = message.split(" ", 3);
                String command = parts[0];
                switch (command) {
                    case "/msg":
                        if (parts.length == 3){
                            sendPrivateMessage(parts[1], "[private] " + username + ": " + parts[2]);
                        } else {
                            sendMessage("Invalid private message format. Usage: /msg <recipient> <message>");
                        }
                        break;
                    case "/create":
                        if (parts.length == 2) {
                            String groupName = parts[1];
                            ChatServer.createGroup(groupName);
                            ChatServer.joinGroup(groupName, this);
                            sendMessage("Group " + groupName + " created. You have joined the group.");
                        } else {
                            sendMessage("Invalid group creation format. Usage: /create <group_name>");
                        }
                        break;
                    case "/join":
                        if (parts.length == 2) {
                            String groupName = parts[1];
                            ChatServer.joinGroup(groupName, this);
                            sendMessage("You have joined group " + groupName);
                        } else {
                            sendMessage("Invalid group join format. Usage: /join <group_name>");
                        }
                        break;
                    case "/leave":
                        if (parts.length == 2) {
                            String groupName = parts[1];
                            ChatServer.leaveGroup(groupName, this);
                            sendMessage("You have left group " + groupName);
                        } else {
                            sendMessage("Invalid group leave format. Usage: /leave <group_name>");
                        }
                        break;
                    case "/group":
                        if (parts.length == 3) {
                            String groupName = parts[1];
                            String groupMessage = parts[2];
                            ChatServer.broadcastGroupMessage(groupName, "[" + groupName + "] " +
                                    username + ": " + groupMessage, this);
                        } else {
                            sendMessage("Invalid group message format. Usage: /group <group_name> <message>");
                        }
                        break;
                    default:
                        ChatServer.broadcastMessage(username + ": " + message);
                        break;
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

    public String getUsername() {
        return username;
    }
}
