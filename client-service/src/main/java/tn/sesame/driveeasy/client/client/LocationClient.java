package tn.sesame.driveeasy.client.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tn.sesame.driveeasy.client.exception.ApiExceptions.RemoteServiceException;

/** Appel synchrone vers location-service (via Eureka). */
@Component
public class LocationClient {

    private static final Logger log = LoggerFactory.getLogger(LocationClient.class);

    /** Réponse de GET /locations/clients/{id}/actives */
    public record LocationsActives(Long clientId, long nombre) {}

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public LocationClient(RestTemplate restTemplate,
                          @Value("${driveeasy.services.location-url:http://location-service}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /** Nombre de locations RESERVEE ou EN_COURS du client. */
    public long nombreLocationsActives(Long clientId) {
        try {
            LocationsActives res = restTemplate.getForObject(
                    baseUrl + "/locations/clients/{id}/actives", LocationsActives.class, clientId);
            return res == null ? 0 : res.nombre();
        } catch (RestClientException e) {
            log.error("location-service injoignable : {}", e.getMessage());
            throw new RemoteServiceException(
                    "Impossible de vérifier les locations actives du client (location-service indisponible)");
        }
    }
}
