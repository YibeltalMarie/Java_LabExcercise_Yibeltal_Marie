package src.model;

import java.util.Objects;

public class Card {
    // Card properties
    private final String rank;    // 2,3,4,5,6,7,8,9,10,J,Q,K,A
    private final String suit;    // ♠️ ♥️ ♣️ ♦️
    private final int value;      // 2-14 for comparing (Ace=14)
    
    // Constructor
    public Card(String rank, String suit, int value) {
        this.rank = rank;
        this.suit = suit;
        this.value = value;
    }
    
    // Getters
    public String getRank() { return rank; }
    public String getSuit() { return suit; }
    public int getValue() { return value; }
    
    // Get card as string (e.g., "A♠️")
    public String toString() {
        return rank + suit;
    }
    
    // Get HTML/Unicode representation for JavaFX
    public String toUnicode() {
        // Map suits to Unicode symbols
        String suitSymbol = switch(suit) {
            case "Spades" -> "♠️";
            case "Hearts" -> "♥️";
            case "Clubs" -> "♣️";
            case "Diamonds" -> "♦️";
            default -> suit;
        };
        return rank + suitSymbol;
    }
    
    // For comparing cards
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return value == card.value && Objects.equals(rank, card.rank) && Objects.equals(suit, card.suit);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(rank, suit, value);
    }
}