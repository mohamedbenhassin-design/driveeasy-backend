package tn.sesame.driveeasy.location.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate @LoadBalanced : "http://vehicule-service" et "http://client-service" sont résolus via Eureka.
 * Construit à partir du RestTemplateBuilder de Spring Boot pour que les appels soient
 * tracés automatiquement (propagation du traceId vers Zipkin).
 */
@Configuration
public class RestConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(3))
                .readTimeout(Duration.ofSeconds(5))
                .build();
    }
}
