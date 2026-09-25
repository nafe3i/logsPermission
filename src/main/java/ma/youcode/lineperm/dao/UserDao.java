package ma.youcode.lineperm.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import ma.youcode.lineperm.model.User;

public class UserDao extends AbstractDao<User> {

    // private static UserDao userDao;
    // userDao  = new UserDao();
    public boolean save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User ne peut pas etre null .");
        }
        String sql = "INSERT INTO users (login,password_hash) VALUES (?,?) ";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, user.getLogin());
            statement.setString(2, user.getPasswordHash());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erreur SQL lors de la création de l'utilisateur.", e);
        }
    }

    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    User user = new User(result.getInt("id"), result.getString("login"), result.getString("password_hash"));
                    return Optional.of(user);
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "erreur lors de la rechrche de l'utilisateur .", exception
            );
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erreur SQL lors de la suppression de l'utilisateur.", e);
        }

    }

    public Optional<User> findByLogin(String login) {
        String sql = "SELECT * FROM users WHERE login = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, login);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    User user = new User(result.getInt("id"), result.getString("login"), result.getString("password_hash"));
                    return Optional.of(user);
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "erreur lors de la rechrche de l'utilisateur .", exception
            );
        }
    }

    // private boolean comparePassword(User user , String password) {
        
    // }
}
