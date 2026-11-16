package tn.sesame.driveeasy.vehicule.service;

import org.springframework.stereotype.Service;
import tn.sesame.driveeasy.vehicule.dto.StatutUpdateRequest;
import tn.sesame.driveeasy.vehicule.dto.VehiculeRequest;
import tn.sesame.driveeasy.vehicule.dto.VehiculeResponse;
import tn.sesame.driveeasy.vehicule.entity.Carburant;
import tn.sesame.driveeasy.vehicule.entity.Categorie;
import tn.sesame.driveeasy.vehicule.entity.StatutVehicule;
import tn.sesame.driveeasy.vehicule.entity.Vehicule;
import tn.sesame.driveeasy.vehicule.exception.ApiExceptions.BusinessException;
import tn.sesame.driveeasy.vehicule.exception.ApiExceptions.ConflictException;
import tn.sesame.driveeasy.vehicule.exception.ApiExceptions.NotFoundException;
import tn.sesame.driveeasy.vehicule.mapper.VehiculeMapper;
import tn.sesame.driveeasy.vehicule.repository.VehiculeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

/** Règles de gestion du parc automobile. */
@Service
public class VehiculeService {

    private final VehiculeRepository repository;

    public VehiculeService(VehiculeRepository repository) {
        this.repository = repository;
    }

    /** Liste filtrée : chaque filtre est optionnel (null = pas de filtre). */
    public List<VehiculeResponse> rechercher(Categorie categorie, Carburant carburant,
                                             StatutVehicule statut, BigDecimal prixMax) {
        return repository.findAll().stream()
                .filter(v -> categorie == null || v.getCategorie() == categorie)
                .filter(v -> carburant == null || v.getCarburant() == carburant)
                .filter(v -> statut == null || v.getStatut() == statut)
                .filter(v -> prixMax == null || v.getPrixJournalier().compareTo(prixMax) <= 0)
                .map(VehiculeMapper::toResponse)
                .toList();
    }

    public VehiculeResponse getById(Long id) {
        return VehiculeMapper.toResponse(findOrThrow(id));
    }

    public VehiculeResponse creer(VehiculeRequest request) {
        verifierAnnee(request.annee());
        repository.findByImmatriculation(request.immatriculation()).ifPresent(v -> {
            throw new ConflictException("Immatriculation déjà utilisée : " + request.immatriculation());
        });
        Vehicule vehicule = new Vehicule();
        VehiculeMapper.updateEntity(vehicule, request);
        return VehiculeMapper.toResponse(repository.save(vehicule));
    }

    public VehiculeResponse modifier(Long id, VehiculeRequest request) {
        Vehicule vehicule = findOrThrow(id);
        verifierAnnee(request.annee());
        repository.findByImmatriculation(request.immatriculation())
                .filter(autre -> !autre.getId().equals(id))
                .ifPresent(v -> {
                    throw new ConflictException("Immatriculation déjà utilisée : " + request.immatriculation());
                });
        VehiculeMapper.updateEntity(vehicule, request);
        return VehiculeMapper.toResponse(repository.save(vehicule));
    }

    /** Suppression refusée (409) si le véhicule est actuellement loué. */
    public void supprimer(Long id) {
        Vehicule vehicule = findOrThrow(id);
        if (vehicule.getStatut() == StatutVehicule.LOUE) {
            throw new ConflictException("Impossible de supprimer un véhicule actuellement loué");
        }
        repository.deleteById(id);
    }

    /** Changement de statut (appelé par location-service au départ / retour). */
    public VehiculeResponse changerStatut(Long id, StatutUpdateRequest request) {
        Vehicule vehicule = findOrThrow(id);
        if (request.statut() == StatutVehicule.LOUE && vehicule.getStatut() == StatutVehicule.MAINTENANCE) {
            throw new ConflictException("Un véhicule en maintenance ne peut pas être loué");
        }
        if (request.kilometrage() != null) {
            if (request.kilometrage() < vehicule.getKilometrage()) {
                throw new BusinessException("Le nouveau kilométrage ne peut pas être inférieur à l'actuel ("
                        + vehicule.getKilometrage() + " km)");
            }
            vehicule.setKilometrage(request.kilometrage());
        }
        vehicule.setStatut(request.statut());
        return VehiculeMapper.toResponse(repository.save(vehicule));
    }

    /**
     * Véhicules réservables sur une période : seuls les véhicules DISPONIBLE le sont
     * (un véhicule LOUE ou en MAINTENANCE ne peut pas être réservé).
     * Le chevauchement avec des réservations existantes est vérifié par location-service.
     */
    public List<VehiculeResponse> disponibles(LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut != null && dateFin != null && dateFin.isBefore(dateDebut)) {
            throw new BusinessException("La date de fin doit être postérieure à la date de début");
        }
        return rechercher(null, null, StatutVehicule.DISPONIBLE, null);
    }

    private Vehicule findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Véhicule introuvable : id=" + id));
    }

    private void verifierAnnee(Integer annee) {
        if (annee > Year.now().getValue()) {
            throw new BusinessException("L'année ne peut pas dépasser l'année courante");
        }
    }
}
