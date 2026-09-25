package ma.youcode.lineperm;

import ma.youcode.lineperm.database.DatabaseConnection;
import ma.youcode.lineperm.ui.ConsoleApp;

public class Main {

    public static void main(String[] args) {
        DatabaseConnection databaseConnection = DatabaseConnection.getInstance();
        try {
            ConsoleApp application = new ConsoleApp();
            application.run();
        } finally {
            databaseConnection.closeConnection();
        }
    }
}
