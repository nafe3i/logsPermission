package ma.youcode.lineperm.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import ma.youcode.lineperm.model.Log;

/**
 * Acces SQL aux evenements d'audit et a leurs statistiques.
 */
public class LogDao extends AbstractDao<Log> {

    @Override
    public boolean save(Log log) {
        if (log == null) {
            throw new IllegalArgumentException("Le log ne peut pas etre null.");
        }

        String sql = """
                INSERT INTO logs (usId, action, fichier, resultat)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, log.getUtilisateurId());
            statement.setString(2, log.getAction());
            statement.setString(3, log.getFichier());
            statement.setString(4, log.isResultatOk() ? "OK" : "REFUSE");

            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur SQL lors de la creation du log.", e);
        }
    }

    @Override
    public Optional<Log> findById(int id) {
        String sql = """
                SELECT id, usId, action, fichier, resultat
                FROM logs
                WHERE id = ?
                """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapperLog(result));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur SQL lors de la recherche du log.", e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM logs WHERE id = ?";

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur SQL lors de la suppression du log.", e);
        }
    }

    public long compterTotal() {
        return compter("SELECT COUNT(*) FROM logs");
    }

    public long compterRefuses() {
        return compter("SELECT COUNT(*) FROM logs WHERE resultat = 'REFUSE'");
    }

    public long compterUtilisateursDistincts() {
        return compter("SELECT COUNT(DISTINCT usId) FROM logs");
    }

    public Map<String, Long> actionsByUser() {
        String sql = """
                SELECT u.login, COUNT(l.id) AS nombre_actions
                FROM logs l
                JOIN users u ON u.id = l.usId
                GROUP BY u.id, u.login
                ORDER BY nombre_actions DESC, u.login
                """;

        return lireCompteursParTexte(sql);
    }

    public Map<String, Long> topFichiers(int limite) {
        if (limite <= 0) {
            throw new IllegalArgumentException("La limite doit etre positive.");
        }

        String sql = """
                SELECT fichier, COUNT(id) AS nombre_lectures
                FROM logs
                WHERE action = 'READ' AND resultat = 'OK'
                GROUP BY fichier
                ORDER BY nombre_lectures DESC, fichier
                LIMIT ?
                """;

        Map<String, Long> resultats = new LinkedHashMap<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, limite);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    resultats.put(result.getString(1), result.getLong(2));
                }
            }
            return resultats;
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur SQL lors du calcul des fichiers les plus consultes.", e);
        }
    }

    public long refusesByUser(String login) {
        if (login == null || login.isBlank()) {
            return 0;
        }

        String sql = """
                SELECT COUNT(l.id)
                FROM logs l
                JOIN users u ON u.id = l.usId
                WHERE u.login = ? AND l.resultat = 'REFUSE'
                """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, login.trim());
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getLong(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur SQL lors du calcul des acces refuses.", e);
        }
    }

    public Optional<Map.Entry<String, Long>> userPlusActif() {
        String sql = """
                SELECT u.login, COUNT(l.id) AS nombre_actions
                FROM logs l
                JOIN users u ON u.id = l.usId
                GROUP BY u.id, u.login
                ORDER BY nombre_actions DESC, u.login
                LIMIT 1
                """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            if (result.next()) {
                return Optional.of(Map.entry(result.getString(1), result.getLong(2)));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur SQL lors de la recherche de l'utilisateur le plus actif.", e);
        }
    }

    public Map<String, Long> repartitionByAction() {
        String sql = """
                SELECT action, COUNT(id) AS nombre_actions
                FROM logs
                GROUP BY action
                ORDER BY action
                """;

        return lireCompteursParTexte(sql);
    }

    private long compter(String sql) {
        try (PreparedStatement statement = getConnection().prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            return result.next() ? result.getLong(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur SQL lors du calcul d'une statistique.", e);
        }
    }

    private Map<String, Long> lireCompteursParTexte(String sql) {
        Map<String, Long> resultats = new LinkedHashMap<>();

        try (PreparedStatement statement = getConnection().prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                resultats.put(result.getString(1), result.getLong(2));
            }
            return resultats;
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur SQL lors du calcul des statistiques.", e);
        }
    }

    private Log mapperLog(ResultSet result) throws SQLException {
        return new Log(
                result.getInt("id"),
                result.getInt("usId"),
                result.getString("action"),
                result.getString("fichier"),
                "OK".equals(result.getString("resultat"))
        );
    }
}
