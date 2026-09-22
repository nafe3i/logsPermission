package ma.youcode.lineperm.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Log {

    private int utilisateurId;
    private String action;
    private String fichier;
    private boolean resultat;
    private LocalDate date = LocalDate.now();
    private String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

    public Log(int utilisateurId, String action, String fichier, boolean resultat) {
        this.utilisateurId = utilisateurId;
        this.action = action;
        this.fichier = fichier;
        this.resultat = resultat;
    }

    public Log(int utilisateurId, String action, String fichier, boolean resultat, LocalDate date, String time) {
        this.utilisateurId = utilisateurId;
        this.action = action;
        this.fichier = fichier;
        this.resultat = resultat;
        this.date = date;
        this.time = time;
    }

    public int getUtilisateur() {
        return utilisateurId;
    }

    public String getAction() {
        return action;
    }

    public String getFichier() {
        return fichier;
    }

    public boolean getResultat() {
        return resultat;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getResultatConvert() {
        String resultatTexte = resultat ? "OK" : "REFUSE";
        return resultatTexte;

    }

    public String logLogContent() {
        return date + ";" + time + ";" + utilisateurId + ";" + action + ";" + fichier + ";" + getResultatConvert();
    }
}
