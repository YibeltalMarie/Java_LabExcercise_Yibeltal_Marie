package database;

import java.sql.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:mariadb://localhost:3306/chat_db";
    private static final String USER = "root";
    private static final String PASSWORD = "mot94her";
    
    private static Connection connection = null;
    
    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("org.mariadb.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✓ Database connected successfully!");
            } catch (ClassNotFoundException e) {
                System.err.println("✗ MariaDB JDBC Driver not found!");
                System.err.println("  Make sure mariadb-java-client.jar is in java_libs/ folder");
            } catch (SQLException e) {
                System.err.println("✗ Database connection failed!");
                System.err.println("  Check: Is MariaDB running?");
                System.err.println("  Error: " + e.getMessage());
            }
        }
        return connection;
    }
    
    public static void close() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing database: " + e.getMessage());
            }
        }
    }
    
    // Test connection
    public static boolean testConnection() {
        try {
            return getConnection() != null && !getConnection().isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}