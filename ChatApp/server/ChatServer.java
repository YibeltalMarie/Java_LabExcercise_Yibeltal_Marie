package server;

import java.net.*;
import java.util.*;
import java.io.*;
import model.Message;
import model.User;
import database.DatabaseConnection;

public class ChatServer {
    private static final int PORT = 5555;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static int clientIdCounter = 1;
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║         CHAT SERVER STARTING           ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("📡 Listening on port: " + PORT);
        System.out.println("💾 Initializing database...");
        
        // Test database connection
        if (DatabaseConnection.testConnection()) {
            System.out.println("✅ Database ready");
        } else {
            System.out.println("⚠️  Database not available - messages won't be saved");
        }
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("✨ Server is ready and waiting for connections...\n");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("📱 New client connected from: " + clientSocket.getInetAddress().getHostAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientIdCounter++);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
            
        } catch (IOException e) {
            System.err.println("❌ Server error: " + e.getMessage());
        }
    }
    
    // Broadcast message to all clients except sender
    public static void broadcast(Message message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
        System.out.println("📢 Broadcast from " + sender.getUsername());
    }
    
    // Send private message to specific client
    public static void sendPrivateMessage(Message message, int receiverId) {
        for (ClientHandler client : clients) {
            if (client.getUserId() == receiverId) {
                client.sendMessage(message);
                System.out.println("📨 Private message from " + message.getSenderName() + " to " + client.getUsername());
                return;
            }
        }
        System.out.println("⚠️  User ID " + receiverId + " is offline");
    }
    
    // Send message to all clients (including system messages)
    public static void sendToAll(Message message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
    
    // Get list of all connected clients
    public static List<ClientHandler> getConnectedClients() {
        return new ArrayList<>(clients);
    }
    
    // Remove disconnected client
    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("❌ " + client.getUsername() + " disconnected. Online: " + clients.size());
        
        // Broadcast updated user list
        broadcastUserList();
    }
    
    // Broadcast online users list to all clients
    public static void broadcastUserList() {
        List<User> onlineUsers = new ArrayList<>();
        for (ClientHandler client : clients) {
            User user = new User();
            user.setId(client.getUserId());
            user.setUsername(client.getUsername());
            user.setStatus("online");
            onlineUsers.add(user);
        }
        
        try {
            for (ClientHandler client : clients) {
                client.sendUserList(onlineUsers);
            }
        } catch (Exception e) {
            System.err.println("Error broadcasting user list: " + e.getMessage());
        }
    }
}