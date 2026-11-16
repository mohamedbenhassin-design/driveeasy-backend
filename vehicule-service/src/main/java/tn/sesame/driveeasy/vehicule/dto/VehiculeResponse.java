package tn.sesame.driveeasy.vehicule.dto;

import tn.sesame.driveeasy.vehicule.entity.Boite;
import tn.sesame.driveeasy.vehicule.entity.Carburant;
import tn.sesame.driveeasy.vehicule.entity.Categorie;
import tn.sesame.driveeasy.vehicule.entity.StatutVehicule;

import java.math.BigDecimal;

/** Représentation d'un véhicule renvoyée par l'API. */
public record VehiculeResponse(
        Long id,
        String immatriculation,
        String marque,
        String modele,
        Integer annee,
        String couleur,
        Carburant carburant,
        Boite boite,
        Categorie categorie,
        Integer nbPlaces,
        Integer kilometrage,
        BigDecimal prixJournalier,
        StatutVehicule statut,
        String imageUrl
) {}
