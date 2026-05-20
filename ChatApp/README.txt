========================================
    SIMPLE CHAT APPLICATION
========================================

WHAT THIS APPLICATION DOES:
---------------------------
A real-time chat system where multiple users can:
- Login with username/password
- Send broadcast messages (to everyone)
- Send private messages (to specific users)
- Send files (images, audio, video)
- See online users
- View chat history from database

FOLDER STRUCTURE:
----------------
ChatApp/
  ├── model/        - Data classes (Message, User)
  ├── database/     - Database operations (DAO)
  ├── server/       - Server code (ChatServer, ClientHandler)
  ├── client/       - Client code (ChatClient - JavaFX)
  ├── java_libs/    - External JAR files (MariaDB driver)
  ├── received_files/ - Files received from other users
  ├── compiled/     - .class files (created during compilation)
  └── README.txt    - This file

PREREQUISITES:
-------------
1. Java 8 or higher (with JavaFX - Java 8 includes it)
2. MariaDB installed and running
3. Database already created with users table

DATABASE SETUP (Already Done):
------------------------------
Database: chat_db
Tables: users, messages

Users already created:
  - Alice / alice123
  - Bob / bob123
  - Charlie / charlie123

HOW TO COMPILE:
---------------
cd ChatApp
export CLASSPATH=".:$PWD/java_libs/mariadb-java-client-3.3.0.jar"
javafx-compile -d compiled model/*.java database/*.java server/*.java client/*.java

HOW TO RUN:
-----------
Terminal 1 (Server):
  cd ChatApp
  export CLASSPATH=".:$PWD/java_libs/mariadb-java-client-3.3.0.jar"
  java -cp compiled:$CLASSPATH server.ChatServer

Terminal 2 (Client 1 - Alice):
  cd ChatApp
  export CLASSPATH=".:$PWD/java_libs/mariadb-java-client-3.3.0.jar"
  javafx-run -cp compiled:$CLASSPATH client.ChatClient

Terminal 3 (Client 2 - Bob):
  cd ChatApp
  export CLASSPATH=".:$PWD/java_libs/mariadb-java-client-3.3.0.jar"
  javafx-run -cp compiled:$CLASSPATH client.ChatClient

HOW TO USE THE CLIENT:
----------------------
1. Enter username and password (Alice/alice123, Bob/bob123, Charlie/charlie123)
2. Server: localhost, Port: 5555
3. Click Login
4. Select recipient from dropdown (ALL for broadcast, or specific user)
5. Type message and click Send
6. Click "Send File" to share images/audio/video

TROUBLESHOOTING:
----------------
- "Connection refused": Start server before clients
- "Class not found": Check JAR file exists in java_libs/
- "Database error": Verify MariaDB is running
- Received files appear in received_files/ folder