# ♠️ Poker Game - Five Card Draw ♥️

A professional implementation of Five-Card Draw Poker using Java Collections and JavaFX.

## Features
- 5-Card Draw poker rules
- Hot seat (pass-and-play) mode for multiple players
- Hand ranking evaluation (Royal Flush to High Card)
- Java Collections used for deck, hand, and game logic
- JavaFX graphical user interface

## Requirements
- Java 26 or higher
- JavaFX 24 (system installed)
- Your custom aliases: `javafx-compile` and `javafx-run`

## Project Structure

PokerGame/
├── src/
│ ├── model/ # Data classes (Card, Deck, Hand, Player, Game)
│ ├── controller/ # Game logic (HandEvaluator)
│ └── view/ # JavaFX UI (PokerTable, CardImage)
├── resources/ # Images and assets
├── test/ # Unit tests
└── README.md # This file



## How to Run

```bash
cd ~/Work/Java-Project/PokerGame

# Compile all Java files
javafx-compile src/model/*.java src/controller/*.java src/view/*.java

# Run the game
javafx-run src.view.PokerTable


How to Play
Game starts with 3 players: Alice, Bob, Charlie

Each player pays 10 chip ante

Each player receives 5 cards

Hot Seat Mode: Pass computer to the player whose turn it is

Player clicks "Show My Cards" to see their hand

Player clicks "End Turn" when done

After all players have taken turns, hands are evaluated

Best hand wins!

Hand Rankings (Best to Worst)
Royal Flush (10)

Straight Flush (9)

Four of a Kind (8)

Full House (7)

Flush (6)

Straight (5)

Three of a Kind (4)

Two Pair (3)

One Pair (2)

High Card (1)