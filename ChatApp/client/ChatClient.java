package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Message;
import model.User;

import java.io.*;
import java.net.Socket;
import java.util.*;



public class ChatClient extends Application {
    
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private User currentUser;
    private boolean isConnected = false;
    
    private VBox messageContainer;
    private ScrollPane scrollPane;
    private TextField inputField;
    private ComboBox<String> recipientCombo;
    private ListView<String> userListView;
    private Label statusLabel;
    private Label currentChatLabel;
    private Stage primaryStage;
    
    private Map<String, List<Message>> conversationMessages = new HashMap<>();
    private String currentChatWith = null;
    private Map<String, Integer> userIdMap = new HashMap<>();
    private Map<Integer, String> idToUsernameMap = new HashMap<>(); // NEW: reverse mapping
    
    private String serverAddress = "localhost";
    private int serverPort = 5555;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Chat Application");
        showLoginScreen();
    }
    
    private void fixConversationKeys() {
        // This fixes messages that were stored under "User2", "User3" etc.
        Map<String, List<Message>> fixedMap = new HashMap<>();
        
        for (Map.Entry<String, List<Message>> entry : conversationMessages.entrySet()) {
            String key = entry.getKey();
            List<Message> messages = entry.getValue();
            
            // Check if this key is actually a user ID format (like "User2")
            if (key.startsWith("User") && key.length() > 4) {
                try {
                    int userId = Integer.parseInt(key.substring(4));
                    // Find the real username for this ID
                    String realUsername = idToUsernameMap.get(userId);
                    if (realUsername != null) {
                        // Move messages to the correct key
                        if (!fixedMap.containsKey(realUsername)) {
                            fixedMap.put(realUsername, new ArrayList<>());
                        }
                        fixedMap.get(realUsername).addAll(messages);
                        System.out.println("Fixed: Moved messages from " + key + " to " + realUsername);
                        continue;
                    }
                } catch (NumberFormatException e) {
                    // Not a numeric ID, keep as is
                }
            }
            
            // Keep original key if not fixed
            if (!fixedMap.containsKey(key)) {
                fixedMap.put(key, new ArrayList<>());
            }
            if (fixedMap.get(key) != messages) {
                fixedMap.get(key).addAll(messages);
            }
        }
        
        conversationMessages.clear();
        conversationMessages.putAll(fixedMap);
    }

    private void showLoginScreen() {
        VBox loginBox = new VBox(15);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(20));
        loginBox.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #3498db);");
        
        Label titleLabel = new Label("💬 Chat Application");
        titleLabel.setFont(new Font("Arial", 28));
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        VBox inputBox = new VBox(10);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setMaxWidth(300);
        inputBox.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-radius: 10;");
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(250);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(250);
        
        TextField serverField = new TextField("localhost");
        serverField.setPromptText("Server");
        
        TextField portField = new TextField("5555");
        portField.setPromptText("Port");
        
        Button loginBtn = new Button("Login");
        loginBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        loginBtn.setPrefWidth(250);
        
        Button registerBtn = new Button("Create Account");
        registerBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        registerBtn.setPrefWidth(250);
        
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");
        
        loginBtn.setOnAction(e -> {
            serverAddress = serverField.getText();
            serverPort = Integer.parseInt(portField.getText());
            
            String username = usernameField.getText();
            String password = passwordField.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Enter username and password");
                return;
            }
            
            loginBtn.setDisable(true);
            loginBtn.setText("Connecting...");
            
            new Thread(() -> {
                boolean success = login(username, password);
                Platform.runLater(() -> {
                    if (success) {
                        showChatScreen();
                    } else {
                        messageLabel.setText("Login failed!");
                        loginBtn.setText("Login");
                        loginBtn.setDisable(false);
                    }
                });
            }).start();
        });
        
        registerBtn.setOnAction(e -> showRegisterScreen());
        
        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(loginBtn, registerBtn);
        
        inputBox.getChildren().addAll(
            new Label("Username:"), usernameField,
            new Label("Password:"), passwordField,
            new Label("Server:"), serverField,
            new Label("Port:"), portField,
            buttonBox, messageLabel
        );
        
        loginBox.getChildren().addAll(titleLabel, inputBox);
        
        Scene scene = new Scene(loginBox, 400, 550);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void showRegisterScreen() {
        Stage regStage = new Stage();
        regStage.setTitle("Create Account");
        
        VBox regBox = new VBox(15);
        regBox.setAlignment(Pos.CENTER);
        regBox.setPadding(new Insets(20));
        regBox.setStyle("-fx-background-color: #ecf0f1;");
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(200);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(200);
        
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm Password");
        confirmField.setPrefWidth(200);
        
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");
        
        Button createBtn = new Button("Create");
        createBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        Button cancelBtn = new Button("Cancel");
        
        createBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirm = confirmField.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Username and password required");
                return;
            }
            if (!password.equals(confirm)) {
                statusLabel.setText("Passwords don't match");
                return;
            }
            if (password.length() < 4) {
                statusLabel.setText("Password too short (min 4)");
                return;
            }
            
            createBtn.setDisable(true);
            createBtn.setText("Creating...");
            
            new Thread(() -> {
                boolean success = sendRegistrationToServer(username, password);
                Platform.runLater(() -> {
                    if (success) {
                        statusLabel.setStyle("-fx-text-fill: green;");
                        statusLabel.setText("Success! You can login.");
                        new Thread(() -> {
                            try { Thread.sleep(1500); } catch (Exception ex) {}
                            Platform.runLater(regStage::close);
                        }).start();
                    } else {
                        statusLabel.setStyle("-fx-text-fill: red;");
                        statusLabel.setText("Username exists!");
                        createBtn.setText("Create");
                        createBtn.setDisable(false);
                    }
                });
            }).start();
        });
        
        cancelBtn.setOnAction(e -> regStage.close());
        
        HBox btnBox = new HBox(10, createBtn, cancelBtn);
        btnBox.setAlignment(Pos.CENTER);
        
        regBox.getChildren().addAll(
            new Label("Create New Account"),
            usernameField, passwordField, confirmField,
            btnBox, statusLabel
        );
        
        Scene scene = new Scene(regBox, 350, 350);
        regStage.setScene(scene);
        regStage.show();
    }
    
    private boolean sendRegistrationToServer(String username, String password) {
        try {
            Socket tempSocket = new Socket(serverAddress, serverPort);
            ObjectOutputStream tempOutput = new ObjectOutputStream(tempSocket.getOutputStream());
            ObjectInputStream tempInput = new ObjectInputStream(tempSocket.getInputStream());
            
            tempOutput.writeObject("REGISTER");
            tempOutput.writeObject(username);
            tempOutput.writeObject(password);
            tempOutput.flush();
            
            String response = (String) tempInput.readObject();
            tempSocket.close();
            
            return response.equals("REGISTER_SUCCESS");
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean login(String username, String password) {
        try {
            socket = new Socket(serverAddress, serverPort);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            
            output.writeObject(username);
            output.writeObject(password);
            output.flush();
            
            String response = (String) input.readObject();
            System.out.println("Login response: " + response);
            
            if (response.equals("LOGIN_SUCCESS")) {
                int userId = (Integer) input.readObject();
                currentUser = new User();
                currentUser.setUsername(username);
                currentUser.setId(userId);
                currentUser.setStatus("online");
                isConnected = true;
                System.out.println("Login successful! User ID: " + userId);
                
                startMessageListener();
                return true;
            }
            
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return false;
    }
    
    private void startMessageListener() {
        new Thread(() -> {
            try {
                while (isConnected) {
                    Object obj = input.readObject();
                    
                    if (obj instanceof Message) {
                        Message msg = (Message) obj;
                        Platform.runLater(() -> handleNewMessage(msg));
                    }
                    else if (obj instanceof String) {
                        String cmd = (String) obj;
                        if (cmd.equals("USER_LIST")) {
                            @SuppressWarnings("unchecked")
                            List<User> users = (List<User>) input.readObject();
                            System.out.println("Received USER_LIST with " + users.size() + " users");
                            Platform.runLater(() -> updateUserList(users));
                        }
                        else if (cmd.equals("CHAT_HISTORY")) {
                            @SuppressWarnings("unchecked")
                            List<Message> history = (List<Message>) input.readObject();
                            System.out.println("Received CHAT_HISTORY with " + history.size() + " messages");
                            Platform.runLater(() -> loadChatHistory(history));
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                if (isConnected) {
                    Platform.runLater(() -> addSystemMessage("Disconnected"));
                }
            }
        }).start();
    }
    
    private void loadChatHistory(List<Message> history) {
        System.out.println("📜 Loading " + history.size() + " messages");
        
        for (Message msg : history) {
            String conversationKey;
            
            if (msg.getSenderId() == currentUser.getId()) {
                // Message sent by me - use receiver_name from database
                conversationKey = msg.getReceiverName();
                if (conversationKey == null || conversationKey.equals("Broadcast")) {
                    conversationKey = "Broadcast";
                }
            } else {
                // Message received from someone else - use sender_name
                conversationKey = msg.getSenderName();
            }
            
            Message displayMsg = new Message();
            if (msg.getSenderId() == currentUser.getId()) {
                displayMsg.setSenderName("You");
            } else {
                displayMsg.setSenderName(conversationKey);
            }
            displayMsg.setContent(msg.getContent());
            displayMsg.setType(msg.getType());
            displayMsg.setFileName(msg.getFileName());
            displayMsg.setTimestamp(msg.getTimestamp());
            
            // Don't copy fileData for history - it will be loaded from disk
            displayMsg.setFileData(null);
            
            if (!conversationMessages.containsKey(conversationKey)) {
                conversationMessages.put(conversationKey, new ArrayList<>());
            }
            conversationMessages.get(conversationKey).add(displayMsg);
            
            System.out.println("  Stored in: " + conversationKey);
        }
        
        System.out.println("📂 Conversations: " + conversationMessages.keySet());
    }
    private void handleNewMessage(Message msg) {
        String sender = msg.getSenderName();
        String conversationKey;
        
        if (sender.equals("You")) {
            conversationKey = currentChatWith;
            if (conversationKey == null) conversationKey = "Broadcast";
        } else {
            conversationKey = sender;
        }
        
        if (!conversationMessages.containsKey(conversationKey)) {
            conversationMessages.put(conversationKey, new ArrayList<>());
        }
        conversationMessages.get(conversationKey).add(msg);
        
        if (conversationKey.equals(currentChatWith)) {
            displayMessage(msg);
        } else if (!sender.equals("You")) {
            markUserHasNewMessage(sender);
        }
    }
    
    private void markUserHasNewMessage(String username) {
        Platform.runLater(() -> {
            for (int i = 0; i < userListView.getItems().size(); i++) {
                String item = userListView.getItems().get(i);
                if (item.contains(username) && !item.contains("📩")) {
                    userListView.getItems().set(i, "📩 " + item);
                    break;
                }
            }
        });
    }
    
    private void showChatScreen() {
        BorderPane root = new BorderPane();
        
        VBox topBox = new VBox(5);
        topBox.setPadding(new Insets(10));
        topBox.setStyle("-fx-background-color: #2c3e50;");
        
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        
        Label userLabel = new Label("👤 " + currentUser.getUsername());
        userLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        statusLabel = new Label("🟢 Online");
        statusLabel.setStyle("-fx-text-fill: #27ae60;");
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> disconnect());
        
        topBar.getChildren().addAll(userLabel, spacer, statusLabel, logoutBtn);
        topBox.getChildren().add(topBar);
        
        currentChatLabel = new Label("Select a chat from the list");
        currentChatLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10; -fx-background-color: #ecf0f1;");
        
        VBox headerBox = new VBox(topBox, currentChatLabel);
        root.setTop(headerBox);
        
        messageContainer = new VBox(5);
        messageContainer.setPadding(new Insets(10));
        
        scrollPane = new ScrollPane(messageContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(450);
        
        userListView = new ListView<>();
        userListView.setPrefWidth(200);
        userListView.setPlaceholder(new Label("No users online"));
        
        userListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String cleanName = newVal.replace("📩 ", "").replace("🟢 ", "").replace("⚫ ", "");
                if (!cleanName.equals(currentUser.getUsername())) {
                    recipientCombo.setValue(cleanName);
                    openChatWith(cleanName);
                }
            }
        });
        
        recipientCombo = new ComboBox<>();
        recipientCombo.getItems().add("ALL (Broadcast)");
        recipientCombo.setValue("ALL (Broadcast)");
        
        recipientCombo.setOnAction(e -> {
            String selected = recipientCombo.getValue();
            if (selected != null && !selected.equals("ALL (Broadcast)")) {
                openChatWith(selected);
            } else if (selected != null && selected.equals("ALL (Broadcast)")) {
                currentChatWith = "Broadcast";
                currentChatLabel.setText("💬 Broadcast Chat");
                loadBroadcastMessages();
            }
        });
        
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10));
        inputBox.setStyle("-fx-background-color: #ecf0f1;");
        
        inputField = new TextField();
        inputField.setPromptText("Type message...");
        inputField.setPrefWidth(450);
        inputField.setOnAction(e -> sendMessage());
        
        Button sendBtn = new Button("Send");
        sendBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        sendBtn.setOnAction(e -> sendMessage());
        
        Button fileBtn = new Button("📎 Upload File");
        fileBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        fileBtn.setOnAction(e -> sendFile());
        
        inputBox.getChildren().addAll(recipientCombo, inputField, sendBtn, fileBtn);
        
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(userListView, scrollPane);
        splitPane.setDividerPositions(0.25);
        
        root.setCenter(splitPane);
        root.setBottom(inputBox);
        
        Scene scene = new Scene(root, 950, 600);
        primaryStage.setTitle("Chat - " + currentUser.getUsername());
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> disconnect());
        primaryStage.show();
        
        requestUserList();
    }
    
    private void openChatWith(String username) {
        currentChatWith = username;
        currentChatLabel.setText("💬 Chat with " + username);
        
        Platform.runLater(() -> {
            for (int i = 0; i < userListView.getItems().size(); i++) {
                String item = userListView.getItems().get(i);
                if (item.contains(username)) {
                    userListView.getItems().set(i, item.replace("📩 ", ""));
                    break;
                }
            }
        });
        
        messageContainer.getChildren().clear();
        
        List<Message> messages = conversationMessages.get(username);
        if (messages != null && !messages.isEmpty()) {
            System.out.println("📂 Showing " + messages.size() + " messages for " + username);
            for (Message msg : messages) {
                displayMessage(msg);
            }
        } else {
            System.out.println("📂 No messages for " + username);
        }
    }
    
    private void loadBroadcastMessages() {
        messageContainer.getChildren().clear();
        List<Message> messages = conversationMessages.get("Broadcast");
        if (messages != null) {
            for (Message msg : messages) {
                displayMessage(msg);
            }
        }
    }
    
    private void displayMessage(Message msg) {
        HBox bubble = new HBox();
        boolean isMe = msg.getSenderName().equals("You");
        bubble.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        
        VBox bubbleContent = new VBox(3);
        
        Label senderLabel = new Label(isMe ? "You" : msg.getSenderName());
        senderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11;");
        
        Node contentNode = null;
        
        if (msg.getType().equals("text")) {
            Label textLabel = new Label(msg.getContent());
            textLabel.setWrapText(true);
            textLabel.setMaxWidth(400);
            contentNode = textLabel;
        }
        else if (msg.getType().equals("image")) {
            // Load image from file path (saved on server)
            String imagePath = msg.getContent();
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(200);
                        imageView.setPreserveRatio(true);
                        imageView.setStyle("-fx-cursor: hand;");
                        imageView.setOnMouseClicked(e -> {
                            Stage imgStage = new Stage();
                            ImageView bigView = new ImageView(image);
                            bigView.setFitWidth(600);
                            bigView.setPreserveRatio(true);
                            VBox vbox = new VBox(bigView);
                            vbox.setAlignment(Pos.CENTER);
                            Scene scene = new Scene(vbox);
                            imgStage.setScene(scene);
                            imgStage.show();
                        });
                        contentNode = imageView;
                    } else {
                        contentNode = new Label("📷 Image: " + msg.getFileName() + " (file not found)");
                    }
                } catch (Exception ex) {
                    contentNode = new Label("📷 Image: " + msg.getFileName());
                }
            } else if (msg.getFileData() != null && msg.getFileData().length > 0) {
                // For real-time messages, use file data
                try {
                    Image image = new Image(new ByteArrayInputStream(msg.getFileData()));
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(200);
                    imageView.setPreserveRatio(true);
                    contentNode = imageView;
                } catch (Exception ex) {
                    contentNode = new Label("📷 Image: " + msg.getFileName());
                }
            } else {
                contentNode = new Label("📷 Image: " + msg.getFileName());
            }
        }
        else if (msg.getType().equals("audio")) {
            String audioPath = msg.getContent();
            if (audioPath != null && !audioPath.isEmpty()) {
                contentNode = createAudioPlayer(audioPath, msg.getFileName());
            } else if (msg.getFileData() != null && msg.getFileData().length > 0) {
                contentNode = createAudioPlayerFromData(msg.getFileData(), msg.getFileName());
            } else {
                contentNode = new Label("🎵 Audio: " + msg.getFileName());
            }
        }
        else if (msg.getType().equals("video")) {
            String videoPath = msg.getContent();
            if (videoPath != null && !videoPath.isEmpty()) {
                contentNode = createVideoPlayer(videoPath, msg.getFileName());
            } else if (msg.getFileData() != null && msg.getFileData().length > 0) {
                contentNode = createVideoPlayerFromData(msg.getFileData(), msg.getFileName());
            } else {
                contentNode = new Label("🎬 Video: " + msg.getFileName());
            }
        }
        else {
            contentNode = new Label("📎 " + msg.getFileName());
        }
        
        if (contentNode == null) {
            contentNode = new Label("Unknown content");
        }
        
        bubbleContent.getChildren().addAll(senderLabel, contentNode);
        
        if (isMe) {
            bubbleContent.setStyle("-fx-background-color: #3498db; -fx-background-radius: 10; -fx-padding: 8;");
            senderLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            bubbleContent.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 8; -fx-border-color: #ddd; -fx-border-radius: 10;");
        }
        
        bubble.getChildren().add(bubbleContent);
        messageContainer.getChildren().add(bubble);
        scrollPane.setVvalue(1.0);
    }
    
    private Node createAudioPlayerFromData(byte[] audioData, String fileName) {
        try {
            File tempFile = File.createTempFile("audio_", "_" + fileName);
            tempFile.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(audioData);
            }
            return createAudioPlayer(tempFile.getAbsolutePath(), fileName);
        } catch (Exception e) {
            return new Label("🎵 Audio: " + fileName);
        }
    }
    
    private Node createVideoPlayerFromData(byte[] videoData, String fileName) {
        try {
            File tempFile = File.createTempFile("video_", "_" + fileName);
            tempFile.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(videoData);
            }
            return createVideoPlayer(tempFile.getAbsolutePath(), fileName);
        } catch (Exception e) {
            return new Label("🎬 Video: " + fileName);
        }
    }
    
    private Node createAudioPlayer(String audioPath, String fileName) {
        VBox container = new VBox(5);
        try {
            File audioFile = new File(audioPath);
            if (audioFile.exists()) {
                Media media = new Media(audioFile.toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                
                Button playBtn = new Button("▶ Play");
                Button stopBtn = new Button("■ Stop");
                
                playBtn.setOnAction(e -> {
                    if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                        mediaPlayer.pause();
                        playBtn.setText("▶ Play");
                    } else {
                        mediaPlayer.play();
                        playBtn.setText("⏸ Pause");
                    }
                });
                
                stopBtn.setOnAction(e -> {
                    mediaPlayer.stop();
                    playBtn.setText("▶ Play");
                });
                
                HBox controls = new HBox(10, playBtn, stopBtn);
                container.getChildren().addAll(new Label("🎵 " + fileName), controls);
            } else {
                container.getChildren().add(new Label("🎵 " + fileName));
            }
        } catch (Exception e) {
            container.getChildren().add(new Label("🎵 " + fileName));
        }
        return container;
    }
    
    private Node createVideoPlayer(String videoPath, String fileName) {
        VBox container = new VBox(5);
        try {
            File videoFile = new File(videoPath);
            if (videoFile.exists()) {
                Media media = new Media(videoFile.toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                MediaView mediaView = new MediaView(mediaPlayer);
                mediaView.setFitWidth(300);
                mediaView.setPreserveRatio(true);
                
                Button playBtn = new Button("▶ Play");
                Button stopBtn = new Button("■ Stop");
                
                playBtn.setOnAction(e -> {
                    if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                        mediaPlayer.pause();
                        playBtn.setText("▶ Play");
                    } else {
                        mediaPlayer.play();
                        playBtn.setText("⏸ Pause");
                    }
                });
                
                stopBtn.setOnAction(e -> {
                    mediaPlayer.stop();
                    playBtn.setText("▶ Play");
                });
                
                HBox controls = new HBox(10, playBtn, stopBtn);
                container.getChildren().addAll(new Label("🎬 " + fileName), mediaView, controls);
            } else {
                container.getChildren().add(new Label("🎬 " + fileName));
            }
        } catch (Exception e) {
            container.getChildren().add(new Label("🎬 " + fileName));
        }
        return container;
    }
    
    private void addSystemMessage(String text) {
        Label sysLabel = new Label("🔔 " + text);
        sysLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic; -fx-font-size: 11;");
        messageContainer.getChildren().add(sysLabel);
        scrollPane.setVvalue(1.0);
    }
    
    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        
        String recipient = recipientCombo.getValue();
        int receiverId = 0;
        
        if (recipient != null && !recipient.equals("ALL (Broadcast)")) {
            receiverId = userIdMap.getOrDefault(recipient, 0);
            System.out.println("Sending to: " + recipient + " with ID: " + receiverId);
        }
        
        Message msg = new Message(currentUser.getId(), receiverId, "text", text);
        msg.setSenderName(currentUser.getUsername());
        
        try {
            output.writeObject(msg);
            output.flush();
            inputField.clear();
        } catch (IOException e) {
            addSystemMessage("Failed to send");
        }
    }
    
    private void sendFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file == null) return;
        
        String recipient = recipientCombo.getValue();
        int receiverId = recipient.equals("ALL (Broadcast)") ? 0 : userIdMap.getOrDefault(recipient, 0);
        
        new Thread(() -> {
            try {
                byte[] fileData = new byte[(int) file.length()];
                try (FileInputStream fis = new FileInputStream(file)) {
                    fis.read(fileData);
                }
                
                String fileType = determineFileType(file.getName());
                
                Message msg = new Message(currentUser.getId(), receiverId, fileType, "");
                msg.setFileName(file.getName());
                msg.setFileData(fileData);
                msg.setSenderName(currentUser.getUsername());
                
                output.writeObject(msg);
                output.flush();
                
                Platform.runLater(() -> addSystemMessage("Sent: " + file.getName()));
                
            } catch (IOException e) {
                Platform.runLater(() -> addSystemMessage("Send failed"));
            }
        }).start();
    }
    
    private String determineFileType(String fileName) {
        if (fileName.matches(".*\\.(jpg|png|gif|bmp|jpeg)$")) return "image";
        if (fileName.matches(".*\\.(mp3|wav)$")) return "audio";
        if (fileName.matches(".*\\.(mp4|avi)$")) return "video";
        return "file";
    }
    
    private void updateUserList(List<User> users) {
        Platform.runLater(() -> {
            userListView.getItems().clear();
            recipientCombo.getItems().clear();
            recipientCombo.getItems().add("ALL (Broadcast)");
            
            userIdMap.clear();
            idToUsernameMap.clear();
            
            for (User user : users) {
                userIdMap.put(user.getUsername(), user.getId());
                idToUsernameMap.put(user.getId(), user.getUsername());
                System.out.println("Mapped: " + user.getUsername() + " -> ID: " + user.getId());
                
                if (user.getId() != currentUser.getId()) {
                    userListView.getItems().add("🟢 " + user.getUsername());
                    recipientCombo.getItems().add(user.getUsername());
                }
            }
            
            // After updating user map, reload and fix any messages that were stored with "UserX" names
            fixConversationKeys();
            
            // Refresh current chat if open
            if (currentChatWith != null && conversationMessages.containsKey(currentChatWith)) {
                messageContainer.getChildren().clear();
                for (Message msg : conversationMessages.get(currentChatWith)) {
                    displayMessage(msg);
                }
            }
            
            System.out.println("User list updated. Total users: " + userListView.getItems().size());
        });
    }
    
    private void requestUserList() {
        try {
            output.writeObject("GET_USERS");
            output.flush();
        } catch (IOException e) {}
    }
    
    private void disconnect() {
        isConnected = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {}
        Platform.exit();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}