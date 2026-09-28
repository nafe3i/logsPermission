package ma.youcode.lineperm.service;

import java.util.Map;
import java.util.Optional;
import ma.youcode.lineperm.dao.LogDao;

/**
 * Service de statistiques. Il ne charge plus access.log : chaque statistique
 * est calculee directement par SQLite au moment de la demande.
 */
public class LogAnalyzer {

    private final LogDao logDao;

    public LogAnalyzer() {
        this.logDao = new LogDao();
    }

    public long nombreTotalActions() {
        return logDao.compterTotal();
    }

    public long nombreAccesRefuse() {
        return logDao.compterRefuses();
    }

    public long nombreUserDistincts() {
        return logDao.compterUtilisateursDistincts();
    }

    public Map<String, Long> nombreActionParUser() {
        return logDao.actionsByUser();
    }

    public Map<String, Long> topFichiersConsultes(int limite) {
        return logDao.topFichiers(limite);
    }

    public long accesRefusesUtilisateur(String login) {
        return logDao.refusesByUser(login);
    }

    public Optional<Map.Entry<String, Long>> utilisateurLePlusActif() {
        return logDao.userPlusActif();
    }

    public Map<String, Long> repartitionParAction() {
        return logDao.repartitionByAction();
    }
}
