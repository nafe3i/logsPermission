package ma.youcode.lineperm.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import ma.youcode.lineperm.access.ControleAcces;
import ma.youcode.lineperm.dao.FileDao;
import ma.youcode.lineperm.dao.LogDao;
import ma.youcode.lineperm.dao.UserDao;
import ma.youcode.lineperm.model.FichierProtege;
import ma.youcode.lineperm.model.Log;
import ma.youcode.lineperm.model.User;

public class FileService {

    private final FileDao fileDao;
    private final LogDao logDao;
    private final UserDao userDao;
    private final Path dossierData = Path.of("data");

    public FileService() {
        this.fileDao = new FileDao();
        this.logDao = new LogDao();
        this.userDao = new UserDao();
    }

    public List<FichierProtege> listerFichiers() {
        return fileDao.findAll();
    }

    public String loginProprietaire(FichierProtege fichier) {
        return userDao.findById(fichier.getProprietaireId())
                .map(User::getLogin)
                .orElse("utilisateur-inconnu");
    }

    public boolean fichierExiste(String nomFichier) {
        return fileDao.findByName(nomFichier).isPresent();
    }

    public String creerFichier(String nomFichier, User utilisateur) {
        if (utilisateur == null) {
            return "Utilisateur non authentifie.";
        }

        if (!nomEstValide(nomFichier)) {
            creerLog(utilisateur, "create", nomFichier, false);
            return "Le nom de fichier est invalide ou contient un chemin.";
        }

        if (fichierExiste(nomFichier)
                || Files.exists(cheminDuFichier(nomFichier))) {
            creerLog(utilisateur, "create", nomFichier, false);
            return "Le fichier existe deja.";
        }

        boolean fichierPhysiqueCree = false;

        try {
            Files.createDirectories(dossierData);
            Files.createFile(cheminDuFichier(nomFichier));
            fichierPhysiqueCree = true;

            FichierProtege fichier = new FichierProtege(
                    nomFichier,
                    utilisateur.getId()
            );

            if (!fileDao.save(fichier)) {
                Files.deleteIfExists(cheminDuFichier(nomFichier));
                creerLog(utilisateur, "create", nomFichier, false);
                return "Impossible de sauvegarder les metadonnees du fichier.";
            }

            creerLog(utilisateur, "create", nomFichier, true);
            return "Fichier cree : " + nomFichier;
        } catch (IOException | IllegalStateException e) {
            if (fichierPhysiqueCree) {
                supprimerFichierPhysiqueSilencieusement(nomFichier);
            }

            creerLog(utilisateur, "create", nomFichier, false);
            return "Erreur lors de la creation du fichier.";
        }
    }

    public String lireFichier(String nomFichier, User utilisateur) {
        Optional<FichierProtege> resultat = fileDao.findByName(nomFichier);

        if (resultat.isEmpty()) {
            creerLog(utilisateur, "read", nomFichier, false);
            return null;
        }

        FichierProtege fichier = resultat.get();

        if (!ControleAcces.estAutorise(
                utilisateur.getId(),
                fichier,
                'r'
        )) {
            creerLog(utilisateur, "read", nomFichier, false);
            return null;
        }

        try {
            String contenu = Files.readString(cheminDuFichier(nomFichier));
            creerLog(utilisateur, "read", nomFichier, true);
            return contenu;
        } catch (IOException e) {
            creerLog(utilisateur, "read", nomFichier, false);
            return null;
        }
    }

    public boolean peutLire(String nomFichier, User utilisateur) {
        return fileDao.findByName(nomFichier)
                .map(fichier -> ControleAcces.estAutorise(
                utilisateur.getId(),
                fichier,
                'r'
        ))
                .orElse(false);
    }

    public boolean peutEcrire(String nomFichier, User utilisateur) {
        return fileDao.findByName(nomFichier)
                .map(fichier -> ControleAcces.estAutorise(
                utilisateur.getId(),
                fichier,
                'w'
        ))
                .orElse(false);
    }

