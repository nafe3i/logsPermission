package ma.youcode.lineperm.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ma.youcode.lineperm.access.ControleAcces;
import ma.youcode.lineperm.model.FichierProtege;
import ma.youcode.lineperm.model.Log;

/**
 * Gere les fichiers de l'application. Les autorisations sont toujours verifiees
 * ici avant de lire ou d'ecrire.
 */
public class FileService {

    private final Map<String, FichierProtege> fichiers = new HashMap<>();
    private final Path dossierData = Path.of("data");
    private final Path fichierLogs = Path.of("access.log");
    private final Path fichierDroits = Path.of("files.txt");

    public FileService() {
        chargerFichiers();
    }
    // LogAnalyzer logList = new LogAnalyzer();
    // private final List<Log> logs = logList.getLlogs();

    /**
     * ls -l : tous les utilisateurs connectes peuvent voir cette liste.
     */
    public List<FichierProtege> listerFichiers() {
        return new ArrayList<>(fichiers.values());
    }

    public boolean fichierExiste(String nomFichier) {
        return fichiers.containsKey(nomFichier);
    }

    /**
     * touch <f>
     */
    public String creerFichier(String nomFichier, String proprietaire) {
        if (!nomEstValide(nomFichier)) {

            crateLog(proprietaire, "create", nomFichier, false);
            return "Le nom de fichier est invalide ou contient un chemin.";
        }
        if (fichierExiste(nomFichier) || Files.exists(cheminDuFichier(nomFichier))) {
            crateLog(proprietaire, "create", nomFichier, false);
            return "Le fichier existe deja.";

        }

        try {
            Files.createDirectories(dossierData);
            Files.createFile(cheminDuFichier(nomFichier));

            FichierProtege fichier = new FichierProtege(nomFichier, proprietaire);
            fichiers.put(nomFichier, fichier);
            sauvegarderDroits();
            crateLog(proprietaire, "create", nomFichier, true);
            return "Fichier cree : " + nomFichier;
        } catch (IOException e) {
            crateLog(proprietaire, "create", nomFichier, false);
            return ("Erreur lors de la creation du fichier" + e);
        }
    }

    /**
     * cat <f>. null veut dire que la lecture est refusee. Une chaine vide reste
     * donc un fichier vide autorise.
     */
    public String lireFichier(String nomFichier, String utilisateur) {
        FichierProtege fichier = chercherFichier(nomFichier);
        if (!ControleAcces.estAutorise(utilisateur, fichier, 'r')) {
            crateLog(utilisateur, "read", nomFichier, false);
            return null;
        }

        try {
            String rets = Files.readString(cheminDuFichier(nomFichier));
            crateLog(utilisateur, "read", nomFichier, true);
            return rets;
        } catch (IOException e) {
            crateLog(utilisateur, "read", nomFichier, false);
            return null;
        }
    }

    public boolean peutLire(String nomFichier, String utilisateur) {
        FichierProtege fichier = chercherFichier(nomFichier);
        return ControleAcces.estAutorise(utilisateur, fichier, 'r');
    }

    public boolean peutEcrire(String nomFichier, String utilisateur) {
        FichierProtege fichier = chercherFichier(nomFichier);
        return ControleAcces.estAutorise(utilisateur, fichier, 'w');
    }

