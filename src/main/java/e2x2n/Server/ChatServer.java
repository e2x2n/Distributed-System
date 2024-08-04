package e2x2n.Server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static final Set<ServerClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    private static final Map<String, Group> groups = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        System.out.println("Server is listening on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");
                ServerClientHandler serverClientHandler = new ServerClientHandler(clientSocket);
                clients.add(serverClientHandler);
                serverClientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcastMessage(String message) {
        synchronized (clients) {
            for (ServerClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    public static synchronized void createGroup(String groupName) {
        if (!groups.containsKey(groupName)) {
            groups.put(groupName, new Group(groupName));
            System.out.println("Group created: " + groupName);
        }
    }

    public static synchronized void joinGroup(String groupName, ServerClientHandler client) {
        if (groups.containsKey(groupName)) {
            groups.get(groupName).addParticipants(client);
            System.out.println(client.getUsername() + " joined group " + groupName);
        }
    }

    public static synchronized void leaveGroup(String groupName, ServerClientHandler client) {
        if (groups.containsKey(groupName)) {
            groups.get(groupName).removeParticipants(client);
            System.out.println(client.getUsername() + " left group " + groupName);
        }
    }

    public static synchronized void broadcastGroupMessage(String groupName, String message,
                                                          ServerClientHandler sender) {
        if (groups.containsKey(groupName)) {
            Group group = groups.get(groupName);
            if (group.getParticipants().contains(sender)) {
                group.broadcastMessage(message);
            } else {
                sender.sendMessage("You are not a member of group " + groupName);
            }
        } else {
            sender.sendMessage("Group " + groupName + " does not exist");
        }
    }
}
