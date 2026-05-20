package server;

import java.io.*;
import java.net.*;
import java.util.*;
import model.Message;
import model.User;
import database.UserDAO;
import database.MessageDAO;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private User currentUser;
    private int clientId;
    private boolean isRunning = true;
    
    public ClientHandler(Socket socket, int clientId) {
        this.socket = socket;
        this.clientId = clientId;
        
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error creating streams: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        try {
            Object firstMessage = input.readObject();
            
            if (firstMessage instanceof String) {
                String command = (String) firstMessage;
                if (command.equals("REGISTER")) {
                    handleRegistration();
                    socket.close();
                    return;
                }
            }
            
            String username = (String) firstMessage;
            String password = (String) input.readObject();
            
            if (!handleLogin(username, password)) {
                System.out.println("Login failed - closing connection");
                socket.close();
                return;
            }
            
            System.out.println("✅ " + currentUser.getUsername() + " logged in successfully");
            
            sendWelcomeMessage();
            
            // IMPORTANT: Send USER_LIST FIRST so userIdMap is populated
            ChatServer.broadcastUserList();  // ← This sends USER_LIST to client
            
            // Then send chat history
            sendChatHistory();               // ← This sends CHAT_HISTORY
            
            while (isRunning) {
                Object obj = input.readObject();
                
                if (obj == null) break;
                
                if (obj instanceof Message) {
                    Message message = (Message) obj;
                    message.setSenderId(currentUser.getId());
                    message.setSenderName(currentUser.getUsername());
                    
                    byte[] fileDataBackup = null;
                    if (message.getFileData() != null && message.getFileData().length > 0) {
                        fileDataBackup = message.getFileData();
                        String savedPath = saveFile(message);
                        message.setContent(savedPath);
                    }
                    
                    boolean saved = MessageDAO.saveMessage(message);
                    System.out.println("Message saved to DB: " + saved);
                    
                    Message senderCopy = new Message();
                    senderCopy.setId(message.getId());
                    senderCopy.setSenderId(message.getSenderId());
                    senderCopy.setSenderName("You");
                    senderCopy.setReceiverId(message.getReceiverId());
                    senderCopy.setType(message.getType());
                    senderCopy.setContent(message.getContent());
                    senderCopy.setFileName(message.getFileName());
                    senderCopy.setTimestamp(message.getTimestamp());
                    
                    sendMessage(senderCopy);
                    
                    if (message.getReceiverId() == 0) {
                        for (ClientHandler client : ChatServer.getConnectedClients()) {
                            if (client.getUserId() != currentUser.getId()) {
                                Message broadcastCopy = new Message();
                                broadcastCopy.setId(message.getId());
                                broadcastCopy.setSenderId(message.getSenderId());
                                broadcastCopy.setSenderName(message.getSenderName());
                                broadcastCopy.setReceiverId(0);
                                broadcastCopy.setType(message.getType());
                                broadcastCopy.setContent(message.getContent());
                                broadcastCopy.setFileName(message.getFileName());
                                broadcastCopy.setTimestamp(message.getTimestamp());
                                if (fileDataBackup != null) {
                                    broadcastCopy.setFileData(fileDataBackup);
                                }
                                client.sendMessage(broadcastCopy);
                            }
                        }
                        System.out.println("📢 Broadcast from " + currentUser.getUsername());
                    } else {
                        for (ClientHandler client : ChatServer.getConnectedClients()) {
                            if (client.getUserId() == message.getReceiverId()) {
                                Message privateCopy = new Message();
                                privateCopy.setId(message.getId());
                                privateCopy.setSenderId(message.getSenderId());
                                privateCopy.setSenderName(message.getSenderName());
                                privateCopy.setReceiverId(message.getReceiverId());
                                privateCopy.setType(message.getType());
                                privateCopy.setContent(message.getContent());
                                privateCopy.setFileName(message.getFileName());
                                privateCopy.setTimestamp(message.getTimestamp());
                                if (fileDataBackup != null) {
                                    privateCopy.setFileData(fileDataBackup);
                                }
                                client.sendMessage(privateCopy);
                                System.out.println("📨 Private message to " + client.getUsername());
                                break;
                            }
                        }
                    }
                    message.setFileData(null);
                }
                else if (obj instanceof String) {
                    String cmd = (String) obj;
                    if (cmd.equals("GET_USERS")) {
                        sendOnlineUsers();
                    }
                }
            }
            
        } catch (IOException e) {
            System.out.println("Connection lost with " + (currentUser != null ? currentUser.getUsername() : "unknown"));
        } catch (ClassNotFoundException e) {
            System.err.println("Invalid data received: " + e.getMessage());
        } finally {
            if (currentUser != null) {
                UserDAO.updateStatus(currentUser.getId(), "offline");
            }
            ChatServer.removeClient(this);
            try { socket.close(); } catch (IOException e) {}
        }
    }
    
    private void handleRegistration() throws IOException, ClassNotFoundException {
        String username = (String) input.readObject();
        String password = (String) input.readObject();
        
        System.out.println("📝 Registration request for: " + username);
        
        if (UserDAO.usernameExists(username)) {
            output.writeObject("REGISTER_FAILED_USERNAME_EXISTS");
            output.flush();
            System.out.println("✗ Registration failed - username exists: " + username);
            return;
        }
        
        boolean success = UserDAO.registerUser(username, password);
        
        if (success) {
            output.writeObject("REGISTER_SUCCESS");
            output.flush();
            System.out.println("✓ New user registered: " + username);
        } else {
            output.writeObject("REGISTER_FAILED");
            output.flush();
            System.out.println("✗ Registration failed for: " + username);
        }
    }
    
    private boolean handleLogin(String username, String password) throws IOException {
        currentUser = UserDAO.login(username, password);
        
        if (currentUser != null) {
            output.writeObject("LOGIN_SUCCESS");
            output.writeObject(currentUser.getId());
            output.flush();
            return true;
        } else {
            output.writeObject("LOGIN_FAILED");
            output.flush();
            return false;
        }
    }
    
    private void sendWelcomeMessage() throws IOException {
        Message welcome = new Message();
        welcome.setSenderId(0);
        welcome.setSenderName("SYSTEM");
        welcome.setReceiverId(currentUser.getId());
        welcome.setType("text");
        welcome.setContent("Welcome " + currentUser.getUsername() + "!");
        sendMessage(welcome);
    }
    
    private void sendChatHistory() throws IOException {
        List<Message> history = MessageDAO.getRecentMessages(currentUser.getId(), 50);
        output.writeObject("CHAT_HISTORY");
        output.writeObject(history);
        output.flush();
        System.out.println("📜 Sent chat history: " + history.size() + " messages");
    }
    
    private void sendOnlineUsers() throws IOException {
        List<User> onlineUsers = new ArrayList<>();
        for (ClientHandler client : ChatServer.getConnectedClients()) {
            User user = new User();
            user.setId(client.getUserId());
            user.setUsername(client.getUsername());
            user.setStatus("online");
            onlineUsers.add(user);
        }
        output.writeObject("USER_LIST");
        output.writeObject(onlineUsers);
        output.flush();
    }
    
    public void sendUserList(List<User> users) {
        try {
            output.writeObject("USER_LIST");
            output.writeObject(users);
            output.flush();
        } catch (IOException e) {
            System.err.println("Error sending user list");
        }
    }
    
    private String saveFile(Message message) {
        String saveDir = "received_files/";
        File dir = new File(saveDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filePath = saveDir + timestamp + "_" + message.getFileName();
        
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(message.getFileData());
            System.out.println("💾 File saved: " + filePath);
            return filePath;
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
            return null;
        }
    }
    
    public void sendMessage(Message message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            System.err.println("Error sending message");
        }
    }
    
    public int getUserId() { return currentUser != null ? currentUser.getId() : -1; }
    public String getUsername() { return currentUser != null ? currentUser.getUsername() : "Unknown"; }
}