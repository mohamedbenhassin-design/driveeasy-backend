package tn.sesame.driveeasy.client.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.sesame.driveeasy.client.client.LocationClient;
import tn.sesame.driveeasy.client.dto.ClientDtos.*;
import tn.sesame.driveeasy.client.entity.Client;
import tn.sesame.driveeasy.client.exception.ApiExceptions.ConflictException;
import tn.sesame.driveeasy.client.exception.ApiExceptions.NotFoundException;
import tn.sesame.driveeasy.client.mapper.ClientMapper;
import tn.sesame.driveeasy.client.repository.ClientRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Règles de gestion des clients. */
@Service
public class ClientService {

    private final ClientRepository repository;
    private final LocationClient locationClient;
    private final int ageMinimum;

    public ClientService(ClientRepository repository, LocationClient locationClient,
                         @Value("${driveeasy.regles.age-minimum:21}") int ageMinimum) {
        this.repository = repository;
        this.locationClient = locationClient;
        this.ageMinimum = ageMinimum;
    }

    /**
     * Recherche par nom / CIN / email.
     * "q" cherche dans les trois champs à la fois ; les autres paramètres filtrent un champ précis.
     */
    public List<ClientResponse> rechercher(String q, String nom, String cin, String email) {
        return repository.findAll().stream()
                .filter(c -> q == null || q.isBlank()
                        || contient(c.getNom() + " " + c.getPrenom(), q)
                        || contient(c.getCin(), q) || contient(c.getEmail(), q))
                .filter(c -> nom == null || contient(c.getNom() + " " + c.getPrenom(), nom))
                .filter(c -> cin == null || contient(c.getCin(), cin))
                .filter(c -> email == null || contient(c.getEmail(), email))
                .map(ClientMapper::toResponse)
                .toList();
    }

    public ClientResponse getById(Long id) {
        return ClientMapper.toResponse(findOrThrow(id));
    }

    public ClientResponse creer(ClientRequest request) {
        verifierUnicite(request, null);
        Client client = new Client();
        ClientMapper.updateEntity(client, request);
        client.setDateInscription(LocalDate.now());
        return ClientMapper.toResponse(repository.save(client));
    }

    public ClientResponse modifier(Long id, ClientRequest request) {
        Client client = findOrThrow(id);
        verifierUnicite(request, id);
        ClientMapper.updateEntity(client, request);
        return ClientMapper.toResponse(repository.save(client));
    }

    /** Suppression refusée (409) si le client a une location active (appel à location-service). */
    public void supprimer(Long id) {
        findOrThrow(id);
        if (locationClient.nombreLocationsActives(id) > 0) {
            throw new ConflictException("Impossible de supprimer un client ayant une location active");
        }
        repository.deleteById(id);
    }

    /** Éligibilité : âge >= 21 ans ET permis non expiré. */
    public EligibiliteResponse eligibilite(Long id) {
        Client client = findOrThrow(id);
        int age = ClientMapper.age(client);
        boolean permisValide = ClientMapper.permisValide(client);
        List<String> motifs = new ArrayList<>();
        if (age < ageMinimum) {
            motifs.add("Le client doit avoir au moins " + ageMinimum + " ans (âge actuel : " + age + ")");
        }
        if (!permisValide) {
            motifs.add("Le permis de conduire a expiré le " + client.getDateExpirationPermis());
        }
        return new EligibiliteResponse(id, motifs.isEmpty(), age, permisValide,
                client.getCategorieClient(), motifs);
    }

    /** Mise à jour de la catégorie de fidélité (pilotée par location-service). */
    public ClientResponse changerCategorie(Long id, CategorieUpdateRequest request) {
        Client client = findOrThrow(id);
        client.setCategorieClient(request.categorieClient());
        return ClientMapper.toResponse(repository.save(client));
    }

    private void verifierUnicite(ClientRequest r, Long idCourant) {
        repository.findByCin(r.cin()).filter(c -> !c.getId().equals(idCourant)).ifPresent(c -> {
            throw new ConflictException("CIN déjà utilisé : " + r.cin());
        });
        repository.findByEmail(r.email()).filter(c -> !c.getId().equals(idCourant)).ifPresent(c -> {
            throw new ConflictException("Email déjà utilisé : " + r.email());
        });
        repository.findByNumeroPermis(r.numeroPermis()).filter(c -> !c.getId().equals(idCourant)).ifPresent(c -> {
            throw new ConflictException("Numéro de permis déjà utilisé : " + r.numeroPermis());
        });
    }

    private Client findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client introuvable : id=" + id));
    }

    private static boolean contient(String valeur, String recherche) {
        return valeur != null && valeur.toLowerCase(Locale.ROOT).contains(recherche.trim().toLowerCase(Locale.ROOT));
    }
}
