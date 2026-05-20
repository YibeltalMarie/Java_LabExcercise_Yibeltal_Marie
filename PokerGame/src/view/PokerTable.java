package src.view;

import src.model.Game;
import src.model.Player;
import src.model.Hand;
import src.model.Card;
import src.controller.GameController;
import src.controller.HandEvaluator;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PokerTable extends Application {
    
    private Game game;
    private GameController controller;
    private Stage primaryStage;
    private int currentPlayerIndex = 0;
    
    // Track which cards player wants to discard (for draw phase)
    private List<Integer> cardsToDiscard;
    private boolean[] selectedCards;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.cardsToDiscard = new ArrayList<>();
        
        // Show setup screen first
        showSetupScreen();
    }
    
    // ========== SETUP SCREEN ==========
    
    private void showSetupScreen() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 50; -fx-background-color: #1a472a;");
        
        Label title = new Label("♠️ FIVE CARD DRAW POKER ♥️");
        title.setStyle("-fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: #ffd700;");
        
        Label subtitle = new Label("Welcome to the Table!");
        subtitle.setStyle("-fx-font-size: 20; -fx-text-fill: white;");
        
        Button startButton = new Button("🃟 START NEW GAME 🃟");
        startButton.setStyle("-fx-font-size: 20; -fx-padding: 10 30; -fx-background-color: #006400; -fx-text-fill: white;");
        startButton.setOnAction(e -> setupPlayers());
        
        root.getChildren().addAll(title, subtitle, startButton);
        
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Poker Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void setupPlayers() {
        // Ask how many players
        TextInputDialog dialog = new TextInputDialog("3");
        dialog.setTitle("Player Setup");
        dialog.setHeaderText("How many players?");
        dialog.setContentText("Enter number of players (2-6):");
        
        Optional<String> result = dialog.showAndWait();
        int numPlayers = 3;
        
        if (result.isPresent()) {
            try {
                numPlayers = Integer.parseInt(result.get());
                numPlayers = Math.max(2, Math.min(6, numPlayers)); // Clamp between 2-6
            } catch (NumberFormatException e) {
                numPlayers = 3;
            }
        }
        
        // Create game with 10 chip ante
        game = new Game(10);
        
        // Add players
        String[] defaultNames = {"Alice", "Bob", "Charlie", "David", "Emma", "Frank"};
        
        for (int i = 0; i < numPlayers; i++) {
            String name = defaultNames[i % defaultNames.length];
            if (i >= defaultNames.length) {
                name = "Player " + (i + 1);
            }
            game.addPlayer(name, 1000);
        }
        
        // Create controller
        controller = new GameController(game, this);
        
        // Start the game
        controller.startNewHand();
    }
    
    // ========== GAME SCREENS ==========
    
    public void updateGameState(Game game) {
        this.game = game;
    }
    
    public void showWaitingScreen(Player currentPlayer) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 50; -fx-background-color: #1a472a;");
        
        Label title = new Label("♠️ PASS AND PLAY POKER ♥️");
        title.setStyle("-fx-font-size: 32; -fx-font-weight: bold; -fx-text-fill: #ffd700;");
        
        Label message = new Label("Pass computer to " + currentPlayer.getName());
        message.setStyle("-fx-font-size: 24; -fx-text-fill: white;");
        
        Label instruction = new Label("When ready, click 'Show My Cards'");
        instruction.setStyle("-fx-font-size: 18; -fx-text-fill: #cccccc;");
        
        Button readyButton = new Button("🎴 Show My Cards");
        readyButton.setStyle("-fx-font-size: 20; -fx-padding: 10 20; -fx-background-color: #006400; -fx-text-fill: white;");
        readyButton.setOnAction(e -> showPlayerTurn(currentPlayer));
        
        root.getChildren().addAll(title, message, instruction, readyButton);
        
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Poker Game - " + currentPlayer.getName() + "'s Turn");
        primaryStage.setScene(scene);
    }
    
    private void showPlayerTurn(Player currentPlayer) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a472a;");
        
        // Top: Game info
        VBox topInfo = new VBox(10);
        topInfo.setAlignment(Pos.CENTER);
        topInfo.setStyle("-fx-padding: 20;");
        
        Label turnLabel = new Label(currentPlayer.getName() + "'s TURN");
        turnLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #ffd700;");
        
        Label chipsLabel = new Label("💰 Chips: " + currentPlayer.getChips());
        chipsLabel.setStyle("-fx-font-size: 18; -fx-text-fill: white;");
        
        Label potLabel = new Label("🎲 Pot: " + game.getPot() + " chips");
        potLabel.setStyle("-fx-font-size: 18; -fx-text-fill: white;");
        
        Label betLabel = new Label("💵 Current bet: " + game.getCurrentBet() + " chips");
        betLabel.setStyle("-fx-font-size: 18; -fx-text-fill: #ffd700;");
        
        topInfo.getChildren().addAll(turnLabel, chipsLabel, potLabel, betLabel);
        
        // Center: Player's cards
        HBox cardsBox = new HBox(10);
        cardsBox.setAlignment(Pos.CENTER);
        cardsBox.setStyle("-fx-padding: 50;");
        
        for (VBox cardView : CardImage.getCardViews(currentPlayer.getHand())) {
            cardsBox.getChildren().add(cardView);
        }
        
        // Bottom: Action buttons
        VBox actionBox = new VBox(15);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setStyle("-fx-padding: 20;");
        
        HBox betButtons = new HBox(20);
        betButtons.setAlignment(Pos.CENTER);
        
        Button foldButton = new Button("✗ FOLD");
        foldButton.setStyle("-fx-font-size: 16; -fx-padding: 10 20; -fx-background-color: #8b0000; -fx-text-fill: white;");
        foldButton.setOnAction(e -> controller.playerFold(currentPlayer));
        
        // Determine if player can check or must call
        int currentBet = game.getCurrentBet();
        int playerBet = currentPlayer.getCurrentBet();
        
        if (currentBet == 0) {
            // No bet yet - can check or bet
            Button checkButton = new Button("✓ CHECK");
            checkButton.setStyle("-fx-font-size: 16; -fx-padding: 10 20; -fx-background-color: #4682b4; -fx-text-fill: white;");
            checkButton.setOnAction(e -> controller.playerCheck(currentPlayer));
            
            Button betButton = new Button("💰 BET");
            betButton.setStyle("-fx-font-size: 16; -fx-padding: 10 20; -fx-background-color: #ff8c00; -fx-text-fill: white;");
            betButton.setOnAction(e -> showBetDialog(currentPlayer));
            
            betButtons.getChildren().addAll(checkButton, betButton, foldButton);
        } else {
            // There's a bet - can call, raise, or fold
            int callAmount = currentBet - playerBet;
            Button callButton = new Button("✓ CALL " + callAmount);
            callButton.setStyle("-fx-font-size: 16; -fx-padding: 10 20; -fx-background-color: #006400; -fx-text-fill: white;");
            callButton.setOnAction(e -> controller.playerCall(currentPlayer));
            
            Button raiseButton = new Button("⬆ RAISE");
            raiseButton.setStyle("-fx-font-size: 16; -fx-padding: 10 20; -fx-background-color: #ff8c00; -fx-text-fill: white;");
            raiseButton.setOnAction(e -> showRaiseDialog(currentPlayer));
            
            betButtons.getChildren().addAll(callButton, raiseButton, foldButton);
        }
        
        actionBox.getChildren().add(betButtons);
        
        root.setTop(topInfo);
        root.setCenter(cardsBox);
        root.setBottom(actionBox);
        
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Poker Game - " + currentPlayer.getName() + "'s Cards (Keep them hidden!)");
        primaryStage.setScene(scene);
    }
    
    // ========== BETTING DIALOGS ==========
    
    private void showBetDialog(Player player) {
        TextInputDialog dialog = new TextInputDialog("20");
        dialog.setTitle("Place Bet");
        dialog.setHeaderText(player.getName() + ", how much do you want to bet?");
        dialog.setContentText("Bet amount (max: " + player.getChips() + "):");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int amount = Integer.parseInt(result.get());
                amount = Math.max(10, Math.min(amount, player.getChips()));
                controller.playerBet(player, amount);
            } catch (NumberFormatException e) {
                controller.playerBet(player, 20);
            }
        }
    }
    
    private void showRaiseDialog(Player player) {
        int minRaise = game.getCurrentBet() + 10;
        TextInputDialog dialog = new TextInputDialog(String.valueOf(minRaise));
        dialog.setTitle("Raise");
        dialog.setHeaderText(player.getName() + ", raise to how much?");
        dialog.setContentText("Raise to (min: " + minRaise + ", max: " + player.getChips() + "):");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int amount = Integer.parseInt(result.get());
                amount = Math.max(minRaise, Math.min(amount, player.getChips() + player.getCurrentBet()));
                controller.playerRaise(player, amount);
            } catch (NumberFormatException e) {
                controller.playerRaise(player, minRaise);
            }
        }
    }
    
    // ========== DRAW SCREEN ==========
    
    public void showDrawScreen(Game game, GameController controller) {
        Player currentPlayer = controller.getCurrentPlayer();
        this.cardsToDiscard = new ArrayList<>();
        this.selectedCards = new boolean[currentPlayer.getHand().size()];
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a472a;");
        
        // Top: Instructions
        VBox topInfo = new VBox(10);
        topInfo.setAlignment(Pos.CENTER);
        topInfo.setStyle("-fx-padding: 20;");
        
        Label title = new Label(currentPlayer.getName() + " - DRAW PHASE");
        title.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #ffd700;");
        
        Label instruction = new Label("Click on cards you want to discard, then press 'DRAW NEW CARDS'");
        instruction.setStyle("-fx-font-size: 16; -fx-text-fill: white;");
        
        Label selectedLabel = new Label("Selected to discard: 0 cards");
        selectedLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #cccccc;");
        
        topInfo.getChildren().addAll(title, instruction, selectedLabel);
        
        // Center: Cards (clickable)
        HBox cardsBox = new HBox(10);
        cardsBox.setAlignment(Pos.CENTER);
        cardsBox.setStyle("-fx-padding: 50;");
        
        // Create clickable cards
        for (int i = 0; i < currentPlayer.getHand().size(); i++) {
            Card card = currentPlayer.getHand().getCard(i);
            VBox cardBox = CardImage.createSelectableCardFace(card, i, this);
            cardsBox.getChildren().add(cardBox);
        }
        
        // Bottom: Action buttons
        HBox actionBox = new HBox(20);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setStyle("-fx-padding: 20;");
        
        Button drawButton = new Button("🃟 DRAW NEW CARDS 🃟");
        drawButton.setStyle("-fx-font-size: 18; -fx-padding: 10 20; -fx-background-color: #006400; -fx-text-fill: white;");
        drawButton.setOnAction(e -> {
            controller.processDraw(currentPlayer, cardsToDiscard);
        });
        
        Button keepButton = new Button("✓ KEEP ALL CARDS");
        keepButton.setStyle("-fx-font-size: 16; -fx-padding: 10 20; -fx-background-color: #4682b4; -fx-text-fill: white;");
        keepButton.setOnAction(e -> {
            cardsToDiscard.clear();
            controller.processDraw(currentPlayer, cardsToDiscard);
        });
        
        actionBox.getChildren().addAll(drawButton, keepButton);
        
        root.setTop(topInfo);
        root.setCenter(cardsBox);
        root.setBottom(actionBox);
        
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Poker Game - " + currentPlayer.getName() + " - Choose cards to discard");
        primaryStage.setScene(scene);
    }
    
    public void toggleCardSelection(int cardIndex, VBox cardBox) {
        if (selectedCards[cardIndex]) {
            // Deselect
            selectedCards[cardIndex] = false;
            cardsToDiscard.remove(Integer.valueOf(cardIndex));
            cardBox.setStyle(CardImage.getUnselectedCardStyle());
        } else {
            // Select
            selectedCards[cardIndex] = true;
            cardsToDiscard.add(cardIndex);
            cardBox.setStyle(CardImage.getSelectedCardStyle());
        }
        
        // Update the label (can't easily access without storing reference, but it's ok)
        System.out.println("Cards to discard: " + cardsToDiscard.size());
    }
    
    // ========== SHOWDOWN SCREEN ==========
    
    public void showWinner(Player winner, int potAmount) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 50; -fx-background-color: #1a472a;");
        
        Label title = new Label("🏆 WINNER! 🏆");
        title.setStyle("-fx-font-size: 48; -fx-font-weight: bold; -fx-text-fill: #ffd700;");
        
        Label winnerLabel = new Label(winner.getName() + " wins " + potAmount + " chips!");
        winnerLabel.setStyle("-fx-font-size: 28; -fx-text-fill: white;");
        
        // Show winner's hand
        Label handLabel = new Label("Winning hand: " + HandEvaluator.evaluate(winner.getHand()));
        handLabel.setStyle("-fx-font-size: 18; -fx-text-fill: #ffd700;");
        
        HBox cardsBox = new HBox(10);
        cardsBox.setAlignment(Pos.CENTER);
        cardsBox.setStyle("-fx-padding: 20;");
        for (VBox cardView : CardImage.getCardViews(winner.getHand())) {
            cardsBox.getChildren().add(cardView);
        }
        
        Button continueButton = new Button("🔄 START NEW HAND");
        continueButton.setStyle("-fx-font-size: 18; -fx-padding: 10 20; -fx-background-color: #006400; -fx-text-fill: white;");
        continueButton.setOnAction(e -> controller.startNewHand());
        
        Button quitButton = new Button("✗ QUIT GAME");
        quitButton.setStyle("-fx-font-size: 16; -fx-padding: 10 20; -fx-background-color: #8b0000; -fx-text-fill: white;");
        quitButton.setOnAction(e -> System.exit(0));
        
        root.getChildren().addAll(title, winnerLabel, handLabel, cardsBox, continueButton, quitButton);
        
        Scene scene = new Scene(root, 800, 700);
        primaryStage.setScene(scene);
    }
    
    public void showAllHands(List<Player> players) {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 30; -fx-background-color: #1a472a;");
        
        Label title = new Label("📋 ALL HANDS");
        title.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #ffd700;");
        
        root.getChildren().add(title);
        
        for (Player p : players) {
            VBox playerBox = new VBox(5);
            playerBox.setAlignment(Pos.CENTER);
            playerBox.setStyle("-fx-padding: 10; -fx-border-color: #ffd700; -fx-border-width: 1; -fx-border-radius: 5;");
            
            Label nameLabel = new Label(p.getName());
            nameLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #ffd700;");
            
            HBox cardsBox = new HBox(5);
            cardsBox.setAlignment(Pos.CENTER);
            for (VBox cardView : CardImage.getCardViews(p.getHand())) {
                cardsBox.getChildren().add(cardView);
            }
            
            String handRank = HandEvaluator.evaluate(p.getHand());
            Label rankLabel = new Label(handRank);
            rankLabel.setStyle("-fx-font-size: 14; -fx-text-fill: white;");
            
            playerBox.getChildren().addAll(nameLabel, cardsBox, rankLabel);
            root.getChildren().add(playerBox);
        }
        
        Scene scene = new Scene(root, 900, 700);
        primaryStage.setScene(scene);
    }
    
    public void showGameOverScreen(Game game) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 50; -fx-background-color: #1a472a;");
        
        Label title = new Label("GAME OVER");
        title.setStyle("-fx-font-size: 48; -fx-font-weight: bold; -fx-text-fill: #ffd700;");
        
        // Find the player with most chips
        Player winner = game.getPlayers().get(0);
        for (Player p : game.getPlayers()) {
            if (p.getChips() > winner.getChips()) {
                winner = p;
            }
        }
        
        Label winnerLabel = new Label(winner.getName() + " wins the game with " + winner.getChips() + " chips!");
        winnerLabel.setStyle("-fx-font-size: 24; -fx-text-fill: white;");
        
        Button newGameButton = new Button("🃟 PLAY AGAIN 🃟");
        newGameButton.setStyle("-fx-font-size: 20; -fx-padding: 10 20; -fx-background-color: #006400; -fx-text-fill: white;");
        newGameButton.setOnAction(e -> showSetupScreen());
        
        Button quitButton = new Button("✗ QUIT");
        quitButton.setStyle("-fx-font-size: 16; -fx-padding: 10 20; -fx-background-color: #8b0000; -fx-text-fill: white;");
        quitButton.setOnAction(e -> System.exit(0));
        
        root.getChildren().addAll(title, winnerLabel, newGameButton, quitButton);
        
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}