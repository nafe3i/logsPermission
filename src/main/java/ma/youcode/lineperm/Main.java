package ma.youcode.lineperm;

import java.sql.Connection;
import ma.youcode.lineperm.database.DatabaseConnection;
import ma.youcode.lineperm.ui.ConsoleApp;

public class Main {

    public static void main(String[] args) {
        DatabaseConnection databaseConnection = DatabaseConnection.getInstance();
        Connection connection = databaseConnection.getConnection();
        System.out.println(databaseConnection +
         "==============================="+connection);
        // Log log = new Log("amine", "lecture", "test.txt", true);
        // System.out.println(log.toString());
        ConsoleApp application = new ConsoleApp();
        application.run();
    }
}
