package ma.youcode.lineperm;

import ma.youcode.lineperm.model.Log;
import ma.youcode.lineperm.ui.ConsoleApp;

public class Main {

    public static void main(String[] args) {

        Log log = new Log("amine", "lecture", "test.txt", true);
        System.out.println(log.toString());
        ConsoleApp application = new ConsoleApp();
        application.run();
    }
}
