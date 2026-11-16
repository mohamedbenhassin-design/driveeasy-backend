package tn.sesame.driveeasy.location.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import tn.sesame.driveeasy.location.entity.StatutLocation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Objets échangés par l'API location-service. */
public final class LocationDtos {

    private LocationDtos() {}

    /** Création d'une réservation ou simulation de prix. */
    public record LocationRequest(
            @NotNull(message = "Le client est obligatoire") @Positive Long clientId,
            @NotNull(message = "Le véhicule est obligatoire") @Positive Long vehiculeId,
            @NotNull(message = "La date de début est obligatoire") LocalDate dateDebut,
            @NotNull(message = "La date de fin prévue est obligatoire") LocalDate dateFinPrevue
    ) {}

    /** Corps (optionnel) du PATCH /locations/{id}/depart. */
    public record DepartRequest(@PositiveOrZero Integer kmDepart) {}

    /** Corps du PATCH /locations/{id}/retour. */
    public record RetourRequest(
            @NotNull(message = "Le kilométrage de retour est obligatoire")
            @PositiveOrZero(message = "Kilométrage invalide") Integer kmRetour,
            LocalDate dateRetourEffectif
    ) {}

    /** Détail du calcul de prix (simulation ou réservation). */
    public record SimulationResponse(
            Long clientId,
            Long vehiculeId,
            LocalDate dateDebut,
            LocalDate dateFinPrevue,
            long nbJours,
            BigDecimal prixJournalier,
            String categorieVehicule,
            BigDecimal coefficientCategorie,
            BigDecimal montantBrut,
            String categorieClient,
            BigDecimal tauxRemise,
            BigDecimal montantRemise,
            BigDecimal montantTotal
    ) {}

    public record LocationResponse(
            Long id,
            Long clientId,
            Long vehiculeId,
            LocalDate dateDebut,
            LocalDate dateFinPrevue,
            LocalDate dateRetourEffectif,
            Integer kmDepart,
            Integer kmRetour,
            BigDecimal prixJournalierApplique,
            BigDecimal montantRemise,
            BigDecimal montantTotal,
            BigDecimal penalite,
            StatutLocation statut,
            LocalDateTime dateCreation
    ) {}

    /** Détail enrichi : la location + les données agrégées du client et du véhicule. */
    public record LocationDetailResponse(
            LocationResponse location,
            RemoteDtos.ClientDto client,
            RemoteDtos.VehiculeDto vehicule,
            long joursRetard,
            List<String> avertissements
    ) {}

    /** Page de résultats (pagination en mémoire). */
    public record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}

    public record LocationsActivesResponse(Long clientId, long nombre) {}

    public record TopVehicule(
            Long vehiculeId,
            String libelle,
            String immatriculation,
            long nombreLocations,
            BigDecimal chiffreAffaires
    ) {}

    public record ChiffreMois(String mois, BigDecimal montant) {}

    public record StatistiquesResponse(
            BigDecimal chiffreAffairesTotal,
            BigDecimal chiffreAffairesMois,
            long nombreLocations,
            Map<StatutLocation, Long> locationsParStatut,
            Map<String, Long> vehiculesParStatut,
            long nombreVehicules,
            BigDecimal tauxOccupation,
            List<TopVehicule> topVehicules,
            List<ChiffreMois> chiffreAffairesParMois
    ) {}
}
