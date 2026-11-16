package tn.sesame.driveeasy.location.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tn.sesame.driveeasy.location.dto.RemoteDtos.StatutVehiculeRequest;
import tn.sesame.driveeasy.location.dto.RemoteDtos.VehiculeDto;

import java.time.LocalDate;
import java.util.List;

/** Communication synchrone location-service -> vehicule-service (résolu par Eureka). */
@Component
public class VehiculeClient {

    private static final String SERVICE = "vehicule-service";
    private static final ParameterizedTypeReference<List<VehiculeDto>> LISTE = new ParameterizedTypeReference<>() {};

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public VehiculeClient(RestTemplate restTemplate,
                          @Value("${driveeasy.services.vehicule-url:http://vehicule-service}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public VehiculeDto getVehicule(Long id) {
        try {
            return restTemplate.getForObject(baseUrl + "/vehicules/{id}", VehiculeDto.class, id);
        } catch (RestClientException e) {
            throw RemoteErrors.translate(e, SERVICE, "lecture du véhicule " + id);
        }
    }

    public List<VehiculeDto> lister() {
        try {
            List<VehiculeDto> list = restTemplate.exchange(baseUrl + "/vehicules", HttpMethod.GET, null, LISTE).getBody();
            return list == null ? List.of() : list;
        } catch (RestClientException e) {
            throw RemoteErrors.translate(e, SERVICE, "liste des véhicules");
        }
    }

    public List<VehiculeDto> disponibles(LocalDate dateDebut, LocalDate dateFin) {
        try {
            List<VehiculeDto> list = restTemplate.exchange(
                    baseUrl + "/vehicules/disponibles?dateDebut={d}&dateFin={f}",
                    HttpMethod.GET, null, LISTE, dateDebut, dateFin).getBody();
            return list == null ? List.of() : list;
        } catch (RestClientException e) {
            throw RemoteErrors.translate(e, SERVICE, "véhicules disponibles");
        }
    }

    /** PATCH /vehicules/{id}/statut : LOUE au départ, DISPONIBLE au retour. */
    public VehiculeDto changerStatut(Long id, String statut, Integer kilometrage) {
        try {
            return restTemplate.exchange(baseUrl + "/vehicules/{id}/statut", HttpMethod.PATCH,
                    new HttpEntity<>(new StatutVehiculeRequest(statut, kilometrage)), VehiculeDto.class, id).getBody();
        } catch (RestClientException e) {
            throw RemoteErrors.translate(e, SERVICE, "changement de statut du véhicule " + id);
        }
    }
}
