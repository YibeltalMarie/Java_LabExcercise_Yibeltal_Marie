# Java Projects

A collection of four Java desktop applications built with JavaFX, covering game development, networking, GUI design, and distributed systems.

---

## Projects Overview

| Project | Description | Key Technology |
|---|---|---|
| [PokerGame](#1-pokergame) | 5-card draw poker with a graphical table | JavaFX, MVC |
| [ChatApp](#2-chatapp) | Real-time multi-user chat with file sharing | Java Sockets, JavaFX, MariaDB |
| [NotepadApp](#3-notepadapp) | Feature-rich text editor | JavaFX, Maven |
| [UniversityRMI](#4-universityrmi) | University management system over RMI | Java RMI, JavaFX, MariaDB |

---

## 1. PokerGame

A fully playable 5-card draw poker game with a JavaFX graphical interface.

### Architecture

The project follows an MVC pattern:

```
PokerGame/src/
├── model/
│   ├── Card.java          — Represents a single playing card (rank, suit, value)
│   ├── Deck.java          — 52-card deck with shuffle, deal, and discard logic
│   ├── Hand.java          — A player's hand of cards, sorted by value
│   └── Player.java        — Player state: chips, hand, fold/active status
├── controller/
│   ├── Game.java          — Core game loop: antes, dealing, betting rounds, turn management
│   ├── GameController.java — Bridges the model and the JavaFX view
│   └── HandEvaluator.java  — Detects all hand ranks from High Card to Royal Flush
└── view/
    ├── PokerTable.java    — Main JavaFX game table UI
    └── CardImage.java     — Renders individual card visuals
```

### Features

- Full 5-card draw gameplay with ante, first betting round, draw phase, and final betting round
- Hand evaluation supporting all 10 standard poker hands
- Chip management and pot tracking
- Fold, call, raise, and draw actions

### Requirements

- Java 8+ with JavaFX included (Java 8 bundles JavaFX by default)

---

## 2. ChatApp

A real-time chat application with a server/client architecture, persistent message history, and multimedia file sharing.

### Architecture

```
ChatApp/
├── model/
│   ├── Message.java       — Serializable message (text, image, audio, video, file)
│   └── User.java          — User credentials and identity
├── database/
│   ├── DatabaseConnection.java — MariaDB connection singleton
│   ├── UserDAO.java            — User authentication and lookup
│   └── MessageDAO.java         — Message persistence and history retrieval
├── server/
│   ├── ChatServer.java    — Listens on port 5555, spawns a ClientHandler per connection
│   └── ClientHandler.java — Handles one connected client: auth, routing, file transfer
└── client/
    └── ChatClient.java    — JavaFX client with login, chat view, and file sending
```

### Features

- Multi-user real-time messaging over TCP sockets
- Broadcast messages (to all users) and private messages (to a specific user)
- File sharing: images, audio, and video (files saved to `received_files/`)
- Persistent chat history stored in MariaDB
- Online user list displayed in the client

### Requirements

- Java 8+ (with JavaFX)
- MariaDB running locally
- `java_libs/mariadb-java-client-3.3.0.jar` present

### Database Setup

Database name: `chat_db`  
Required tables: `users`, `messages`

Pre-seeded accounts:

| Username | Password  |
|----------|-----------|
| Alice    | alice123  |
| Bob      | bob123    |
| Charlie  | charlie123 |

### Running

```bash
# 1. Compile
cd ChatApp
export CLASSPATH=".:$PWD/java_libs/mariadb-java-client-3.3.0.jar"
javafx-compile -d compiled model/*.java database/*.java server/*.java client/*.java

# 2. Start the server (Terminal 1)
java -cp compiled:$CLASSPATH server.ChatServer

# 3. Start a client (Terminal 2, 3, ...)
javafx-run -cp compiled:$CLASSPATH client.ChatClient
```

Then enter credentials and `localhost:5555` in the login screen.

---

## 3. NotepadApp

A desktop text editor built with JavaFX and Maven, modelled after a classic Notepad experience.

### Architecture

```
NotepadApp/src/main/java/com/notepad/
├── Main.java               — Entry point, delegates to NotepadApp
├── NotepadApp.java         — JavaFX Application: wires all components together
├── EditorPane.java         — The main text area
├── StatusBar.java          — Shows line/column count and character count
├── FileManager.java        — New, Open, Save, Save As, and unsaved-changes guard
├── MenuBarBuilder.java     — Builds the full menu bar with keyboard shortcuts
├── FindReplaceDialog.java  — Find and Find & Replace dialog
└── FontChooserDialog.java  — Font family, style, and size picker
```

### Features

- File operations: New, Open, Save, Save As with unsaved-changes confirmation
- Edit operations: Cut, Copy, Paste, Select All
- Find and Find & Replace with case-insensitive search
- Font chooser (family, style, size)
- Word wrap toggle
- Live status bar showing line, column, and character count

### Requirements

- Java 21+
- JavaFX 21.0.2 (resolved automatically via Maven)

### Build & Run

```bash
cd NotepadApp
mvn javafx:run
```

---

## 4. UniversityRMI

A university management system using Java RMI so the JavaFX client communicates with the server over the network without any direct database access from the client side.

### Architecture

```
UniversityRMI/src/university/
├── common/
│   ├── UniversityService.java  — RMI Remote interface (shared contract)
│   ├── StudentDTO.java         — Serializable student data transfer object
│   └── TeacherDTO.java         — Serializable teacher data transfer object
├── server/
│   ├── ServerMain.java             — Starts RMI registry on port 1099 and binds the service
│   └── UniversityServiceImpl.java  — Implements the remote interface; calls the DAOs
├── db/
│   ├── DatabaseConnection.java — MariaDB connection factory
│   ├── StudentDAO.java         — Student CRUD operations
│   └── TeacherDAO.java         — Teacher CRUD operations
└── client/
    └── UniversityClientApp.java — JavaFX GUI; calls only UniversityService via RMI
```

### Features

- Add and list students (with department, section, and year)
- Add and list teachers
- Clean separation: the client has zero database dependencies — all data access goes through the RMI interface
- JavaFX table views for browsing records
- Live connection status indicator with a Retry button

### Requirements

- Java 21+ with JavaFX
- MariaDB running locally with a `university_db` database
- `java_libs/mariadb-java-client-3.3.0.jar` present

### Running

A convenience script is provided:

```bash
cd UniversityRMI
chmod +x run.sh

./run.sh compile   # Compile all sources into out/
./run.sh server    # Start the RMI server (do this first)
./run.sh client    # Launch the JavaFX client
```

The server binds to `localhost:1099` under the name `UniversityService`.

---

## Common Prerequisites

All four projects require Java with JavaFX. The ChatApp and UniversityRMI projects additionally require a local MariaDB instance. The database credentials used across both projects are:

- **Host:** `localhost:3306`
- **User:** `root`
- **Password:** `mot94her`

> Make sure to update the credentials in `DatabaseConnection.java` if your setup differs.