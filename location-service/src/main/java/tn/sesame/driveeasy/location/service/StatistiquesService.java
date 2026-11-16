package tn.sesame.driveeasy.location.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.sesame.driveeasy.location.client.VehiculeClient;
import tn.sesame.driveeasy.location.dto.LocationDtos.ChiffreMois;
import tn.sesame.driveeasy.location.dto.LocationDtos.StatistiquesResponse;
import tn.sesame.driveeasy.location.dto.LocationDtos.TopVehicule;
import tn.sesame.driveeasy.location.dto.RemoteDtos.VehiculeDto;
import tn.sesame.driveeasy.location.entity.Location;
import tn.sesame.driveeasy.location.entity.StatutLocation;
import tn.sesame.driveeasy.location.repository.LocationRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Indicateurs : chiffre d'affaires, taux d'occupation du parc, véhicules les plus loués. */
@Service
public class StatistiquesService {

    private static final Logger log = LoggerFactory.getLogger(StatistiquesService.class);

    private final LocationRepository repository;
    private final VehiculeClient vehiculeClient;

    public StatistiquesService(LocationRepository repository, VehiculeClient vehiculeClient) {
        this.repository = repository;
        this.vehiculeClient = vehiculeClient;
    }

    public StatistiquesResponse calculer() {
        List<Location> toutes = repository.findAll();
        List<Location> terminees = toutes.stream().filter(l -> l.getStatut() == StatutLocation.TERMINEE).toList();
        YearMonth moisCourant = YearMonth.now();

        // Chiffre d'affaires = montant total (pénalités incluses) des locations terminées
        BigDecimal caTotal = somme(terminees);
        BigDecimal caMois = somme(terminees.stream()
                .filter(l -> YearMonth.from(l.getDateRetourEffectif()).equals(moisCourant)).toList());

        Map<StatutLocation, Long> parStatut = new EnumMap<>(StatutLocation.class);
        for (StatutLocation s : StatutLocation.values()) parStatut.put(s, 0L);
        toutes.forEach(l -> parStatut.merge(l.getStatut(), 1L, Long::sum));

        // CA des 6 derniers mois (pour le graphique du tableau de bord)
        List<ChiffreMois> parMois = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth m = moisCourant.minusMonths(i);
            parMois.add(new ChiffreMois(m.toString(), somme(terminees.stream()
                    .filter(l -> YearMonth.from(l.getDateRetourEffectif()).equals(m)).toList())));
        }

        // Données du parc : appel à vehicule-service (dégradé si indisponible)
        Map<Long, VehiculeDto> vehicules = new HashMap<>();
        Map<String, Long> vehiculesParStatut = new LinkedHashMap<>();
        BigDecimal tauxOccupation = BigDecimal.ZERO;
        try {
            vehicules = vehiculeClient.lister().stream().collect(Collectors.toMap(VehiculeDto::id, Function.identity()));
            for (String s : List.of("DISPONIBLE", "LOUE", "MAINTENANCE")) vehiculesParStatut.put(s, 0L);
            vehicules.values().forEach(v -> vehiculesParStatut.merge(v.statut(), 1L, Long::sum));
            if (!vehicules.isEmpty()) {
                tauxOccupation = BigDecimal.valueOf(vehiculesParStatut.get("LOUE") * 100.0 / vehicules.size())
                        .setScale(1, RoundingMode.HALF_UP);
            }
        } catch (RuntimeException e) {
            log.warn("Statistiques du parc indisponibles : {}", e.getMessage());
        }

        // Top 5 des véhicules les plus loués (hors annulations)
        Map<Long, List<Location>> parVehicule = toutes.stream()
                .filter(l -> l.getStatut() != StatutLocation.ANNULEE)
                .collect(Collectors.groupingBy(Location::getVehiculeId));
        final Map<Long, VehiculeDto> parc = vehicules;
        List<TopVehicule> top = parVehicule.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .limit(5)
                .map(e -> {
                    VehiculeDto v = parc.get(e.getKey());
                    return new TopVehicule(e.getKey(),
                            v != null ? v.libelle() : "Véhicule #" + e.getKey(),
                            v != null ? v.immatriculation() : null,
                            e.getValue().size(),
                            somme(e.getValue().stream().filter(l -> l.getStatut() == StatutLocation.TERMINEE).toList()));
                })
                .toList();

        return new StatistiquesResponse(caTotal, caMois, toutes.size(), parStatut, vehiculesParStatut,
                vehicules.size(), tauxOccupation, top, parMois);
    }

    private static BigDecimal somme(List<Location> locations) {
        return locations.stream().map(Location::getMontantTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
