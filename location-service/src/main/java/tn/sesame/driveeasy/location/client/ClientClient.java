package tn.sesame.driveeasy.location.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tn.sesame.driveeasy.location.dto.RemoteDtos.CategorieClientRequest;
import tn.sesame.driveeasy.location.dto.RemoteDtos.ClientDto;
import tn.sesame.driveeasy.location.dto.RemoteDtos.EligibiliteDto;

/** Communication synchrone location-service -> client-service (résolu par Eureka). */
@Component
public class ClientClient {

    private static final String SERVICE = "client-service";

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ClientClient(RestTemplate restTemplate,
                        @Value("${driveeasy.services.client-url:http://client-service}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public ClientDto getClient(Long id) {
        try {
            return restTemplate.getForObject(baseUrl + "/clients/{id}", ClientDto.class, id);
        } catch (RestClientException e) {
            throw RemoteErrors.translate(e, SERVICE, "lecture du client " + id);
        }
    }

    /** GET /clients/{id}/eligibilite */
    public EligibiliteDto eligibilite(Long id) {
        try {
            return restTemplate.getForObject(baseUrl + "/clients/{id}/eligibilite", EligibiliteDto.class, id);
        } catch (RestClientException e) {
            throw RemoteErrors.translate(e, SERVICE, "éligibilité du client " + id);
        }
    }

    /** PATCH /clients/{id}/categorie */
    public ClientDto changerCategorie(Long id, String categorie) {
        try {
            return restTemplate.exchange(baseUrl + "/clients/{id}/categorie", HttpMethod.PATCH,
                    new HttpEntity<>(new CategorieClientRequest(categorie)), ClientDto.class, id).getBody();
        } catch (RestClientException e) {
            throw RemoteErrors.translate(e, SERVICE, "mise à jour de la catégorie du client " + id);
        }
    }
}
