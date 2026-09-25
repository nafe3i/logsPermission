package ma.youcode.lineperm.service;

// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.StandardOpenOption;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

import ma.youcode.lineperm.model.User;
import ma.youcode.lineperm.dao.UserDao;

/**
 * Gere les comptes et la connexion.
 */
public class UserService {

    private final UserDao userDao;

    public UserService() {
        this.userDao = new UserDao();
    }

    public String inscrire(String login, String motDePasse) {
        if (login == null || motDePasse == null) {
            return "Login et mot de passe ne peuvent pas etre vides.";
        }

        login = login.trim();
        if (login.isEmpty() || motDePasse.isEmpty()) {
            return "Login et mot de passe ne peuvent pas etre vides.";
        }
        if (login.contains(" ") || login.contains(":")) {
            return "Le login ne doit contenir ni espace ni caractere ':'.";
        }

        try {
            if (userDao.findByLogin(login).isPresent()) {
                return "L'utilisateur est deja enregistre.";
            }

            String motDePasseHache = BCrypt.hashpw(motDePasse, BCrypt.gensalt());
            User nouvelUtilisateur = new User(login, motDePasseHache);
            if (!userDao.save(nouvelUtilisateur)) {
                return "Impossible de creer le compte.";
            }

            return "Creation de compte reussie.";
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return "Erreur lors de la sauvegarde du compte.";
        }
    }

    public Optional<User> motDePasseCorrect(String login, String motDePasse) {
        if (login == null || motDePasse == null) {
            return Optional.empty();
        }

        String loginNormalise = login.trim();
        if (loginNormalise.isEmpty() || motDePasse.isEmpty()) {
            return Optional.empty();
        }

        Optional<User> utilisateur = userDao.findByLogin(loginNormalise);
        if (utilisateur.isEmpty()) {
            return Optional.empty();
        }

        User user = utilisateur.get();
        if (BCrypt.checkpw(motDePasse, user.getPasswordHash())) {
            return Optional.of(user);
        }

        return Optional.empty();
    }
}
