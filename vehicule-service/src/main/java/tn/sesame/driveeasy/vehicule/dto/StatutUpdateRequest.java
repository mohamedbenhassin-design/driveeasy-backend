package tn.sesame.driveeasy.vehicule.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import tn.sesame.driveeasy.vehicule.entity.StatutVehicule;

/**
 * Corps du PATCH /vehicules/{id}/statut (appelé par location-service).
 * Le kilométrage est optionnel : il est mis à jour au retour du véhicule.
 */
public record StatutUpdateRequest(
        @NotNull(message = "Le statut est obligatoire") StatutVehicule statut,
        @PositiveOrZero(message = "Le kilométrage doit être positif") Integer kilometrage
) {}
