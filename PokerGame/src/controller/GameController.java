package src.controller;

import src.model.Game;
import src.model.Player;
import src.model.Hand;
import src.model.Card;
import src.view.PokerTable;
import java.util.List;
import java.util.ArrayList;

/**
 * GameController acts as the intermediary between the Model (Game data)
 * and the View (JavaFX UI). It handles:
 * - Game flow control
 * - Turn management
 * - Betting logic
 * - Player actions
 */
public class GameController {
    
    private Game game;
    private PokerTable view;
    private int currentPlayerIndex;
    private int currentBettingRound;
    private boolean gameInProgress;
    
    // Betting round constants
    public static final int ROUND_FIRST_BET = 1;
    public static final int ROUND_DRAW = 2;
    public static final int ROUND_FINAL_BET = 3;
    public static final int ROUND_SHOWDOWN = 4;
    
    public GameController(Game game, PokerTable view) {
        this.game = game;
        this.view = view;
        this.currentPlayerIndex = 0;
        this.currentBettingRound = ROUND_FIRST_BET;
        this.gameInProgress = true;
    }
    
    // ========== GAME FLOW CONTROL ==========
    
    /**
     * Start a new hand of poker
     */
    public void startNewHand() {
        game.startNewHand();
        currentPlayerIndex = 0;
        currentBettingRound = ROUND_FIRST_BET;
        
        // Notify view to update display
        view.updateGameState(game);
        view.showWaitingScreen(getCurrentPlayer());
    }
    
    /**
     * Move to the next player's turn
     */
    public void nextPlayer() {
        currentPlayerIndex++;
        
        if (currentPlayerIndex >= game.getPlayerCount()) {
            // End of round - move to next phase
            advanceToNextRound();
        } else {
            // Next player's turn
            view.showWaitingScreen(getCurrentPlayer());
        }
    }
    
    /**
     * Advance to the next phase of the game
     */
    private void advanceToNextRound() {
        switch (currentBettingRound) {
            case ROUND_FIRST_BET:
                // First betting round complete, move to draw phase
                currentBettingRound = ROUND_DRAW;
                currentPlayerIndex = 0;
                view.showDrawScreen(game, this);
                break;
                
            case ROUND_DRAW:
                // Draw phase complete, move to final betting
                currentBettingRound = ROUND_FINAL_BET;
                currentPlayerIndex = 0;
                view.showWaitingScreen(getCurrentPlayer());
                break;
                
            case ROUND_FINAL_BET:
                // Final betting complete, show showdown
                currentBettingRound = ROUND_SHOWDOWN;
                determineWinner();
                break;
                
            case ROUND_SHOWDOWN:
                // Game over, ready for new hand
                view.showGameOverScreen(game);
                break;
        }
    }
    
    /**
     * Process a player's draw decision (which cards to discard)
     */
    public void processDraw(Player player, List<Integer> cardIndicesToDiscard) {
        Hand playerHand = player.getHand();
        
        // Get the cards to discard
        ArrayList<Card> discardedCards = new ArrayList<>();
        for (int index : cardIndicesToDiscard) {
            if (index >= 0 && index < playerHand.size()) {
                discardedCards.add(playerHand.getCard(index));
            }
        }
        
        // Remove discarded cards from hand
        for (Card card : discardedCards) {
            playerHand.removeCard(card);
            game.getDeck().discard(card);
        }
        
        // Draw new cards
        int cardsToDraw = discardedCards.size();
        for (int i = 0; i < cardsToDraw; i++) {
            Card newCard = game.getDeck().drawCard();
            if (newCard != null) {
                playerHand.addCard(newCard);
            }
        }
        
        // Sort the hand after drawing
        playerHand.sortCardsByValue();
        
        // Log the action
        System.out.println(player.getName() + " discarded " + cardsToDraw + " card(s) and drew " + cardsToDraw);
        
        // Move to next player
        currentPlayerIndex++;
        
        if (currentPlayerIndex >= game.getPlayerCount()) {
            advanceToNextRound();
        } else {
            view.showDrawScreen(game, this);
        }
    }
    
    // ========== BETTING ACTIONS ==========
    
    /**
     * Player performs a CHECK (pass without betting)
     */
    public void playerCheck(Player player) {
        System.out.println(player.getName() + " checks");
        
        // Check if betting round should end
        if (isBettingRoundComplete()) {
            advanceToNextRound();
        } else {
            nextPlayer();
        }
    }
    
    /**
     * Player performs a BET (initiate betting)
     */
    public void playerBet(Player player, int amount) {
        if (amount > player.getChips()) {
            amount = player.getChips(); // All-in
        }
        
        player.removeChips(amount);
        player.addToCurrentBet(amount);
        game.addToPot(amount);
        game.setCurrentBet(amount);
        
        System.out.println(player.getName() + " bets " + amount);
        
        // Reset other players' current bet status
        resetOtherPlayersCurrentBet(player);
        
        nextPlayer();
    }
    
