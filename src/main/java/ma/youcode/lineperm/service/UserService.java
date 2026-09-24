package ma.youcode.lineperm.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

import ma.youcode.lineperm.model.User;
import ma.youcode.lineperm.dao.UserDao;

/** Gere les comptes et la connexion. */
public class UserService {

    private final Map<String, User> utilisateurs = new HashMap<>();
    private final Path fichierUtilisateurs = Path.of("users.txt");

    public UserService() {
        chargerUtilisateurs();
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
        if (chercherUtilisateur(login) != null) {
            return "L'utilisateur est deja enregistre.";
        }

        String motDePasseHache = BCrypt.hashpw(motDePasse, BCrypt.gensalt());
        User nouvelUtilisateur = new User(login, motDePasseHache);

        try {
            // String ligne = login + ":" + motDePasseHache + System.lineSeparator();
            // Files.writeString(fichierUtilisateurs, ligne, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            // utilisateurs.put(login, nouvelUtilisateur);
            UserDao userDao = new UserDao();
            userDao.save(nouvelUtilisateur);    
            return "Compte cree avec succes.";
        } catch (IOException e) {
            return "Erreur lors de la sauvegarde du compte.";
        }
    }

    public boolean motDePasseCorrect(String login, String motDePasse) {
        if (login == null || motDePasse == null) {
            return false;
        }

        User utilisateur = chercherUtilisateur(login.trim());
        if (utilisateur == null) {
            return false;
        }

        return BCrypt.checkpw(motDePasse, utilisateur.getPasswordHash());
    }

    private User chercherUtilisateur(String login) {
        return utilisateurs.get(login);
    }

    private void chargerUtilisateurs() {
        if (!Files.exists(fichierUtilisateurs)) {
            return;
        }

        try {
            List<String> lignes = Files.readAllLines(fichierUtilisateurs);
            for (String ligne : lignes) {
                String[] morceaux = ligne.split(":", 2);
                if (morceaux.length == 2 && !utilisateurs.containsKey(morceaux[0])) {
                    utilisateurs.put(morceaux[0], new User(morceaux[0], morceaux[1]));
                }
            }
        } catch (IOException e) {
            System.out.println("Impossible de charger les utilisateurs.");
        }
    }
}
