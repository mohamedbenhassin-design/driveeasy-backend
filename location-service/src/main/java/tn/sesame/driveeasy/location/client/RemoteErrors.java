package tn.sesame.driveeasy.location.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import tn.sesame.driveeasy.location.exception.ApiExceptions.BusinessException;
import tn.sesame.driveeasy.location.exception.ApiExceptions.ConflictException;
import tn.sesame.driveeasy.location.exception.ApiExceptions.NotFoundException;
import tn.sesame.driveeasy.location.exception.ApiExceptions.RemoteServiceException;

/**
 * Traduit les erreurs HTTP d'un service distant en exceptions locales
 * (gestion des erreurs distribuées) : 404 -> 404, 409 -> 409, 400 -> 400, autre -> 503.
 */
final class RemoteErrors {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RemoteErrors() {}

    static RuntimeException translate(RestClientException e, String service, String contexte) {
        if (e instanceof HttpStatusCodeException http) {
            String message = extraireMessage(http, contexte);
            HttpStatus status = HttpStatus.resolve(http.getStatusCode().value());
            if (status == HttpStatus.NOT_FOUND) return new NotFoundException(message);
            if (status == HttpStatus.CONFLICT) return new ConflictException(message);
            if (status == HttpStatus.BAD_REQUEST) return new BusinessException(message);
        }
        return new RemoteServiceException(service + " indisponible (" + contexte + ")");
    }

    /** Récupère le champ "message" du JSON d'erreur renvoyé par le service distant. */
    private static String extraireMessage(HttpStatusCodeException http, String defaut) {
        try {
            JsonNode node = MAPPER.readTree(http.getResponseBodyAsString());
            if (node != null && node.hasNonNull("message")) {
                return node.get("message").asText();
            }
        } catch (Exception ignored) {
            // corps non JSON : on garde le message par défaut
        }
        return defaut;
    }
}