    /**
     * Player performs a CALL (match current bet)
     */
    public void playerCall(Player player) {
        int currentBet = game.getCurrentBet();
        int amountToCall = currentBet - player.getCurrentBet();
        
        if (amountToCall > player.getChips()) {
            amountToCall = player.getChips(); // All-in
        }
        
        if (amountToCall > 0) {
            player.removeChips(amountToCall);
            player.addToCurrentBet(amountToCall);
            game.addToPot(amountToCall);
            System.out.println(player.getName() + " calls " + amountToCall);
        } else {
            System.out.println(player.getName() + " checks");
        }
        
        nextPlayer();
    }
    
    /**
     * Player performs a RAISE (increase the bet)
     */
    public void playerRaise(Player player, int newTotalBet) {
        int amountToRaise = newTotalBet - player.getCurrentBet();
        
        if (amountToRaise > player.getChips()) {
            amountToRaise = player.getChips();
            newTotalBet = player.getCurrentBet() + amountToRaise;
        }
        
        player.removeChips(amountToRaise);
        player.addToCurrentBet(amountToRaise);
        game.addToPot(amountToRaise);
        game.setCurrentBet(newTotalBet);
        
        System.out.println(player.getName() + " raises to " + newTotalBet);
        
        // Reset other players' current bet status
        resetOtherPlayersCurrentBet(player);
        
        nextPlayer();
    }
    
    /**
     * Player FOLDS (quits the current hand)
     */
    public void playerFold(Player player) {
        player.fold();
        System.out.println(player.getName() + " folds!");
        
        // Check if only one player remains
        int activePlayers = countActivePlayers();
        if (activePlayers == 1) {
            // Only one player left - they win automatically
            awardPotToLastActivePlayer();
        } else if (activePlayers == 0) {
            // Everyone folded - shouldn't happen
            System.out.println("Error: No active players!");
        } else {
            nextPlayer();
        }
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Get the current player
     */
    public Player getCurrentPlayer() {
        if (currentPlayerIndex < game.getPlayerCount()) {
            return game.getPlayers().get(currentPlayerIndex);
        }
        return null;
    }
    
    /**
     * Get current betting round
     */
    public int getCurrentBettingRound() {
        return currentBettingRound;
    }
    
    /**
     * Count how many players are still active (haven't folded)
     */
    private int countActivePlayers() {
        int count = 0;
        for (Player p : game.getPlayers()) {
            if (!p.hasFolded() && p.isActive()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Reset current bet for all players except the one who just raised
     */
    private void resetOtherPlayersCurrentBet(Player exceptionPlayer) {
        for (Player p : game.getPlayers()) {
            if (p != exceptionPlayer && !p.hasFolded()) {
                // They need to match the new bet
                // Current bet tracking handled in individual actions
            }
        }
    }
    
    /**
     * Check if the betting round is complete
     */
    private boolean isBettingRoundComplete() {
        int currentBet = game.getCurrentBet();
        
        for (Player p : getActivePlayers()) {
            if (p.getCurrentBet() < currentBet && !p.hasFolded()) {
                return false; // Some player hasn't matched the bet yet
            }
        }
        return true;
    }
    
    /**
     * Get list of active players (haven't folded)
     */
    public List<Player> getActivePlayers() {
        List<Player> active = new ArrayList<>();
        for (Player p : game.getPlayers()) {
            if (!p.hasFolded() && p.isActive()) {
                active.add(p);
            }
        }
        return active;
    }
    
    /**
     * If only one player remains active, they win the pot automatically
     */
    private void awardPotToLastActivePlayer() {
        List<Player> activePlayers = getActivePlayers();
        if (activePlayers.size() == 1) {
            Player winner = activePlayers.get(0);
            int potAmount = game.getPot();
            winner.addChips(potAmount);
            game.clearPot();
            
            System.out.println(winner.getName() + " wins " + potAmount + " chips by default!");
            view.showWinner(winner, potAmount);
        }
    }
    
    /**
     * Determine the winner at showdown
     */
    private void determineWinner() {
        List<Player> activePlayers = getActivePlayers();
        
        if (activePlayers.size() == 1) {
            awardPotToLastActivePlayer();
            return;
        }
        
        // Find the player with the best hand
        Player winner = activePlayers.get(0);
        int bestStrength = HandEvaluator.getHandStrength(winner.getHand());
        String bestHand = HandEvaluator.evaluate(winner.getHand());
        
        for (Player p : activePlayers) {
            int strength = HandEvaluator.getHandStrength(p.getHand());
            if (strength > bestStrength) {
                bestStrength = strength;
                bestHand = HandEvaluator.evaluate(p.getHand());
                winner = p;
            }
        }
        
        // Award pot to winner
        int potAmount = game.getPot();
        winner.addChips(potAmount);
        game.clearPot();
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🏆 " + winner.getName() + " wins " + potAmount + " chips with " + bestHand + "! 🏆");
        System.out.println("=".repeat(50));
        
        // Show results in view
        view.showWinner(winner, potAmount);
        view.showAllHands(activePlayers);
    }
    
    /**
     * Check if the game is still in progress
     */
    public boolean isGameInProgress() {
        return gameInProgress;
    }
    
    /**
     * End the game
     */
    public void endGame() {
        gameInProgress = false;
        System.out.println("Game ended!");
    }
    
    /**
     * Get the current pot amount
     */
    public int getPot() {
        return game.getPot();
    }
    
    /**
     * Get current bet amount
     */
    public int getCurrentBet() {
        return game.getCurrentBet();
    }
}