    /**
     * nano <f> : le contenu est entierement remplace.
     */
    public String ecrireFichier(String nomFichier, String contenu, String utilisateur) {
        FichierProtege fichier = chercherFichier(nomFichier);
        if (fichier == null) {
            crateLog(utilisateur, "write", nomFichier, false);

            return "Le fichier n'existe pas.";
        }
        if (!ControleAcces.estAutorise(utilisateur, fichier, 'w')) {
            crateLog(utilisateur, "write", nomFichier, false);
            return "Permission denied.";
        }

        try {
            Files.writeString(cheminDuFichier(nomFichier), contenu,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            crateLog(utilisateur, "write", nomFichier, true);
            return "Contenu ecrit avec succes.";
        } catch (IOException e) {
            crateLog(utilisateur, "write", nomFichier, false);
            return "Erreur lors de l'ecriture du fichier.";
        }
    }

    /**
     * rm <f> : supprime le contenu et ses metadonnees si le droit d est
     * accorde.
     */
    public String supprimerFichier(String nomFichier, String utilisateur) {
        FichierProtege fichier = chercherFichier(nomFichier);
        if (fichier == null) {
            crateLog(utilisateur, "SUPPRESSION", nomFichier, false);
            return "Le fichier n'existe pas.";
        }
        if (!ControleAcces.estAutorise(utilisateur, fichier, 'd')) {
            crateLog(utilisateur, "SUPPRESSION", nomFichier, false);
            return "Permission denied.";
        }

        try {
            Files.delete(cheminDuFichier(nomFichier));
            fichiers.remove(nomFichier);
            sauvegarderDroits();
            crateLog(utilisateur, "SUPPRESSION", nomFichier, true);
            return "Fichier supprime : " + nomFichier;
        } catch (IOException e) {
            crateLog(utilisateur, "SUPPRESSION", nomFichier, false);
            return "Erreur lors de la suppression du fichier.";
        }
    }

    /**
     * chmod <r|w|d> <f> ou chmod -<r|w|d> <f>
     */
    public String changerDroit(String nomFichier, char droit, boolean accorder, String utilisateur) {
        FichierProtege fichier = chercherFichier(nomFichier);
        if (fichier == null) {
            return "Le fichier n'existe pas.";
        }
        if (!ControleAcces.estProprietaire(utilisateur, fichier)) {
            return "Permission denied.";
        }
        if (droit != 'r' && droit != 'w' && droit != 'd') {
            return "Droit invalide.";
        }

        if (droitAutres(fichier, droit) == accorder) {
            if (accorder) {
                return "Le droit " + droit + " est deja accorde.";
            }
            return "Le droit " + droit + " est deja retire.";
        }

        modifierDroitAutres(fichier, droit, accorder);
        sauvegarderDroits();

        if (accorder) {
            return "Droit " + droit + " accorde aux autres.";
        }
        return "Droit " + droit + " retire aux autres.";
    }

    private FichierProtege chercherFichier(String nomFichier) {
        return fichiers.get(nomFichier);
    }

    private Path cheminDuFichier(String nomFichier) {
        return dossierData.resolve(nomFichier);
    }

    private boolean nomEstValide(String nomFichier) {
        if (nomFichier == null || nomFichier.trim().isEmpty()) {
            return false;
        }
        if (nomFichier.equals(".") || nomFichier.equals("..")) {
            return false;
        }
        if (nomFichier.contains("/") || nomFichier.contains("\\")
                || nomFichier.contains(":") || nomFichier.contains(";")) {
            return false;
        }
        return true;
    }

    private boolean droitAutres(FichierProtege fichier, char droit) {
        if (droit == 'r') {
            return fichier.aDroitLectureAutres();
        }
        if (droit == 'w') {
            return fichier.aDroitEcritureAutres();
        }
        if (droit == 'd') {
            return fichier.aDroitSuppressionAutres();
        }
        return false;
    }

    private void modifierDroitAutres(FichierProtege fichier, char droit, boolean valeur) {
        if (droit == 'r') {
            fichier.setDroitLectureAutres(valeur);
        }
        if (droit == 'w') {
            fichier.setDroitEcritureAutres(valeur);
        }
        if (droit == 'd') {
            fichier.setDroitSuppressionAutres(valeur);
        }
    }

    /**
     * Relit files.txt au lancement. Format : nom;proprietaire;rwd;r--
     */
    private void chargerFichiers() {
        if (!Files.exists(fichierDroits)) {
            return;
        }

        try {
            List<String> lignes = Files.readAllLines(fichierDroits);
            for (String ligne : lignes) {
                String[] morceaux = ligne.split(";");
                if (morceaux.length != 4 || !nomEstValide(morceaux[0]) || morceaux[1].isEmpty()) {
                    continue;
                }

                FichierProtege fichier = new FichierProtege(
                        morceaux[0], morceaux[1],
                        droitPresent(morceaux[2], 'r'),
                        droitPresent(morceaux[2], 'w'),
                        droitPresent(morceaux[2], 'd'),
                        droitPresent(morceaux[3], 'r'),
                        droitPresent(morceaux[3], 'w'),
                        droitPresent(morceaux[3], 'd'));

                if (!fichierExiste(fichier.getNom())) {
                    fichiers.put(fichier.getNom(), fichier);
                }
            }
        } catch (IOException e) {
            System.out.println("Impossible de charger les fichiers.");
        }
    }

    private boolean droitPresent(String bloc, char droit) {
        if (bloc.length() != 3) {
            return false;
        }
        if (droit == 'r') {
            return bloc.charAt(0) == 'r';
        }
        if (droit == 'w') {
            return bloc.charAt(1) == 'w';
        }
        if (droit == 'd') {
            return bloc.charAt(2) == 'd';
        }
        return false;
    }

    /**
     * Reecrit toutes les lignes afin d'avoir une seule ligne par fichier.
     */
    private void sauvegarderDroits() {
        StringBuilder contenu = new StringBuilder();

        for (FichierProtege fichier : fichiers.values()) {
            contenu.append(fichier.getNom()).append(";")
                    .append(fichier.getProprietaire()).append(";")
                    .append(fichier.getDroitsProprietaire()).append(";")
                    .append(fichier.getDroitsAutres()).append(System.lineSeparator());
        }

        try {
            Files.writeString(fichierDroits, contenu.toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("Impossible de sauvegarder les droits.");
        }
    }
    // Log log = new Log(proprietaire, "create", nomFichier, false);

    // logs.add (log);
    private void crateLog(String utilisateur, String act, String nomFichier, boolean realiser) {
        Log log = new Log(utilisateur, act, nomFichier, realiser);
        try {
            Files.writeString(fichierLogs, log.logLogContent() + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde du log.");
        }

    }

}
