import src.model.Card;
import src.model.Hand;
import src.controller.HandEvaluator;
import java.util.ArrayList;

public class HandEvaluatorTest {
    
    public static void main(String[] args) {
        System.out.println("Running Hand Evaluator Tests...\n");
        
        // Test 1: Royal Flush
        Hand royalFlush = new Hand();
        royalFlush.addCard(new Card("A", "Spades", 14));
        royalFlush.addCard(new Card("K", "Spades", 13));
        royalFlush.addCard(new Card("Q", "Spades", 12));
        royalFlush.addCard(new Card("J", "Spades", 11));
        royalFlush.addCard(new Card("10", "Spades", 10));
        System.out.println("Royal Flush: " + HandEvaluator.evaluate(royalFlush));
        System.out.println("Strength: " + HandEvaluator.getHandStrength(royalFlush) + " (should be 10)\n");
        
        // Test 2: Four of a Kind
        Hand fourKind = new Hand();
        fourKind.addCard(new Card("K", "Spades", 13));
        fourKind.addCard(new Card("K", "Hearts", 13));
        fourKind.addCard(new Card("K", "Clubs", 13));
        fourKind.addCard(new Card("K", "Diamonds", 13));
        fourKind.addCard(new Card("2", "Spades", 2));
        System.out.println("Four of a Kind: " + HandEvaluator.evaluate(fourKind));
        System.out.println("Strength: " + HandEvaluator.getHandStrength(fourKind) + " (should be 8)\n");
        
        // Test 3: Full House
        Hand fullHouse = new Hand();
        fullHouse.addCard(new Card("Q", "Spades", 12));
        fullHouse.addCard(new Card("Q", "Hearts", 12));
        fullHouse.addCard(new Card("Q", "Clubs", 12));
        fullHouse.addCard(new Card("5", "Spades", 5));
        fullHouse.addCard(new Card("5", "Hearts", 5));
        System.out.println("Full House: " + HandEvaluator.evaluate(fullHouse));
        System.out.println("Strength: " + HandEvaluator.getHandStrength(fullHouse) + " (should be 7)\n");
        
        // Test 4: Flush
        Hand flush = new Hand();
        flush.addCard(new Card("A", "Hearts", 14));
        flush.addCard(new Card("10", "Hearts", 10));
        flush.addCard(new Card("7", "Hearts", 7));
        flush.addCard(new Card("4", "Hearts", 4));
        flush.addCard(new Card("2", "Hearts", 2));
        System.out.println("Flush: " + HandEvaluator.evaluate(flush));
        System.out.println("Strength: " + HandEvaluator.getHandStrength(flush) + " (should be 6)\n");
        
        // Test 5: Straight
        Hand straight = new Hand();
        straight.addCard(new Card("10", "Spades", 10));
        straight.addCard(new Card("9", "Hearts", 9));
        straight.addCard(new Card("8", "Clubs", 8));
        straight.addCard(new Card("7", "Diamonds", 7));
        straight.addCard(new Card("6", "Spades", 6));
        System.out.println("Straight: " + HandEvaluator.evaluate(straight));
        System.out.println("Strength: " + HandEvaluator.getHandStrength(straight) + " (should be 5)\n");
        
        // Test 6: One Pair
        Hand onePair = new Hand();
        onePair.addCard(new Card("J", "Spades", 11));
        onePair.addCard(new Card("J", "Hearts", 11));
        onePair.addCard(new Card("A", "Clubs", 14));
        onePair.addCard(new Card("8", "Diamonds", 8));
        onePair.addCard(new Card("3", "Spades", 3));
        System.out.println("One Pair: " + HandEvaluator.evaluate(onePair));
        System.out.println("Strength: " + HandEvaluator.getHandStrength(onePair) + " (should be 2)\n");
        
        System.out.println("All tests completed!");
    }
}