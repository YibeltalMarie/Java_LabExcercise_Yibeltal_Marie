package src.controller;

import src.model.Card;
import src.model.Hand;
import java.util.*;

public class HandEvaluator {
    
    // Evaluate a hand and return its rank
    public static String evaluate(Hand hand) {
        ArrayList<Card> cards = hand.getCards();
        
        if (isRoyalFlush(cards)) return "Royal Flush";
        if (isStraightFlush(cards)) return "Straight Flush";
        if (isFourOfAKind(cards)) return "Four of a Kind";
        if (isFullHouse(cards)) return "Full House";
        if (isFlush(cards)) return "Flush";
        if (isStraight(cards)) return "Straight";
        if (isThreeOfAKind(cards)) return "Three of a Kind";
        if (isTwoPair(cards)) return "Two Pair";
        if (isOnePair(cards)) return "One Pair";
        return "High Card";
    }
    
    // Get hand strength (10 = best, 1 = worst)
    public static int getHandStrength(Hand hand) {
        String rank = evaluate(hand);
        return switch(rank) {
            case "Royal Flush" -> 10;
            case "Straight Flush" -> 9;
            case "Four of a Kind" -> 8;
            case "Full House" -> 7;
            case "Flush" -> 6;
            case "Straight" -> 5;
            case "Three of a Kind" -> 4;
            case "Two Pair" -> 3;
            case "One Pair" -> 2;
            default -> 1;
        };
    }
    
    private static boolean isRoyalFlush(ArrayList<Card> cards) {
        if (!isFlush(cards)) return false;
        ArrayList<Integer> values = getValues(cards);
        return values.containsAll(Arrays.asList(10, 11, 12, 13, 14));
    }
    
    private static boolean isStraightFlush(ArrayList<Card> cards) {
        return isFlush(cards) && isStraight(cards);
    }
    
    private static boolean isFourOfAKind(ArrayList<Card> cards) {
        Map<Integer, Integer> valueCounts = getValueCounts(cards);
        return valueCounts.containsValue(4);
    }
    
    private static boolean isFullHouse(ArrayList<Card> cards) {
        Map<Integer, Integer> valueCounts = getValueCounts(cards);
        return valueCounts.containsValue(3) && valueCounts.containsValue(2);
    }
    
    private static boolean isFlush(ArrayList<Card> cards) {
        String firstSuit = cards.get(0).getSuit();
        for (Card card : cards) {
            if (!card.getSuit().equals(firstSuit)) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean isStraight(ArrayList<Card> cards) {
        ArrayList<Integer> values = new ArrayList<>(getValues(cards));
        Collections.sort(values);
        
        // Check normal straight
        for (int i = 0; i < values.size() - 1; i++) {
            if (values.get(i + 1) != values.get(i) + 1) {
                // Check Ace-low straight (A,2,3,4,5)
                if (values.equals(Arrays.asList(2, 3, 4, 5, 14))) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }
    
    private static boolean isThreeOfAKind(ArrayList<Card> cards) {
        Map<Integer, Integer> valueCounts = getValueCounts(cards);
        return valueCounts.containsValue(3);
    }
    
    private static boolean isTwoPair(ArrayList<Card> cards) {
        Map<Integer, Integer> valueCounts = getValueCounts(cards);
        int pairCount = 0;
        for (int count : valueCounts.values()) {
            if (count == 2) pairCount++;
        }
        return pairCount == 2;
    }
    
    private static boolean isOnePair(ArrayList<Card> cards) {
        Map<Integer, Integer> valueCounts = getValueCounts(cards);
        return valueCounts.containsValue(2);
    }
    
    private static Map<Integer, Integer> getValueCounts(ArrayList<Card> cards) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (Card card : cards) {
            int value = card.getValue();
            counts.put(value, counts.getOrDefault(value, 0) + 1);
        }
        return counts;
    }
    
    private static ArrayList<Integer> getValues(ArrayList<Card> cards) {
        ArrayList<Integer> values = new ArrayList<>();
        for (Card card : cards) {
            values.add(card.getValue());
        }
        return values;
    }
}