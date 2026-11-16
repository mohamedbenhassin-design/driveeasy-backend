package tn.sesame.driveeasy.vehicule.exception;

import java.time.LocalDateTime;
import java.util.Map;

/** Format JSON uniforme des erreurs renvoyées par l'API. */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> details
) {}
