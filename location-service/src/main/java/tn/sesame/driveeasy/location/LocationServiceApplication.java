package tn.sesame.driveeasy.location;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Microservice de gestion des locations (port 8083).
 * Orchestre les appels vers vehicule-service et client-service.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class LocationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LocationServiceApplication.class, args);
    }
}
