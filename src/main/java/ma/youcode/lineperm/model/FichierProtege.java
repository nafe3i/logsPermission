package ma.youcode.lineperm.model;

/**
 * Contient les informations d'un fichier et ses six droits.
 * Cette classe ne lit ni n'ecrit sur le disque : elle garde seulement les donnees.
 */
public class FichierProtege {

    private String nom;
    private String proprietaire;

    private boolean lectureProprietaire;
    private boolean ecritureProprietaire;
    private boolean suppressionProprietaire;

    private boolean lectureAutres;
    private boolean ecritureAutres;
    private boolean suppressionAutres;

    /** Constructeur utilise quand un utilisateur cree un nouveau fichier. */
    public FichierProtege(String nom, String proprietaire) {
        this(nom, proprietaire, true, true, true, false, false, false);
    }

    /** Constructeur utilise au demarrage pour relire les droits sauvegardes. */
    public FichierProtege(String nom, String proprietaire,
            boolean lectureProprietaire, boolean ecritureProprietaire, boolean suppressionProprietaire,
            boolean lectureAutres, boolean ecritureAutres, boolean suppressionAutres) {

        this.nom = nom;
        this.proprietaire = proprietaire;
        this.lectureProprietaire = lectureProprietaire;
        this.ecritureProprietaire = ecritureProprietaire;
        this.suppressionProprietaire = suppressionProprietaire;
        this.lectureAutres = lectureAutres;
        this.ecritureAutres = ecritureAutres;
        this.suppressionAutres = suppressionAutres;
    }

    public String getNom() {
        return nom;
    }

    public String getProprietaire() {
        return proprietaire;
    }

    public boolean aDroitLectureProprietaire() {
        return lectureProprietaire;
    }

    public boolean aDroitEcritureProprietaire() {
        return ecritureProprietaire;
    }

    public boolean aDroitSuppressionProprietaire() {
        return suppressionProprietaire;
    }

    public boolean aDroitLectureAutres() {
        return lectureAutres;
    }

    public boolean aDroitEcritureAutres() {
        return ecritureAutres;
    }

    public boolean aDroitSuppressionAutres() {
        return suppressionAutres;
    }

    public void setDroitLectureAutres(boolean valeur) {
        lectureAutres = valeur;
    }

    public void setDroitEcritureAutres(boolean valeur) {
        ecritureAutres = valeur;
    }

    public void setDroitSuppressionAutres(boolean valeur) {
        suppressionAutres = valeur;
    }

    /** Exemple de resultat : rwd|-w- */
    public String getDroits() {
        return getDroitsProprietaire() + "|" + getDroitsAutres();
    }

    public String getDroitsProprietaire() {
        return "" + (lectureProprietaire ? 'r' : '-')
                + (ecritureProprietaire ? 'w' : '-')
                + (suppressionProprietaire ? 'd' : '-');
    }

    public String getDroitsAutres() {
        return "" + (lectureAutres ? 'r' : '-')
                + (ecritureAutres ? 'w' : '-')
                + (suppressionAutres ? 'd' : '-');
    }
}
