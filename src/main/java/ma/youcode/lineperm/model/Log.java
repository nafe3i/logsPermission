package ma.youcode.lineperm.model;

import java.util.Locale;
import java.util.Set;

/**
 * Represente un evenement d'audit de l'application.
 *
 * <p>Le login n'est pas stocke ici : la base conserve l'identifiant de
 * l'utilisateur dans {@code logs.usId}. Le login pourra toujours etre retrouve
 * par une jointure avec la table {@code users}.</p>
 */
public class Log {

    private static final Set<String> ACTIONS_AUTORISEES = Set.of(
            "CREATE", "READ", "WRITE", "DELETE", "CHMOD"
    );

    private final int id;
    private final int utilisateurId;
    private final String action;
    private final String fichier;
    private final boolean resultatOk;

    /**
     * Constructeur utilise avant la sauvegarde. L'identifiant du log sera cree
     * par SQLite.
     */
    public Log(int utilisateurId, String action, String fichier, boolean resultatOk) {
        this(0, utilisateurId, action, fichier, resultatOk);
    }

    /**
     * Constructeur utilise par LogDao lorsqu'il relit un log existant.
     */
    public Log(int id, int utilisateurId, String action, String fichier, boolean resultatOk) {
        if (utilisateurId <= 0) {
            throw new IllegalArgumentException("L'identifiant utilisateur doit etre positif.");
        }
        if (fichier == null || fichier.isBlank()) {
            throw new IllegalArgumentException("Le nom du fichier est obligatoire.");
        }

        String actionNormalisee = normaliserAction(action);
        if (!ACTIONS_AUTORISEES.contains(actionNormalisee)) {
            throw new IllegalArgumentException("Action de log invalide : " + action);
        }

        this.id = id;
        this.utilisateurId = utilisateurId;
        this.action = actionNormalisee;
        this.fichier = fichier;
        this.resultatOk = resultatOk;
    }

    private String normaliserAction(String action) {
        if (action == null) {
            return "";
        }
        return action.trim().toUpperCase(Locale.ROOT);
    }

    public int getId() {
        return id;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public String getAction() {
        return action;
    }

    public String getFichier() {
        return fichier;
    }

    public boolean isResultatOk() {
        return resultatOk;
    }
}
