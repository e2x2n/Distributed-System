package e2x2n.server;

import e2x2n.service.UserService;
import e2x2n.service.GroupService;
import e2x2n.service.MessageService;

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
                out.println("Enter command: /register or /login");
                String command = in.readLine();
                if ("/register".equalsIgnoreCase(command)) {
                    handleRegistration();
                } else if ("/login".equalsIgnoreCase(command)) {
                    if (handleAuthentication()) {
                        break;
                    }
                } else {
                    out.println("Invalid command. Please enter /register or /login");
                }
            }

            String message;
            while ((message = in.readLine()) != null) {
                String[] parts = message.split(" ", 3);
                String command = parts[0];
                switch (command) {
                    case "/msg":
                        if (parts.length == 3){
                            String recipient = parts[1];
                            String privateMessage = parts[2];
                            if (UserService.isUserExists(recipient)) {
                                sendPrivateMessage(recipient, username + ": " + privateMessage);
                                MessageService.sendMessage(UserService.getUserId(username),
                                        UserService.getUserId(recipient), null, privateMessage);
                            } else {
                                sendMessage("User " + recipient + " not found");
                            }
                        } else {
                            sendMessage("Invalid private message format. Usage: /msg <recipient> <message>");
                        }
                        break;
                    case "/create":
                        if (parts.length == 2) {
                            String groupName = parts[1];
//                            ChatServer.createGroup(groupName);
//                            ChatServer.joinGroup(groupName, this);
                            sendMessage("Group " + groupName + " created. You have joined the group.");
                            GroupService.createGroup(groupName);
                            GroupService.addUserToGroup(groupName, username);
                        } else {
                            sendMessage("Invalid group creation format. Usage: /create <group_name>");
                        }
                        break;
                    case "/join":
                        if (parts.length == 2) {
                            String groupName = parts[1];
                            try {
                                GroupService.addUserToGroup(groupName, username);
                                sendMessage("You have joined group " + groupName);
                            } catch (IllegalArgumentException e) {
                                sendMessage(e.getMessage());
                            }
                        } else {
                            sendMessage("Invalid group join format. Usage: /join <group_name>");
                        }
                        break;
                    case "/leave":
                        if (parts.length == 2) {
                            String groupName = parts[1];
                            try {
                                GroupService.removeUserFromGroup(username, groupName);
                                sendMessage("You have left group " + groupName);
                            } catch (IllegalArgumentException e) {
                                sendMessage(e.getMessage());
                            }
                        } else {
                            sendMessage("Invalid group leave format. Usage: /leave <group_name>");
                        }
                        break;
                    case "/group":
                        if (parts.length == 3) {
                            String groupName = parts[1];
                            String groupMessage = parts[2];
                            if (GroupService.isGroupExists(groupName)) {
                                ChatServer.broadcastGroupMessage(groupName, "[" + groupName + "] " +
                                        username + ": " + groupMessage, this);
                                MessageService.sendMessage(UserService.getUserId(username),
                                        null, GroupService.getGroupId(groupName), groupMessage);
                            } else {
                                sendMessage("Group " + groupName + " does not exist");
                            }
                        } else {
                            sendMessage("Invalid group message format. Usage: /group <group_name> <message>");
                        }
                        break;
                    default:
                        ChatServer.broadcastMessage(username + ": " + message);
                        MessageService.sendMessage(UserService.getUserId(username),
                                null, null, message);
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

    private void handleRegistration() throws IOException {
            out.println("Create your username: ");
            String username = in.readLine();
            out.println("Create your password: ");
            String password = in.readLine();
            try {
                UserService.registerUser(username, password);
                out.println("Registration successful, welcome, " + username);
            } catch (IllegalArgumentException e) {
                out.println(e.getMessage());
            }
    }

    private boolean handleAuthentication() throws IOException {
        out.println("Enter your username: ");
        String username = in.readLine();
        out.println("Enter your password: ");
        String password = in.readLine();
        if (UserService.authenticateUser(username, password)) {
            this.username = username;
            synchronized (serverClientHandlers) {
                if (!serverClientHandlers.containsKey(username)) {
                    serverClientHandlers.put(username, this);
                    out.println("Welcome, " + username);
                    return true;
                } else {
                    out.println("User already logged in");
                }
            }
        } else {
                out.println("Invalid username or password");
        }
        return false;
    }

    public void sendPrivateMessage(String recipient, String message) {
            ServerClientHandler recipientHandler = serverClientHandlers.get(recipient);
            recipientHandler.sendMessage("[Private] " + message);
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getUsername() {
        return username;
    }
}
