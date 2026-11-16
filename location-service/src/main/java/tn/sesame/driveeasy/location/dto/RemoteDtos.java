package tn.sesame.driveeasy.location.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * Copies locales (partielles) des objets renvoyés par les autres microservices.
 * Chaque service garde son propre modèle : on ne partage pas de classes entre services.
 */
public final class RemoteDtos {

    private RemoteDtos() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VehiculeDto(
            Long id,
            String immatriculation,
            String marque,
            String modele,
            String categorie,
            String carburant,
            Integer kilometrage,
            BigDecimal prixJournalier,
            String statut,
            String imageUrl
    ) {
        public String libelle() {
            return marque + " " + modele;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ClientDto(
            Long id,
            String cin,
            String nom,
            String prenom,
            String email,
            String telephone,
            String categorieClient,
            boolean permisValide
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EligibiliteDto(
            Long clientId,
            boolean eligible,
            int age,
            boolean permisValide,
            String categorieClient,
            List<String> motifs
    ) {}

    /** Corps envoyé à PATCH /vehicules/{id}/statut */
    public record StatutVehiculeRequest(String statut, Integer kilometrage) {}

    /** Corps envoyé à PATCH /clients/{id}/categorie */
    public record CategorieClientRequest(String categorieClient) {}
}
