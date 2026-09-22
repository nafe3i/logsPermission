package ma.youcode.lineperm.access;

import ma.youcode.lineperm.model.FichierProtege;

/** Classe qui decide uniquement si un droit est autorise ou non. */
public class ControleAcces {

    public static boolean estProprietaire(int userId, FichierProtege fichier) {
        if (userId == null || fichier == null) {
            return false;
        }
        return login.equals(fichier.getOwnerId());
    }

    /**
     * Un utilisateur utilise un seul bloc de droits : proprietaire OU autres.
     */
    public static boolean estAutorise(String login, FichierProtege fichier, char droit) {
        if (login == null || fichier == null) {
            return false;
        }

        if (estProprietaire(login, fichier)) {
            if (droit == 'r') {
                return fichier.aDroitLectureProprietaire();
            }
            if (droit == 'w') {
                return fichier.aDroitEcritureProprietaire();
            }
            if (droit == 'd') {
                return fichier.aDroitSuppressionProprietaire();
            }
        } else {
            if (droit == 'r') {
                return fichier.aDroitLectureAutres();
            }
            if (droit == 'w') {
                return fichier.aDroitEcritureAutres();
            }
            if (droit == 'd') {
                return fichier.aDroitSuppressionAutres();
            }
        }

        return false;
    }
}
