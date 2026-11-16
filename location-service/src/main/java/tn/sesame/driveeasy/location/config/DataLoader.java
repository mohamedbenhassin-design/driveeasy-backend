package tn.sesame.driveeasy.location.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tn.sesame.driveeasy.location.entity.Location;
import tn.sesame.driveeasy.location.entity.StatutLocation;
import tn.sesame.driveeasy.location.repository.LocationRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Historique initial cohérent avec les jeux de données de vehicule-service et client-service
 * (clients 1..6, véhicules 1..8 ; le véhicule 3 est LOUE car il a une location EN_COURS).
 */
@Component
@ConditionalOnProperty(name = "driveeasy.datasource.seed-data", havingValue = "true", matchIfMissing = true)
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private final LocationRepository repository;

    public DataLoader(LocationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        LocalDate t = LocalDate.now();
        // Locations terminées (historique pour les statistiques)
        terminee(1L, 1L, t.minusMonths(5).withDayOfMonth(3), 4, 0, "90", "342.00", 35000);
        terminee(3L, 6L, t.minusMonths(4).withDayOfMonth(10), 3, 0, "320", "1296.00", 11000);
        terminee(2L, 2L, t.minusMonths(3).withDayOfMonth(5), 5, 1, "95", "617.50", 17000);
        terminee(1L, 4L, t.minusMonths(2).withDayOfMonth(12), 2, 0, "160", "364.80", 8500);
        terminee(3L, 5L, t.minusMonths(1).withDayOfMonth(8), 7, 0, "210", "1719.90", 26000);
        terminee(6L, 1L, t.minusMonths(1).withDayOfMonth(20), 3, 2, "90", "540.00", 35500);
        terminee(2L, 8L, t.withDayOfMonth(1), 2, 0, "350", "1050.00", 14500);

        // Location en cours (véhicule 3 = Golf, statut LOUE dans vehicule-service)
        Location enCours = base(1L, 3L, t.minusDays(2), t.plusDays(3), "130");
        enCours.setKmDepart(62000);
        enCours.setMontantTotal(new BigDecimal("617.50"));
        enCours.setMontantRemise(new BigDecimal("32.50"));
        enCours.setStatut(StatutLocation.EN_COURS);
        repository.save(enCours);

        // Réservation future
        Location reservee = base(2L, 4L, t.plusDays(5), t.plusDays(8), "160");
        reservee.setMontantTotal(new BigDecimal("576.00"));
        reservee.setStatut(StatutLocation.RESERVEE);
        repository.save(reservee);

        log.info("Jeu de données locations chargé en mémoire");
    }

    private void terminee(Long clientId, Long vehiculeId, LocalDate debut, int jours, int retard,
                          String prix, String total, int kmDepart) {
        Location l = base(clientId, vehiculeId, debut, debut.plusDays(jours), prix);
        l.setDateRetourEffectif(debut.plusDays(jours + retard));
        l.setKmDepart(kmDepart);
        l.setKmRetour(kmDepart + 120 * jours);
        BigDecimal penalite = new BigDecimal(prix).multiply(new BigDecimal("1.5")).multiply(BigDecimal.valueOf(retard));
        l.setPenalite(penalite);
        l.setMontantTotal(new BigDecimal(total).add(penalite));
        l.setStatut(StatutLocation.TERMINEE);
        repository.save(l);
    }

    private Location base(Long clientId, Long vehiculeId, LocalDate debut, LocalDate fin, String prix) {
        Location l = new Location();
        l.setClientId(clientId);
        l.setVehiculeId(vehiculeId);
        l.setDateDebut(debut);
        l.setDateFinPrevue(fin);
        l.setPrixJournalierApplique(new BigDecimal(prix));
        LocalDate creation = debut.minusDays(3);
        l.setDateCreation((creation.isAfter(LocalDate.now()) ? LocalDate.now() : creation).atTime(10, 0));
        return l;
    }
}
