package src.model;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private List<Player> players;
    private Deck deck;
    private int pot;
    private int currentBet;
    private int anteAmount;
    private int currentPlayerIndex;
    private boolean handInProgress;
    
    public Game(int anteAmount) {
        this.players = new ArrayList<>();
        this.deck = new Deck();
        this.pot = 0;
        this.currentBet = 0;
        this.anteAmount = anteAmount;
        this.currentPlayerIndex = 0;
        this.handInProgress = false;
    }
    
    // ========== PLAYER MANAGEMENT ==========
    
    public void addPlayer(String name, int chips) {
        players.add(new Player(name, chips));
        System.out.println(name + " joined the game with " + chips + " chips!");
    }
    
    public void removePlayer(Player player) {
        players.remove(player);
        System.out.println(player.getName() + " left the game.");
    }
    
    public List<Player> getPlayers() {
        return players;
    }
    
    public int getPlayerCount() {
        return players.size();
    }
    
    // ========== GAME FLOW ==========
    
    public void startNewHand() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("STARTING NEW HAND!");
        System.out.println("=".repeat(50));
        
        // Reset for new hand
        deck = new Deck();
        deck.shuffle();
        pot = 0;
        currentBet = 0;
        handInProgress = true;
        
        // Reset all players
        for (Player p : players) {
            p.resetForNewHand();
        }
        
        // Collect antes
        collectAntes();
        
        // Deal cards
        dealCards();
    }
    
    private void collectAntes() {
        System.out.println("\n--- COLLECTING ANTES ---");
        int totalAnte = 0;
        
        for (Player p : players) {
            if (p.hasEnoughChips(anteAmount)) {
                p.removeChips(anteAmount);
                pot += anteAmount;
                totalAnte += anteAmount;
                System.out.println(p.getName() + " pays " + anteAmount + " ante. Remaining: " + p.getChips());
            } else {
                System.out.println(p.getName() + " doesn't have enough chips and is removed!");
                removePlayer(p);
            }
        }
        
        System.out.println("Total ante collected: " + totalAnte);
        System.out.println("Pot: " + pot + " chips");
    }
    
    private void dealCards() {
        System.out.println("\n--- DEALING CARDS ---");
        for (Player p : players) {
            ArrayList<Card> cardList = deck.dealHand();
            Hand hand = new Hand(cardList);
            p.setHand(hand);
            System.out.println(p.getName() + " receives 5 cards");
        }
    }
    
    public void endHand() {
        handInProgress = false;
        System.out.println("\nHand ended!");
    }
    
    public boolean isHandInProgress() {
        return handInProgress;
    }
    
    // ========== BETTING METHODS ==========
    
    public void addToPot(int amount) {
        pot += amount;
        System.out.println("Added " + amount + " to pot. Pot is now: " + pot);
    }
    
    public void clearPot() {
        pot = 0;
    }
    
    public int getPot() {
        return pot;
    }
    
    public void setCurrentBet(int bet) {
        this.currentBet = bet;
        System.out.println("Current bet is now: " + currentBet);
    }
    
    public int getCurrentBet() {
        return currentBet;
    }
    
    // ========== PLAYER TURN MANAGEMENT ==========
    
    public void setCurrentPlayerIndex(int index) {
        this.currentPlayerIndex = index;
    }
    
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }
    
    public Player getCurrentPlayer() {
        if (currentPlayerIndex >= 0 && currentPlayerIndex < players.size()) {
            return players.get(currentPlayerIndex);
        }
        return null;
    }
    
    public void advanceToNextPlayer() {
        currentPlayerIndex++;
        if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }
    }
    
    // ========== DECK METHODS ==========
    
    public Deck getDeck() {
        return deck;
    }
    
    // ========== UTILITY METHODS ==========
    
    public int getAnteAmount() {
        return anteAmount;
    }
    
    public void printGameState() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("GAME STATE");
        System.out.println("=".repeat(40));
        System.out.println("Pot: " + pot + " chips");
        System.out.println("Current bet: " + currentBet);
        System.out.println("Players: " + players.size());
        System.out.println("-".repeat(40));
        
        for (Player p : players) {
            String status = p.hasFolded() ? "FOLDED" : (p.isActive() ? "ACTIVE" : "INACTIVE");
            System.out.println(p.getName() + ": " + p.getChips() + " chips | " + status);
            if (!p.hasFolded() && p.isActive()) {
                System.out.println("   Hand: " + p.getHand());
            }
        }
        System.out.println("=".repeat(40));
    }
    
    // ========== WINNER DETECTION ==========
    
    public List<Player> getActivePlayers() {
        List<Player> active = new ArrayList<>();
        for (Player p : players) {
            if (!p.hasFolded() && p.isActive()) {
                active.add(p);
            }
        }
        return active;
    }
    
    public boolean isOnlyOnePlayerRemaining() {
        return getActivePlayers().size() == 1;
    }
    
    public Player getLastActivePlayer() {
        List<Player> active = getActivePlayers();
        if (active.size() == 1) {
            return active.get(0);
        }
        return null;
    }
    
    // ========== RESET METHODS ==========
    
    public void resetGame() {
        pot = 0;
        currentBet = 0;
        currentPlayerIndex = 0;
        handInProgress = false;
        
        for (Player p : players) {
            p.resetForNewHand();
        }
        
        deck = new Deck();
    }
    
    // ========== STRING REPRESENTATION ==========
    
    @Override
    public String toString() {
        return "Game{players=" + players.size() + ", pot=" + pot + ", currentBet=" + currentBet + "}";
    }
}