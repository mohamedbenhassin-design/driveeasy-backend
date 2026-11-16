package tn.sesame.driveeasy.vehicule.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tn.sesame.driveeasy.vehicule.dto.StatutUpdateRequest;
import tn.sesame.driveeasy.vehicule.dto.VehiculeRequest;
import tn.sesame.driveeasy.vehicule.dto.VehiculeResponse;
import tn.sesame.driveeasy.vehicule.entity.Carburant;
import tn.sesame.driveeasy.vehicule.entity.Categorie;
import tn.sesame.driveeasy.vehicule.entity.StatutVehicule;
import tn.sesame.driveeasy.vehicule.service.VehiculeService;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/** API REST du parc automobile. Exposée via la gateway sous /api/vehicules/** */
@RestController
@RequestMapping("/vehicules")
public class VehiculeController {

    private final VehiculeService service;

    public VehiculeController(VehiculeService service) {
        this.service = service;
    }

    /** GET /vehicules?categorie=&carburant=&statut=&prixMax= : 200 */
    @GetMapping
    public List<VehiculeResponse> lister(@RequestParam(required = false) Categorie categorie,
                                         @RequestParam(required = false) Carburant carburant,
                                         @RequestParam(required = false) StatutVehicule statut,
                                         @RequestParam(required = false) BigDecimal prixMax) {
        return service.rechercher(categorie, carburant, statut, prixMax);
    }

    /** Déclaré avant /{id} pour plus de lisibilité (Spring privilégie de toute façon le chemin littéral). */
    @GetMapping("/disponibles")
    public List<VehiculeResponse> disponibles(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return service.disponibles(dateDebut, dateFin);
    }

    @GetMapping("/{id}")
    public VehiculeResponse detail(@PathVariable Long id) {
        return service.getById(id);
    }

    /** POST : 201 Created + en-tête Location */
    @PostMapping
    public ResponseEntity<VehiculeResponse> creer(@Valid @RequestBody VehiculeRequest request) {
        VehiculeResponse created = service.creer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public VehiculeResponse modifier(@PathVariable Long id, @Valid @RequestBody VehiculeRequest request) {
        return service.modifier(id, request);
    }

    /** DELETE : 204 No Content (409 si loué) */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }

    @PatchMapping("/{id}/statut")
    public VehiculeResponse changerStatut(@PathVariable Long id, @Valid @RequestBody StatutUpdateRequest request) {
        return service.changerStatut(id, request);
    }
}
