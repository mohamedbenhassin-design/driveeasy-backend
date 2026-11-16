package tn.sesame.driveeasy.client.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tn.sesame.driveeasy.client.dto.ClientDtos.*;
import tn.sesame.driveeasy.client.service.ClientService;

import java.net.URI;
import java.util.List;

/** API REST des clients. Exposée via la gateway sous /api/clients/** */
@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    /** GET /clients?q=  (ou ?nom= / ?cin= / ?email=) */
    @GetMapping
    public List<ClientResponse> rechercher(@RequestParam(required = false) String q,
                                           @RequestParam(required = false) String nom,
                                           @RequestParam(required = false) String cin,
                                           @RequestParam(required = false) String email) {
        return service.rechercher(q, nom, cin, email);
    }

    @GetMapping("/{id}")
    public ClientResponse detail(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<ClientResponse> creer(@Valid @RequestBody ClientRequest request) {
        ClientResponse created = service.creer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ClientResponse modifier(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return service.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }

    @GetMapping("/{id}/eligibilite")
    public EligibiliteResponse eligibilite(@PathVariable Long id) {
        return service.eligibilite(id);
    }

    @PatchMapping("/{id}/categorie")
    public ClientResponse changerCategorie(@PathVariable Long id, @Valid @RequestBody CategorieUpdateRequest request) {
        return service.changerCategorie(id, request);
    }
}
