package src.view;

import src.model.Card;
import src.model.Hand;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.util.ArrayList;

public class CardImage {
    
    // Convert a card to a visual representation
    public static VBox createCardFace(Card card) {
        VBox cardBox = new VBox();
        cardBox.setAlignment(Pos.CENTER);
        cardBox.setPrefSize(100, 140);
        cardBox.setStyle("-fx-background-color: white; " +
                         "-fx-border-color: black; " +
                         "-fx-border-width: 2; " +
                         "-fx-background-radius: 10; " +
                         "-fx-border-radius: 10;");
        
        // Set color for red suits
        String suitColor = (card.getSuit().equals("Hearts") || card.getSuit().equals("Diamonds")) 
                           ? "#ff0000" : "#000000";
        
        Label cardLabel = new Label(card.toString());
        cardLabel.setStyle("-fx-font-size: 36; -fx-text-fill: " + suitColor + ";");
        cardBox.getChildren().add(cardLabel);
        
        return cardBox;
    }
    
    // Create a card back (face down)
    public static VBox createCardBack() {
        VBox cardBox = new VBox();
        cardBox.setAlignment(Pos.CENTER);
        cardBox.setPrefSize(100, 140);
        cardBox.setStyle("-fx-background-color: #2c3e50; " +
                         "-fx-border-color: #ecf0f1; " +
                         "-fx-border-width: 2; " +
                         "-fx-background-radius: 10; " +
                         "-fx-border-radius: 10;");
        
        Label backLabel = new Label("🃟");
        backLabel.setStyle("-fx-font-size: 48; -fx-text-fill: white;");
        cardBox.getChildren().add(backLabel);
        
        return cardBox;
    }
    
    // Get card views for an entire hand (face up)
    public static ArrayList<VBox> getCardViews(Hand hand) {
        ArrayList<VBox> cardViews = new ArrayList<>();
        for (Card card : hand.getCards()) {
            cardViews.add(createCardFace(card));
        }
        return cardViews;
    }

    // Add these methods to src/view/CardImage.java

    public static VBox createSelectableCardFace(Card card, int index, PokerTable pokerTable) {
        VBox cardBox = new VBox();
        cardBox.setAlignment(Pos.CENTER);
        cardBox.setPrefSize(100, 140);
        cardBox.setStyle(getUnselectedCardStyle());
        cardBox.setUserData(index);
        
        String suitColor = (card.getSuit().equals("Hearts") || card.getSuit().equals("Diamonds")) 
                        ? "#ff0000" : "#000000";
        
        Label rankLabel = new Label(card.getRank());
        rankLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: " + suitColor + ";");
        
        Label suitLabel = new Label(getSuitSymbol(card.getSuit()));
        suitLabel.setStyle("-fx-font-size: 48; -fx-text-fill: " + suitColor + ";");
        
        cardBox.getChildren().addAll(rankLabel, suitLabel);
        
        cardBox.setOnMouseClicked(e -> {
            pokerTable.toggleCardSelection(index, cardBox);
        });
        
        return cardBox;
    }

    public static String getUnselectedCardStyle() {
        return "-fx-background-color: white; " +
            "-fx-border-color: black; " +
            "-fx-border-width: 2; " +
            "-fx-background-radius: 10; " +
            "-fx-border-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 2, 2);";
    }

    public static String getSelectedCardStyle() {
        return "-fx-background-color: #90EE90; " +
            "-fx-border-color: #ffd700; " +
            "-fx-border-width: 4; " +
            "-fx-background-radius: 10; " +
            "-fx-border-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(255,215,0,0.5), 10, 0, 0, 0);";
    }

    private static String getSuitSymbol(String suit) {
        switch(suit) {
            case "Spades": return "♠️";
            case "Hearts": return "♥️";
            case "Clubs": return "♣️";
            case "Diamonds": return "♦️";
            default: return "?";
        }
    }
}