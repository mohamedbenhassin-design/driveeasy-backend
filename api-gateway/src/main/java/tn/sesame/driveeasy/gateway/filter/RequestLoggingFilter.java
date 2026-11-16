package tn.sesame.driveeasy.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Filtre global personnalisé :
 * - ajoute un identifiant de requête X-Request-Id (transmis aux microservices et renvoyé au client) ;
 * - mesure le temps de traitement et l'expose dans l'en-tête X-Response-Time ;
 * - journalise chaque requête : méthode, chemin, statut, durée.
 */
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    public static final String REQUEST_ID = "X-Request-Id";
    public static final String RESPONSE_TIME = "X-Response-Time";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long debut = System.currentTimeMillis();
        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        final String id = requestId;

        ServerHttpRequest request = exchange.getRequest().mutate().header(REQUEST_ID, id).build();
        ServerWebExchange mutated = exchange.mutate().request(request).build();

        // Les en-têtes de réponse doivent être ajoutés avant l'envoi de la réponse
        mutated.getResponse().beforeCommit(() -> {
            mutated.getResponse().getHeaders().set(REQUEST_ID, id);
            mutated.getResponse().getHeaders().set(RESPONSE_TIME, (System.currentTimeMillis() - debut) + "ms");
            return Mono.empty();
        });

        return chain.filter(mutated).doFinally(signal -> log.info("[{}] {} {} -> {} ({} ms)",
                id, request.getMethod(), request.getURI().getPath(),
                mutated.getResponse().getStatusCode(), System.currentTimeMillis() - debut));
    }

    /** Exécuté en premier parmi les filtres globaux. */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
