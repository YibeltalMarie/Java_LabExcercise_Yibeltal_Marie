package src.model;

import java.util.ArrayList;

public class Player {
    private String name;
    private int chips;
    private Hand hand;
    private boolean isActive;      // Still in current hand?
    private boolean hasFolded;
    private int currentBet;        // Chips bet in current round
    
    public Player(String name, int startingChips) {
        this.name = name;
        this.chips = startingChips;
        this.hand = new Hand();
        this.isActive = true;
        this.hasFolded = false;
        this.currentBet = 0;
    }
    
    // Getters
    public String getName() { return name; }
    public int getChips() { return chips; }
    public Hand getHand() { return hand; }
    public boolean isActive() { return isActive; }
    public boolean hasFolded() { return hasFolded; }
    public int getCurrentBet() { return currentBet; }
    
    // Chip management
    public void addChips(int amount) { 
        chips += amount; 
    }
    
    public boolean removeChips(int amount) {
        if (chips >= amount) {
            chips -= amount;
            return true;
        }
        return false;
    }
    
    public void addToCurrentBet(int amount) {
        currentBet += amount;
    }
    
    // Hand management
    public void setHand(Hand newHand) {
        this.hand = newHand;
    }
    
    public void clearHand() {
        hand.clear();
    }
    
    // Game actions
    public void fold() {
        hasFolded = true;
        isActive = false;
        clearHand();
    }
    
    public void resetForNewHand() {
        hand.clear();
        isActive = true;
        hasFolded = false;
        currentBet = 0;
    }
    
    // Status methods
    public boolean hasEnoughChips(int amount) {
        return chips >= amount;
    }
    
    // String representation
    public String toString() {
        return name + " - Chips: " + chips + (isActive ? " (Active)" : " (Folded)");
    }
}