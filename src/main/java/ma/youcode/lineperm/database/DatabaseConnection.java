package ma.youcode.lineperm.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConnection {

    // public static void main(String[] args) {
    //     DatabaseConnection instance1 = DatabaseConnection.getInstance();
    //     System.out.println(instance1);
    // }
    private static DatabaseConnection instance;
    // private static boolean  createconn = false ;

    private static final String URL = "jdbc:sqlite:database/linePermition.db";

    private final Connection connection;

    private DatabaseConnection() {
        try {
            this.connection = DriverManager.getConnection(URL);

            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Impossible de demarrer la connexion SQLite.", e
            );
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }

        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de SQLite.");
        }
    }
}
