package university.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Responsibility: Provide database connections.
 * All DB credentials live here — nowhere else.
 */
public class DatabaseConnection {

    private static final String URL      = "jdbc:mariadb://localhost:3306/university_db";
    private static final String DB_USER  = "root";
    private static final String DB_PASS  = "mot94her";

    static {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            System.out.println("✓ MariaDB driver loaded.");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ MariaDB driver not found: " + e.getMessage());
        }
    }

    /** Returns a fresh connection. Caller is responsible for closing it. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASS);
    }
}
