package ma.youcode.lineperm.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ma.youcode.lineperm.model.FichierProtege;

public class FileDao extends AbstractDao<FichierProtege> {

    @Override
    public boolean save(FichierProtege fichier) {
        if (fichier == null) {
            throw new IllegalArgumentException("Le fichier ne peut pas etre null.");
        }

        String sql = """
                INSERT INTO protected_files (
                    name,
                    owner_id,
                    owner_read,
                    owner_write,
                    owner_delete,
                    others_read,
                    others_write,
                    others_delete
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, fichier.getNom());
            statement.setInt(2, fichier.getProprietaireId());
            statement.setBoolean(3, fichier.aDroitLectureProprietaire());
            statement.setBoolean(4, fichier.aDroitEcritureProprietaire());
            statement.setBoolean(5, fichier.aDroitSuppressionProprietaire());
            statement.setBoolean(6, fichier.aDroitLectureAutres());
            statement.setBoolean(7, fichier.aDroitEcritureAutres());
            statement.setBoolean(8, fichier.aDroitSuppressionAutres());

            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erreur SQL lors de la creation du fichier.", e
            );
        }
    }

    @Override
    public Optional<FichierProtege> findById(int id) {
        String sql = """
                SELECT id, name, owner_id,
                       owner_read, owner_write, owner_delete,
                       others_read, others_write, others_delete
                FROM protected_files
                WHERE id = ?
                """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapperFichier(result));
                }

                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erreur SQL lors de la recherche du fichier.", e
            );
        }
    }

    public Optional<FichierProtege> findByName(String nom) {
        String sql = """
                SELECT id, name, owner_id,
                       owner_read, owner_write, owner_delete,
                       others_read, others_write, others_delete
                FROM protected_files
                WHERE name = ?
                """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, nom);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapperFichier(result));
                }

                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erreur SQL lors de la recherche du fichier.", e
            );
        }
    }

    public List<FichierProtege> findAll() {
        String sql = """
                SELECT id, name, owner_id,
                       owner_read, owner_write, owner_delete,
                       others_read, others_write, others_delete
                FROM protected_files
                ORDER BY name
                """;

        List<FichierProtege> fichiers = new ArrayList<>();

        try (PreparedStatement statement = getConnection().prepareStatement(sql); ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                fichiers.add(mapperFichier(result));
            }

            return fichiers;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erreur SQL lors de la liste des fichiers.", e
            );
        }
    }

    public List<FichierProtege> findByProprietaire(int proprietaireId) {
        String sql = """
                SELECT id, name, owner_id,
                       owner_read, owner_write, owner_delete,
                       others_read, others_write, others_delete
                FROM protected_files
                WHERE owner_id = ?
                ORDER BY name
                """;

        List<FichierProtege> fichiers = new ArrayList<>();

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, proprietaireId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    fichiers.add(mapperFichier(result));
                }
            }

            return fichiers;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erreur SQL lors de la recherche des fichiers du proprietaire.", e
            );
        }
    }

    public boolean updateDroits(FichierProtege fichier) {
        String sql = """
                UPDATE protected_files
                SET owner_read = ?,
                    owner_write = ?,
                    owner_delete = ?,
                    others_read = ?,
                    others_write = ?,
                    others_delete = ?
                WHERE id = ?
                """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setBoolean(1, fichier.aDroitLectureProprietaire());
            statement.setBoolean(2, fichier.aDroitEcritureProprietaire());
            statement.setBoolean(3, fichier.aDroitSuppressionProprietaire());
            statement.setBoolean(4, fichier.aDroitLectureAutres());
            statement.setBoolean(5, fichier.aDroitEcritureAutres());
            statement.setBoolean(6, fichier.aDroitSuppressionAutres());
            statement.setInt(7, fichier.getId());

            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erreur SQL lors de la mise a jour des droits.", e
            );
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM protected_files WHERE id = ?";

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erreur SQL lors de la suppression du fichier.", e
            );
        }
    }

    private FichierProtege mapperFichier(ResultSet result) throws SQLException {
        return new FichierProtege(
                result.getInt("id"),
                result.getString("name"),
                result.getInt("owner_id"),
                result.getBoolean("owner_read"),
                result.getBoolean("owner_write"),
                result.getBoolean("owner_delete"),
                result.getBoolean("others_read"),
                result.getBoolean("others_write"),
                result.getBoolean("others_delete")
        );
    }
}
