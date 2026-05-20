package src.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private ArrayList<Card> cards;
    private ArrayList<Card> discardPile;
    
    public Deck() {
        cards = new ArrayList<>();
        discardPile = new ArrayList<>();
        initializeDeck();
    }
    
    private void initializeDeck() {
        // Define ranks, suits, and values
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        int[] values = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
        String[] suits = {"Spades", "Hearts", "Clubs", "Diamonds"};
        String[] suitSymbols = {"♠️", "♥️", "♣️", "♦️"};
        
        // Create all 52 cards
        for (String suit : suits) {
            for (int i = 0; i < ranks.length; i++) {
                Card card = new Card(ranks[i], suit, values[i]);
                cards.add(card);
            }
        }
    }
    
    // Shuffle the deck using Collections.shuffle()
    public void shuffle() {
        Collections.shuffle(cards);
        System.out.println("Deck shuffled! " + cards.size() + " cards remaining.");
    }
    
    // Draw a card from the top of the deck
    public Card drawCard() {
        if (cards.isEmpty()) {
            System.out.println("Deck is empty! Reshuffling discard pile...");
            reshuffle();
        }
        
        if (cards.isEmpty()) {
            return null;
        }
        
        return cards.remove(0);
    }
    
    // Add a card to discard pile
    public void discard(Card card) {
        discardPile.add(card);
    }
    
    // Discard multiple cards
    public void discardCards(List<Card> cardsToDiscard) {
        discardPile.addAll(cardsToDiscard);
        cards.removeAll(cardsToDiscard);
    }
    
    // Reshuffle discard pile back into deck
    private void reshuffle() {
        cards.addAll(discardPile);
        discardPile.clear();
        shuffle();
    }
    
    // Deal 5 cards to a player
    public ArrayList<Card> dealHand() {
        ArrayList<Card> hand = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            hand.add(drawCard());
        }
        return hand;
    }
    
    // Check how many cards are left
    public int remainingCards() {
        return cards.size();
    }
    
    public int getDiscardCount() {
        return discardPile.size();
    }
    
    // For debugging
    public void printDeck() {
        for (Card card : cards) {
            System.out.print(card + " ");
        }
        System.out.println();
    }
}