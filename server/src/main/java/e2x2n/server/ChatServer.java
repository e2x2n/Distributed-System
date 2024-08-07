package e2x2n.server;

import e2x2n.service.GroupService;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static final Set<ServerClientHandler> clients = Collections.synchronizedSet(new HashSet<>());

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

    public static synchronized void broadcastGroupMessage(String groupName, String message,
                                                          ServerClientHandler sender) {
        Set<String> members = GroupService.getGroupMembers(groupName);
        if (members != null && members.contains(sender.getUsername())) {
            synchronized (clients) {
                for (ServerClientHandler client : clients) {
                    if (members.contains(client.getUsername())) {
                        client.sendMessage(message);
                    }
                }
            }
        } else {
            sender.sendMessage("You are not a member of group " + groupName);
        }
    }
}
