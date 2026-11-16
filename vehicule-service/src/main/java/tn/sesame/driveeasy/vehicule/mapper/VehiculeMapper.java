package tn.sesame.driveeasy.vehicule.mapper;

import tn.sesame.driveeasy.vehicule.dto.VehiculeRequest;
import tn.sesame.driveeasy.vehicule.dto.VehiculeResponse;
import tn.sesame.driveeasy.vehicule.entity.StatutVehicule;
import tn.sesame.driveeasy.vehicule.entity.Vehicule;

/** Conversion entité <-> DTO (séparation des entités et des objets échangés). */
public final class VehiculeMapper {

    private VehiculeMapper() {}

    public static VehiculeResponse toResponse(Vehicule v) {
        return new VehiculeResponse(v.getId(), v.getImmatriculation(), v.getMarque(), v.getModele(),
                v.getAnnee(), v.getCouleur(), v.getCarburant(), v.getBoite(), v.getCategorie(),
                v.getNbPlaces(), v.getKilometrage(), v.getPrixJournalier(), v.getStatut(), v.getImageUrl());
    }

    /** Copie les champs de la requête dans l'entité (création ou modification). */
    public static void updateEntity(Vehicule v, VehiculeRequest r) {
        v.setImmatriculation(r.immatriculation().trim().toUpperCase());
        v.setMarque(r.marque().trim());
        v.setModele(r.modele().trim());
        v.setAnnee(r.annee());
        v.setCouleur(r.couleur());
        v.setCarburant(r.carburant());
        v.setBoite(r.boite());
        v.setCategorie(r.categorie());
        v.setNbPlaces(r.nbPlaces());
        v.setKilometrage(r.kilometrage());
        v.setPrixJournalier(r.prixJournalier());
        v.setImageUrl(r.imageUrl());
        if (r.statut() != null) {
            v.setStatut(r.statut());
        } else if (v.getStatut() == null) {
            v.setStatut(StatutVehicule.DISPONIBLE);
        }
    }
}
