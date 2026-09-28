package ma.youcode.lineperm.access;

import ma.youcode.lineperm.model.FichierProtege;

public final class ControleAcces {

    private ControleAcces() {
    }

    public static boolean estProprietaire(
            int utilisateurId,
            FichierProtege fichier
    ) {
        return utilisateurId > 0
                && fichier != null
                && utilisateurId == fichier.getProprietaireId();
    }

    public static boolean estAutorise(
            int utilisateurId,
            FichierProtege fichier,
            char droit
    ) {
        if (utilisateurId <= 0 || fichier == null) {
            return false;
        }

        if (estProprietaire(utilisateurId, fichier)) {
            return switch (droit) {
                case 'r' ->
                    fichier.aDroitLectureProprietaire();
                case 'w' ->
                    fichier.aDroitEcritureProprietaire();
                case 'd' ->
                    fichier.aDroitSuppressionProprietaire();
                default ->
                    false;
            };
        }

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
}
