package tn.sesame.driveeasy.location.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tn.sesame.driveeasy.location.dto.LocationDtos.*;
import tn.sesame.driveeasy.location.dto.RemoteDtos.VehiculeDto;
import tn.sesame.driveeasy.location.entity.StatutLocation;
import tn.sesame.driveeasy.location.service.LocationService;
import tn.sesame.driveeasy.location.service.StatistiquesService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/** API REST des locations. Exposée via la gateway sous /api/locations/** */
@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService service;
    private final StatistiquesService statistiquesService;

    public LocationController(LocationService service, StatistiquesService statistiquesService) {
        this.service = service;
        this.statistiquesService = statistiquesService;
    }

    /** GET /locations?statut=&clientId=&vehiculeId=&dateDebut=&dateFin=&page=0&size=10 */
    @GetMapping
    public PageResponse<LocationResponse> lister(
            @RequestParam(required = false) StatutLocation statut,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehiculeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.rechercher(statut, clientId, vehiculeId, dateDebut, dateFin, page, size);
    }

    @GetMapping("/statistiques")
    public StatistiquesResponse statistiques() {
        return statistiquesService.calculer();
    }

    /** Véhicules réellement libres sur une période (utilisé par l'assistant de réservation). */
    @GetMapping("/vehicules-disponibles")
    public List<VehiculeDto> vehiculesDisponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return service.vehiculesDisponibles(dateDebut, dateFin);
    }

    /** Utilisé par client-service avant la suppression d'un client. */
    @GetMapping("/clients/{clientId}/actives")
    public LocationsActivesResponse actives(@PathVariable Long clientId) {
        return service.locationsActives(clientId);
    }

    @GetMapping("/{id}")
    public LocationDetailResponse detail(@PathVariable Long id) {
        return service.detail(id);
    }

    @PostMapping
    public ResponseEntity<LocationResponse> creer(@Valid @RequestBody LocationRequest request) {
        LocationResponse created = service.creer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/simulation")
    public SimulationResponse simuler(@Valid @RequestBody LocationRequest request) {
        return service.simuler(request);
    }

    @PatchMapping("/{id}/depart")
    public LocationResponse depart(@PathVariable Long id, @Valid @RequestBody(required = false) DepartRequest request) {
        return service.depart(id, request);
    }

    @PatchMapping("/{id}/retour")
    public LocationResponse retour(@PathVariable Long id, @Valid @RequestBody RetourRequest request) {
        return service.retour(id, request);
    }

    @PatchMapping("/{id}/annulation")
    public LocationResponse annuler(@PathVariable Long id) {
        return service.annuler(id);
    }
}
