package tn.sesame.driveeasy.location.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.sesame.driveeasy.location.client.ClientClient;
import tn.sesame.driveeasy.location.client.VehiculeClient;
import tn.sesame.driveeasy.location.dto.LocationDtos.*;
import tn.sesame.driveeasy.location.dto.RemoteDtos.ClientDto;
import tn.sesame.driveeasy.location.dto.RemoteDtos.EligibiliteDto;
import tn.sesame.driveeasy.location.dto.RemoteDtos.VehiculeDto;
import tn.sesame.driveeasy.location.entity.Location;
import tn.sesame.driveeasy.location.entity.StatutLocation;
import tn.sesame.driveeasy.location.exception.ApiExceptions.BusinessException;
import tn.sesame.driveeasy.location.exception.ApiExceptions.ConflictException;
import tn.sesame.driveeasy.location.exception.ApiExceptions.NotFoundException;
import tn.sesame.driveeasy.location.mapper.LocationMapper;
import tn.sesame.driveeasy.location.repository.LocationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Cycle de vie des locations. Orchestration des appels vers client-service et vehicule-service,
 * avec compensation en cas d'échec pour garder un état cohérent.
 */
@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    private final LocationRepository repository;
    private final VehiculeClient vehiculeClient;
    private final ClientClient clientClient;
    private final TarificationService tarification;

    public LocationService(LocationRepository repository, VehiculeClient vehiculeClient,
                           ClientClient clientClient, TarificationService tarification) {
        this.repository = repository;
        this.vehiculeClient = vehiculeClient;
        this.clientClient = clientClient;
        this.tarification = tarification;
    }

    // ------------------------------------------------------------------ lecture

    /** Liste paginée et filtrée (statut, client, véhicule, période). */
    public PageResponse<LocationResponse> rechercher(StatutLocation statut, Long clientId, Long vehiculeId,
                                                     LocalDate dateDebut, LocalDate dateFin, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException("Pagination invalide : page >= 0 et 1 <= size <= 100");
        }
        LocalDate debut = dateDebut != null ? dateDebut : LocalDate.MIN;
        LocalDate fin = dateFin != null ? dateFin : LocalDate.MAX;
        List<LocationResponse> filtrees = repository.findAll().stream()
                .filter(l -> statut == null || l.getStatut() == statut)
                .filter(l -> clientId == null || l.getClientId().equals(clientId))
                .filter(l -> vehiculeId == null || l.getVehiculeId().equals(vehiculeId))
                .filter(l -> l.chevauche(debut, fin))
                .map(LocationMapper::toResponse)
                .toList();
        int total = filtrees.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponse<>(filtrees.subList(from, to), page, size, total, totalPages);
    }

    /** Détail enrichi : agrège les données du client et du véhicule (tolérant aux pannes). */
    public LocationDetailResponse detail(Long id) {
        Location l = findOrThrow(id);
        List<String> avertissements = new ArrayList<>();
        ClientDto client = null;
        VehiculeDto vehicule = null;
        try {
            client = clientClient.getClient(l.getClientId());
        } catch (RuntimeException e) {
            avertissements.add("Client indisponible : " + e.getMessage());
        }
        try {
            vehicule = vehiculeClient.getVehicule(l.getVehiculeId());
        } catch (RuntimeException e) {
            avertissements.add("Véhicule indisponible : " + e.getMessage());
        }
        LocalDate reference = l.getDateRetourEffectif() != null ? l.getDateRetourEffectif() : LocalDate.now();
        long retard = l.getStatut() == StatutLocation.EN_COURS || l.getStatut() == StatutLocation.TERMINEE
                ? tarification.joursRetard(l.getDateFinPrevue(), reference) : 0;
        return new LocationDetailResponse(LocationMapper.toResponse(l), client, vehicule, retard, avertissements);
    }

    public LocationsActivesResponse locationsActives(Long clientId) {
        long nombre = repository.findByClientId(clientId).stream().filter(Location::estActive).count();
        return new LocationsActivesResponse(clientId, nombre);
    }

    /** Véhicules DISPONIBLE (vehicule-service) sans réservation qui chevauche la période. */
    public List<VehiculeDto> vehiculesDisponibles(LocalDate debut, LocalDate fin) {
        verifierPeriode(debut, fin, false);
        Set<Long> occupes = repository.findAll().stream()
                .filter(Location::estActive)
                .filter(l -> l.chevauche(debut, fin))
                .map(Location::getVehiculeId)
                .collect(Collectors.toSet());
        return vehiculeClient.disponibles(debut, fin).stream()
                .filter(v -> !occupes.contains(v.id()))
                .toList();
    }

    // ------------------------------------------------------------------ simulation / création

    /** Simulation du prix sans création (aucune vérification de disponibilité). */
    public SimulationResponse simuler(LocationRequest request) {
        verifierPeriode(request.dateDebut(), request.dateFinPrevue(), false);
        ClientDto client = clientClient.getClient(request.clientId());
        VehiculeDto vehicule = vehiculeClient.getVehicule(request.vehiculeId());
        return construireSimulation(request, client.categorieClient(), vehicule);
    }

    /**
     * Création d'une réservation :
     * 1. le client doit être éligible (appel client-service)
     * 2. le véhicule doit être DISPONIBLE (appel vehicule-service)
     * 3. aucune autre réservation active ne doit chevaucher la période
     */
    public LocationResponse creer(LocationRequest request) {
        verifierPeriode(request.dateDebut(), request.dateFinPrevue(), true);

        EligibiliteDto eligibilite = clientClient.eligibilite(request.clientId());
        if (!eligibilite.eligible()) {
            throw new BusinessException("Client non éligible : " + String.join(" ; ", eligibilite.motifs()));
        }

        VehiculeDto vehicule = vehiculeClient.getVehicule(request.vehiculeId());
        if (!"DISPONIBLE".equals(vehicule.statut())) {
            throw new ConflictException("Véhicule non réservable (statut " + vehicule.statut() + ")");
        }
        boolean chevauchement = repository.findByVehiculeId(vehicule.id()).stream()
                .anyMatch(l -> l.estActive() && l.chevauche(request.dateDebut(), request.dateFinPrevue()));
        if (chevauchement) {
            throw new ConflictException("Le véhicule est déjà réservé sur cette période");
        }

        SimulationResponse prix = construireSimulation(request, eligibilite.categorieClient(), vehicule);
        Location location = new Location();
        location.setClientId(request.clientId());
        location.setVehiculeId(request.vehiculeId());
        location.setDateDebut(request.dateDebut());
        location.setDateFinPrevue(request.dateFinPrevue());
        location.setPrixJournalierApplique(vehicule.prixJournalier());
        location.setMontantRemise(prix.montantRemise());
        location.setMontantTotal(prix.montantTotal());
        location.setStatut(StatutLocation.RESERVEE);
        location.setDateCreation(LocalDateTime.now());
        log.info("Réservation créée pour client={} véhicule={} montant={}",
                request.clientId(), request.vehiculeId(), prix.montantTotal());
        return LocationMapper.toResponse(repository.save(location));
    }

    // ------------------------------------------------------------------ cycle de vie

    /**
     * Départ : RESERVEE -> EN_COURS, le véhicule passe à LOUE.
     * Si l'appel à vehicule-service échoue, la location reste RESERVEE (aucune modification).
     * Si l'enregistrement local échoue après le PATCH, on compense en remettant le véhicule DISPONIBLE.
     */
    public LocationResponse depart(Long id, DepartRequest request) {
        Location l = findOrThrow(id);
        if (l.getStatut() != StatutLocation.RESERVEE) {
            throw new ConflictException("Seule une location RESERVEE peut démarrer (statut actuel : " + l.getStatut() + ")");
        }
        VehiculeDto vehicule = vehiculeClient.changerStatut(l.getVehiculeId(), "LOUE", null);
        try {
            l.setKmDepart(request != null && request.kmDepart() != null ? request.kmDepart() : vehicule.kilometrage());
            l.setStatut(StatutLocation.EN_COURS);
            return LocationMapper.toResponse(repository.save(l));
        } catch (RuntimeException e) {
            log.error("Échec local après passage du véhicule à LOUE : compensation", e);
            l.setStatut(StatutLocation.RESERVEE);
            vehiculeClient.changerStatut(l.getVehiculeId(), "DISPONIBLE", null);
            throw e;
        }
    }

    /**
     * Retour : EN_COURS -> TERMINEE. Calcul des pénalités de retard, véhicule remis DISPONIBLE
     * avec le nouveau kilométrage, puis mise à jour éventuelle de la catégorie de fidélité.
     */
    public LocationResponse retour(Long id, RetourRequest request) {
        Location l = findOrThrow(id);
        if (l.getStatut() != StatutLocation.EN_COURS) {
            throw new ConflictException("Seule une location EN_COURS peut être clôturée (statut actuel : " + l.getStatut() + ")");
        }
        LocalDate dateRetour = request.dateRetourEffectif() != null ? request.dateRetourEffectif() : LocalDate.now();
        if (dateRetour.isBefore(l.getDateDebut())) {
            throw new BusinessException("La date de retour ne peut pas précéder la date de début");
        }
        if (l.getKmDepart() != null && request.kmRetour() < l.getKmDepart()) {
            throw new BusinessException("Le kilométrage de retour (" + request.kmRetour()
                    + ") est inférieur au kilométrage de départ (" + l.getKmDepart() + ")");
        }

        // 1. Appel distant d'abord : si vehicule-service échoue, la location reste EN_COURS
        vehiculeClient.changerStatut(l.getVehiculeId(), "DISPONIBLE", request.kmRetour());

        // 2. Calcul final
        long retard = tarification.joursRetard(l.getDateFinPrevue(), dateRetour);
        var penalite = tarification.penalite(l.getPrixJournalierApplique(), retard);
        l.setDateRetourEffectif(dateRetour);
        l.setKmRetour(request.kmRetour());
        l.setPenalite(penalite);
        l.setMontantTotal(l.getMontantTotal().add(penalite));
        l.setStatut(StatutLocation.TERMINEE);
        repository.save(l);

        // 3. Fidélité (non bloquant : la location est déjà terminée)
        mettreAJourFidelite(l.getClientId());
        return LocationMapper.toResponse(l);
    }

    /** Annulation : seule une réservation (RESERVEE) peut être annulée. */
    public LocationResponse annuler(Long id) {
        Location l = findOrThrow(id);
        if (l.getStatut() != StatutLocation.RESERVEE) {
            throw new ConflictException("Seule une location RESERVEE peut être annulée (statut actuel : " + l.getStatut() + ")");
        }
        l.setStatut(StatutLocation.ANNULEE);
        return LocationMapper.toResponse(repository.save(l));
    }

    // ------------------------------------------------------------------ privé

    /** FIDELE après 5 locations terminées, VIP après 15 (jamais de rétrogradation). */
    private void mettreAJourFidelite(Long clientId) {
        try {
            long terminees = repository.findByClientId(clientId).stream()
                    .filter(l -> l.getStatut() == StatutLocation.TERMINEE).count();
            String cible = tarification.categorieFidelite(terminees);
            ClientDto client = clientClient.getClient(clientId);
            if (rang(cible) > rang(client.categorieClient())) {
                clientClient.changerCategorie(clientId, cible);
                log.info("Client {} promu en catégorie {}", clientId, cible);
            }
        } catch (RuntimeException e) {
            log.warn("Mise à jour de fidélité du client {} impossible : {}", clientId, e.getMessage());
        }
    }

    private static int rang(String categorie) {
        return switch (categorie == null ? "STANDARD" : categorie) {
            case "VIP" -> 2;
            case "FIDELE" -> 1;
            default -> 0;
        };
    }

    private SimulationResponse construireSimulation(LocationRequest r, String categorieClient, VehiculeDto v) {
        TarificationService.Calcul c = tarification.calculer(r.dateDebut(), r.dateFinPrevue(),
                v.prixJournalier(), v.categorie(), categorieClient);
        return new SimulationResponse(r.clientId(), r.vehiculeId(), r.dateDebut(), r.dateFinPrevue(),
                c.nbJours(), v.prixJournalier(), v.categorie(), c.coefficient(), c.montantBrut(),
                categorieClient, c.tauxRemise(), c.montantRemise(), c.montantTotal());
    }

    private void verifierPeriode(LocalDate debut, LocalDate fin, boolean creation) {
        if (debut == null || fin == null) {
            throw new BusinessException("Les dates de début et de fin sont obligatoires");
        }
        if (fin.isBefore(debut)) {
            throw new BusinessException("La date de fin prévue doit être postérieure ou égale à la date de début");
        }
        if (creation && debut.isBefore(LocalDate.now())) {
            throw new BusinessException("La date de début ne peut pas être dans le passé");
        }
    }

    Location findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location introuvable : id=" + id));
    }
}
