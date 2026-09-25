package ma.youcode.lineperm.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import ma.youcode.lineperm.model.Log;

/**
 * Charge access.log une seule fois et fournit les analyses de statistiques.
 */
public class LogAnalyzer {

    private final Path fichierLogs = Path.of("access.log");
    private final List<Log> logs = new ArrayList<>();

    // public List getLlogs() {
    //     return logs;
    // }

    public LogAnalyzer() {
        chargerLogs();
    }

    private void chargerLogs() {
        logs.clear();

        if (!Files.exists(fichierLogs)) {
            return;
        }

        try {
            List<String> lignes = Files.readAllLines(fichierLogs);
            for (String ligne : lignes) {
                String[] morceaux = ligne.split(";");
                if (morceaux.length != 6) {
                    continue;
                }

                try {
                    LocalDate date = LocalDate.parse(morceaux[0].trim());
                    String heure = morceaux[1].trim();
                    String utilisateur = morceaux[2].trim();
                    String action = morceaux[3].trim();
                    String fichier = morceaux[4].trim();
                    boolean resultat = "OK".equals(morceaux[5].trim());

                    logs.add(new Log(utilisateur, action, fichier, resultat, date, heure));
                } catch (DateTimeParseException e) {
                }
            }
        } catch (IOException e) {
            System.out.println("Impossible de charger les logs.");
        }
    }

    public long nombreTotalActions() {
        chargerLogs();
        return logs.stream().count();
    }

    public long nombreAccesRefuse() {
        chargerLogs();
        return logs.stream().filter(log -> !log.getResultat()).count();
    }

    public long nombreUserDistincts() {
        chargerLogs();
        return logs.stream().map(Log::getUtilisateur).distinct().count();
    }

    public Map<String, Long> nombreActionParUser() {
        chargerLogs();
        Map< String, Long> actionsParUtilisateur = logs.stream().collect(Collectors.groupingBy(Log::getUtilisateur, Collectors.counting()));
        return actionsParUtilisateur;
    }

    public void topFichierConsulter() {
        chargerLogs();
        logs.stream().collect(Collectors.groupingBy(Log::getFichier, Collectors.counting())).entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .forEach(System.out::println);

        // for (Map.Entry<String, Long> logObjet : fileConsulter.entrySet()) {
        //     System.out.println(logObjet.getKey());
        //     System.out.println(logObjet.getValue());
        // }
        // fileConsulter.entrySet().stream().sorted((l1, l2) -> l2.getValue().compareTo(l1.getValue())).limit(3).forEach(System.out::println);
        // return
        // Set
    }

    public long accessRefuseUtilisateur(String nomUser) {
        chargerLogs();
        return logs.stream().filter(log -> log.getUtilisateur().equals(nomUser) && !log.getResultat()).count();
    }

    public Optional<Map.Entry<String, Long>> actifUser() {
        chargerLogs();
        Map<String, Long> userActif = logs.stream()
                .collect(Collectors.groupingBy(
                        Log::getUtilisateur,
                        Collectors.counting()
                ));

        return userActif.entrySet().stream()
                .max(Map.Entry.comparingByValue());
    }

    public Map<String, Long> actionssParType() {
        chargerLogs();
        Map<String, Long> actionType = logs.stream().collect(Collectors.groupingBy(Log::getAction, Collectors.counting()));
        return actionType;
    }
}
