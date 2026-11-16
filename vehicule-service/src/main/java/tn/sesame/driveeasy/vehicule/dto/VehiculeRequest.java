package tn.sesame.driveeasy.vehicule.dto;

import jakarta.validation.constraints.*;
import tn.sesame.driveeasy.vehicule.entity.Boite;
import tn.sesame.driveeasy.vehicule.entity.Carburant;
import tn.sesame.driveeasy.vehicule.entity.Categorie;
import tn.sesame.driveeasy.vehicule.entity.StatutVehicule;

import java.math.BigDecimal;

/** Données reçues pour créer / modifier un véhicule (validées par Bean Validation : 400 si invalides). */
public record VehiculeRequest(
        @NotBlank(message = "L'immatriculation est obligatoire") String immatriculation,
        @NotBlank(message = "La marque est obligatoire") String marque,
        @NotBlank(message = "Le modèle est obligatoire") String modele,
        @NotNull(message = "L'année est obligatoire") @Min(value = 1980, message = "Année invalide") Integer annee,
        String couleur,
        @NotNull(message = "Le carburant est obligatoire") Carburant carburant,
        @NotNull(message = "La boîte est obligatoire") Boite boite,
        @NotNull(message = "La catégorie est obligatoire") Categorie categorie,
        @NotNull(message = "Le nombre de places est obligatoire")
        @Min(value = 2, message = "Au moins 2 places") @Max(value = 9, message = "Au plus 9 places") Integer nbPlaces,
        @NotNull(message = "Le kilométrage est obligatoire")
        @PositiveOrZero(message = "Le kilométrage doit être positif") Integer kilometrage,
        @NotNull(message = "Le prix journalier est obligatoire")
        @Positive(message = "Le prix journalier doit être strictement positif") BigDecimal prixJournalier,
        StatutVehicule statut,
        String imageUrl
) {}
