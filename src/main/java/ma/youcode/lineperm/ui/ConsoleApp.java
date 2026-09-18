package ma.youcode.lineperm.ui;

import java.util.Scanner;
import java.util.Map;
import ma.youcode.lineperm.model.FichierProtege;
import ma.youcode.lineperm.service.FileService;
import ma.youcode.lineperm.service.LogAnalyzer;
import ma.youcode.lineperm.service.UserService;

/**
 * Interface texte de LinPerm.
 */
public class ConsoleApp {

    private final Scanner scanner = new Scanner(System.in);
    private final UserService userService = new UserService();
    private final FileService fileService = new FileService();
    private final LogAnalyzer logAnalyzer = new LogAnalyzer();

    private String utilisateurConnecte = null;

    public void run() {
        afficherBienvenue();

        while (true) {
            afficherInvite();

            if (!scanner.hasNextLine()) {
                return;
            }

            String ligne = scanner.nextLine().trim();
            if (ligne.isEmpty()) {
                continue;
            }

            String[] mots = ligne.split("\\s+");
            String commande = mots[0];

            switch (commande) {
                case "signup":
                    gererInscription(mots);
                    break;
                case "login":
                    gererConnexion(mots);
                    break;
                case "logout":
                    gererDeconnexion(mots);
                    break;
                case "ls":
                    gererListe(mots);
                    break;
                case "touch":
                    gererCreation(mots);
                    break;
                case "cat":
                    gererLecture(mots);
                    break;
                case "nano":
                    gererEdition(mots);
                    break;
                case "rm":
                    gererSuppression(mots);
                    break;
                case "chmod":
                    gererChmod(mots);
                    break;
                case "stats":
                    gererStats();
                    break;
                case "help":
                    afficherAide();
                    break;
                case "exit":
                    System.out.println("Au revoir.");
                    return;
                default:
                    System.out.println("Commande inconnue. Tapez help.");
                    break;
            }
        }
    }

    private void afficherBienvenue() {
        System.out.println("=====================================");
        System.out.println("  LinPerm - gestion de fichiers");
        System.out.println("=====================================");
        System.out.println("Commandes : signup | login | stats | help | exit");
    }

    private void afficherInvite() {
        if (utilisateurConnecte == null) {
            System.out.print("linperm> ");
        } else {
            System.out.print(utilisateurConnecte + "@linperm> ");
        }
    }

    private void gererInscription(String[] mots) {
        if (!nombreMotsCorrect(mots, 1, "Usage : signup")) {
            return;
        }
        if (utilisateurConnecte != null) {
            System.out.println("Deja connecte. Deconnectez-vous d'abord.");
            return;
        }

        System.out.print("Login : ");
        String login = lireLigne();
        System.out.print("Mot de passe : ");
        String motDePasse = lireLigne();

        if (login == null || motDePasse == null) {
            return;
        }

        System.out.println(userService.inscrire(login, motDePasse));
    }

    private void gererConnexion(String[] mots) {
        if (!nombreMotsCorrect(mots, 1, "Usage : login")) {
            return;
        }
        if (utilisateurConnecte != null) {
            System.out.println("Deja connecte. Deconnectez-vous d'abord.");
            return;
        }

        System.out.print("Login : ");
        String login = lireLigne();
        System.out.print("Mot de passe : ");
        String motDePasse = lireLigne();

        if (login == null || motDePasse == null) {
            return;
        }

        if (userService.motDePasseCorrect(login, motDePasse)) {
            utilisateurConnecte = login.trim();
            System.out.println("Bienvenue " + utilisateurConnecte + " !");
        } else {
            System.out.println("Login ou mot de passe incorrect.");
        }
    }

    private void gererDeconnexion(String[] mots) {
        if (!nombreMotsCorrect(mots, 1, "Usage : logout")) {
            return;
        }
        if (!estConnecte()) {
            return;
        }

        utilisateurConnecte = null;
        System.out.println("Deconnecte.");
    }