    public String ecrireFichier(
            String nomFichier,
            String contenu,
            User utilisateur
    ) {
        Optional<FichierProtege> resultat = fileDao.findByName(nomFichier);

        if (resultat.isEmpty()) {
            creerLog(utilisateur, "write", nomFichier, false);
            return "Le fichier n'existe pas.";
        }

        FichierProtege fichier = resultat.get();

        if (!ControleAcces.estAutorise(
                utilisateur.getId(),
                fichier,
                'w'
        )) {
            creerLog(utilisateur, "write", nomFichier, false);
            return "Permission denied.";
        }

        try {
            Files.writeString(
                    cheminDuFichier(nomFichier),
                    contenu,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            creerLog(utilisateur, "write", nomFichier, true);
            return "Contenu ecrit avec succes.";
        } catch (IOException e) {
            creerLog(utilisateur, "write", nomFichier, false);
            return "Erreur lors de l'ecriture du fichier.";
        }
    }

    public String supprimerFichier(String nomFichier, User utilisateur) {
        Optional<FichierProtege> resultat = fileDao.findByName(nomFichier);

        if (resultat.isEmpty()) {
            creerLog(utilisateur, "delete", nomFichier, false);
            return "Le fichier n'existe pas.";
        }

        FichierProtege fichier = resultat.get();

        if (!ControleAcces.estAutorise(
                utilisateur.getId(),
                fichier,
                'd'
        )) {
            creerLog(utilisateur, "delete", nomFichier, false);
            return "Permission denied.";
        }

        if (!fileDao.delete(fichier.getId())) {
            creerLog(utilisateur, "delete", nomFichier, false);
            return "Impossible de supprimer les metadonnees du fichier.";
        }

        try {
            Files.deleteIfExists(cheminDuFichier(nomFichier));
            creerLog(utilisateur, "delete", nomFichier, true);
            return "Fichier supprime : " + nomFichier;
        } catch (IOException e) {
            creerLog(utilisateur, "delete", nomFichier, false);
            return "Metadonnees supprimees, mais fichier physique non supprime.";
        }
    }

    public String changerDroit(
            String nomFichier,
            char droit,
            boolean accorder,
            User utilisateur
    ) {
        Optional<FichierProtege> resultat = fileDao.findByName(nomFichier);

        if (resultat.isEmpty()) {
            creerLog(utilisateur, "CHMOD", nomFichier, false);
            return "Le fichier n'existe pas.";
        }

        FichierProtege fichier = resultat.get();

        if (!ControleAcces.estProprietaire(
                utilisateur.getId(),
                fichier
        )) {
            creerLog(utilisateur, "CHMOD", nomFichier, false);
            return "Permission denied.";
        }

        if (droit != 'r' && droit != 'w' && droit != 'd') {
            creerLog(utilisateur, "CHMOD", nomFichier, false);
            return "Droit invalide.";
        }

        if (droitAutres(fichier, droit) == accorder) {
            creerLog(utilisateur, "CHMOD", nomFichier, true);
            return accorder
                    ? "Le droit " + droit + " est deja accorde."
                    : "Le droit " + droit + " est deja retire.";
        }

        modifierDroitAutres(fichier, droit, accorder);

        if (!fileDao.updateDroits(fichier)) {
            creerLog(utilisateur, "CHMOD", nomFichier, false);
            return "Impossible de sauvegarder les droits.";
        }

        creerLog(utilisateur, "CHMOD", nomFichier, true);
        return accorder
                ? "Droit " + droit + " accorde aux autres."
                : "Droit " + droit + " retire aux autres.";
    }

    private Path cheminDuFichier(String nomFichier) {
        return dossierData.resolve(nomFichier);
    }

    private boolean nomEstValide(String nomFichier) {
        if (nomFichier == null || nomFichier.trim().isEmpty()) {
            return false;
        }

        return !nomFichier.equals(".")
                && !nomFichier.equals("..")
                && !nomFichier.contains("/")
                && !nomFichier.contains("\\")
                && !nomFichier.contains(":")
                && !nomFichier.contains(";");
    }

    private boolean droitAutres(FichierProtege fichier, char droit) {
        return switch (droit) {
            case 'r' ->
                fichier.aDroitLectureAutres();
            case 'w' ->
                fichier.aDroitEcritureAutres();
            case 'd' ->
                fichier.aDroitSuppressionAutres();
            default ->
                false;
        };
    }

    private void modifierDroitAutres(
            FichierProtege fichier,
            char droit,
            boolean valeur
    ) {
        switch (droit) {
            case 'r' ->
                fichier.setDroitLectureAutres(valeur);
            case 'w' ->
                fichier.setDroitEcritureAutres(valeur);
            case 'd' ->
                fichier.setDroitSuppressionAutres(valeur);
            default -> {
            }
        }
    }

    private void creerLog(
            User utilisateur,
            String action,
            String nomFichier,
            boolean resultat
    ) {
        if (utilisateur == null) {
            return;
        }

        String fichier = nomFichier == null || nomFichier.isBlank()
                ? "(inconnu)"
                : nomFichier;

        logDao.save(new Log(utilisateur.getId(), action, fichier, resultat));
    }

    private void supprimerFichierPhysiqueSilencieusement(String nomFichier) {
        try {
            Files.deleteIfExists(cheminDuFichier(nomFichier));
        } catch (IOException e) {
            System.err.println(
                    "Nettoyage impossible du fichier physique : " + nomFichier
            );
        }
    }
}
