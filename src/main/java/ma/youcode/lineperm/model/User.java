package ma.youcode.lineperm.model;

/**
 * Represente un utilisateur en memoire.
 */
public class User {

    private int id;
    private final String login;
    private final String passwordHash;

    public User(String login, String passwordHash) {
        this.login = login;
        this.passwordHash = passwordHash;
    }

    public User(int id, String login, String passwordHash) {
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
    }

    public String getLogin() {
        return login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public int getId() {
        return id;
    }
}