    private void gererListe(String[] mots) {
        if (!estConnecte()) {
            return;
        }
        if (!nombreMotsCorrect(mots, 2, "Usage : ls -l")) {
            return;
        }
        if (!mots[1].equals("-l")) {
            System.out.println("Usage : ls -l");
            return;
        }

        for (FichierProtege fichier : fileService.listerFichiers()) {
            System.out.println(fichier.getDroits() + " "
                    + fichier.getProprietaire() + " " + fichier.getNom());
        }
    }

    private void gererCreation(String[] mots) {
        if (!estConnecte()) {
            return;
        }
        if (!nombreMotsCorrect(mots, 2, "Usage : touch <f>")) {
            return;
        }

        System.out.println(fileService.creerFichier(mots[1], utilisateurConnecte));
    }

    private void gererLecture(String[] mots) {
        if (!estConnecte()) {
            return;
        }
        if (!nombreMotsCorrect(mots, 2, "Usage : cat <f>")) {
            return;
        }
        if (!fileService.fichierExiste(mots[1])) {
            System.out.println("Le fichier n'existe pas.");
            return;
        }

        String contenu = fileService.lireFichier(mots[1], utilisateurConnecte);
        if (contenu == null) {
            System.out.println("Permission denied.");
            return;
        }

        System.out.print(contenu);
        if (!contenu.isEmpty() && !contenu.endsWith(System.lineSeparator())) {
            System.out.println();
        }
    }

    private void gererEdition(String[] mots) {
        if (!estConnecte()) {
            return;
        }
        if (!nombreMotsCorrect(mots, 2, "Usage : nano <f>")) {
            return;
        }

        String nomFichier = mots[1];
        if (!fileService.fichierExiste(nomFichier)) {
            System.out.println("Le fichier n'existe pas.");
            return;
        }
        if (!fileService.peutEcrire(nomFichier, utilisateurConnecte)) {
            System.out.println("Permission denied.");
            return;
        }

        afficherAncienContenu(nomFichier);
        System.out.println("Saisissez le nouveau contenu. Fin : EOF sur une ligne seule.");

        StringBuilder nouveauContenu = new StringBuilder();
        while (true) {
            String ligne = lireLigne();
            if (ligne == null) {
                return;
            }
            if (ligne.equals("EOF")) {
                break;
            }
            nouveauContenu.append(ligne).append(System.lineSeparator());
        }

        System.out.println(fileService.ecrireFichier(
                nomFichier, nouveauContenu.toString(), utilisateurConnecte));
    }

    private void afficherAncienContenu(String nomFichier) {
        if (!fileService.peutLire(nomFichier, utilisateurConnecte)) {
            System.out.println("Contenu actuel masque : edition a l'aveugle.");
            return;
        }

        String ancienContenu = fileService.lireFichier(nomFichier, utilisateurConnecte);
        if (ancienContenu != null && !ancienContenu.isEmpty()) {
            System.out.print(ancienContenu);
            if (!ancienContenu.endsWith(System.lineSeparator())) {
                System.out.println();
            }
        }
    }

    private void gererSuppression(String[] mots) {
        if (!estConnecte()) {
            return;
        }
        if (!nombreMotsCorrect(mots, 2, "Usage : rm <f>")) {
            return;
        }

        System.out.println(fileService.supprimerFichier(mots[1], utilisateurConnecte));
    }

    private void gererChmod(String[] mots) {
        if (!estConnecte()) {
            return;
        }
        if (!nombreMotsCorrect(mots, 3, "Usage : chmod <r|w|d|-r|-w|-d> <f>")) {
            return;
        }

        String droitDemande = mots[1];
        boolean accorder;
        char droit;

        if (droitDemande.length() == 1) {
            accorder = true;
            droit = droitDemande.charAt(0);
        } else if (droitDemande.length() == 2 && droitDemande.charAt(0) == '-') {
            accorder = false;
            droit = droitDemande.charAt(1);
        } else {
            System.out.println("Usage : chmod <r|w|d|-r|-w|-d> <f>");
            return;
        }

        if (droit != 'r' && droit != 'w' && droit != 'd') {
            System.out.println("Usage : chmod <r|w|d|-r|-w|-d> <f>");
            return;
        }

        System.out.println(fileService.changerDroit(
                mots[2], droit, accorder, utilisateurConnecte));
    }

