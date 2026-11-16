package tn.sesame.driveeasy.client.dto;

import jakarta.validation.constraints.*;
import tn.sesame.driveeasy.client.entity.CategorieClient;

import java.time.LocalDate;
import java.util.List;

/** Objets échangés par l'API client-service (séparés de l'entité Client). */
public final class ClientDtos {

    private ClientDtos() {}

    /** Création / modification d'un client. */
    public record ClientRequest(
            @NotBlank(message = "Le CIN est obligatoire")
            @Pattern(regexp = "\\d{8}", message = "Le CIN doit contenir 8 chiffres") String cin,
            @NotBlank(message = "Le nom est obligatoire") String nom,
            @NotBlank(message = "Le prénom est obligatoire") String prenom,
            @NotNull(message = "La date de naissance est obligatoire")
            @Past(message = "La date de naissance doit être dans le passé") LocalDate dateNaissance,
            @NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email,
            @Pattern(regexp = "^$|^\\+?[0-9 ]{8,15}$", message = "Téléphone invalide") String telephone,
            String adresse,
            @NotBlank(message = "Le numéro de permis est obligatoire") String numeroPermis,
            @NotNull(message = "La date d'expiration du permis est obligatoire") LocalDate dateExpirationPermis,
            CategorieClient categorieClient
    ) {}

    /** Client renvoyé par l'API, enrichi de champs calculés (âge, validité du permis). */
    public record ClientResponse(
            Long id,
            String cin,
            String nom,
            String prenom,
            LocalDate dateNaissance,
            int age,
            String email,
            String telephone,
            String adresse,
            String numeroPermis,
            LocalDate dateExpirationPermis,
            boolean permisValide,
            CategorieClient categorieClient,
            LocalDate dateInscription
    ) {}

    /** Résultat de GET /clients/{id}/eligibilite. */
    public record EligibiliteResponse(
            Long clientId,
            boolean eligible,
            int age,
            boolean permisValide,
            CategorieClient categorieClient,
            List<String> motifs
    ) {}

    /** Corps du PATCH /clients/{id}/categorie. */
    public record CategorieUpdateRequest(
            @NotNull(message = "La catégorie est obligatoire") CategorieClient categorieClient
    ) {}
}
