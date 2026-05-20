package src.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Hand {
    private ArrayList<Card> cards;
    private String handRank;      // "Royal Flush", "Pair", etc.
    private int handStrength;     // 1-10 (10 = best)
    
    public Hand() {
        this.cards = new ArrayList<>();
        this.handRank = "High Card";
        this.handStrength = 1;
    }
    
    public Hand(ArrayList<Card> cards) {
        this.cards = new ArrayList<>(cards);
        sortCardsByValue();
    }
    
    // Sort cards from highest to lowest value
    public void sortCardsByValue() {
        cards.sort((c1, c2) -> Integer.compare(c2.getValue(), c1.getValue()));
    }
    
    // Add a card to the hand
    public void addCard(Card card) {
        cards.add(card);
        sortCardsByValue();
    }
    
    // Remove a card from the hand
    public void removeCard(Card card) {
        cards.remove(card);
    }
    
    // Remove cards at specific indices (for drawing)
    public void removeCardsAtIndices(ArrayList<Integer> indices) {
        // Sort in reverse to avoid index shifting
        indices.sort(Collections.reverseOrder());
        for (int index : indices) {
            if (index >= 0 && index < cards.size()) {
                cards.remove(index);
            }
        }
    }
    
    // Get all cards
    public ArrayList<Card> getCards() {
        return new ArrayList<>(cards);
    }
    
    // Get number of cards
    public int size() {
        return cards.size();
    }
    
    // Check if hand is empty
    public boolean isEmpty() {
        return cards.isEmpty();
    }
    
    // Clear the hand
    public void clear() {
        cards.clear();
    }
    
    // Get card at specific position
    public Card getCard(int index) {
        if (index >= 0 && index < cards.size()) {
            return cards.get(index);
        }
        return null;
    }
    
    // Get hand rank (e.g., "Flush", "Pair")
    public String getHandRank() {
        return handRank;
    }
    
    public void setHandRank(String handRank) {
        this.handRank = handRank;
    }
    
    public int getHandStrength() {
        return handStrength;
    }
    
    public void setHandStrength(int handStrength) {
        this.handStrength = handStrength;
    }
    
    // String representation of hand
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Card card : cards) {
            sb.append(card).append(" ");
        }
        return sb.toString().trim();
    }
}