    private void gererStats() {
        System.out.println("Bienvenue dans LogAnalyzer. Choisissez une statistique par son numero.");

        while (true) {
            afficherMenuStats();
            System.out.print("Choix : ");
            String choix = lireLigne();

            if (choix == null) {
                return;
            }

            switch (choix.trim()) {
                case "1":
                    System.out.println(
                            "Nombre total d'actions : "
                            + logAnalyzer.nombreTotalActions()
                    );
                    break;

                case "2":
                    System.out.println(
                            "Nombre d'acces refuses : "
                            + logAnalyzer.nombreAccesRefuse()
                    );
                    break;

                case "3":
                    System.out.println(
                            "Nombre d'utilisateurs distincts : "
                            + logAnalyzer.nombreUserDistincts()
                    );
                    break;

                case "4":
                    Map<String, Long> actionsParUtilisateur
                            = logAnalyzer.nombreActionParUser();

                    actionsParUtilisateur.forEach(
                            (utilisateur, nombre)
                            -> System.out.println(utilisateur + " : " + nombre)
                    );
                    break;

                case "5":
                    System.out.println("Top 3 des fichiers consultes :");
                    logAnalyzer.topFichierConsulter();
                    break;

                case "6":
                    System.out.print("Nom de l'utilisateur : ");
                    String nomUser = scanner.nextLine().trim();

                    long accesRefuses
                            = logAnalyzer.accessRefuseUtilisateur(nomUser);

                    System.out.println(
                            "Nombre d'acces refuses pour " + nomUser + " : "
                            + accesRefuses
                    );
                    break;

                case "7":
                    System.out.println("Utilisateur le plus actif :");

                    logAnalyzer.actifUser()
                            .ifPresentOrElse(
                                    entry -> System.out.println(
                                            entry.getKey() + " : " + entry.getValue()
                                    ),
                                    () -> System.out.println("Aucun log disponible.")
                            );
                    break;

                case "8":
                    Map<String, Long> actionsParType
                            = logAnalyzer.actionssParType();

                    actionsParType.forEach(
                            (action, nombre)
                            -> System.out.println(action + " : " + nombre)
                    );
                    break;

                case "9":
                    return;

                default:
                    System.out.println("Choix invalide.");
                    break;
            }
        }
    }

    private void afficherMenuStats() {
        System.out.println("=== LogAnalyzer ===");
        System.out.println("1. Nombre total d'actions");
        System.out.println("2. Nombre d'acces refuses");
        System.out.println("3. Utilisateurs distincts");
        System.out.println("4. Actions par utilisateur");
        System.out.println("5. Top 3 des fichiers consultes");
        System.out.println("6. Acces refuses d'un utilisateur");
        System.out.println("7. Utilisateur le plus actif");
        System.out.println("8. Repartition des actions par type");
        System.out.println("9. Quitter le menu");
    }

    private boolean estConnecte() {
        if (utilisateurConnecte != null) {
            return true;
        }

        System.out.println("Vous n'etes pas connecte.");
        return false;
    }

    private boolean nombreMotsCorrect(String[] mots, int nombreAttendu, String aide) {
        if (mots.length == nombreAttendu) {
            return true;
        }

        System.out.println(aide);
        return false;
    }

    private String lireLigne() {
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine();
    }

    private void afficherAide() {
        System.out.println("signup | login | logout | ls -l | touch <f> | cat <f> | stats");
        System.out.println("nano <f> | rm <f> | chmod <r|w|d|-r|-w|-d> <f> | exit");
    }
}
