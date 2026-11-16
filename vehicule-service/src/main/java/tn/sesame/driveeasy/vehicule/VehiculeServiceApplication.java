package tn.sesame.driveeasy.vehicule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservice de gestion du parc automobile (port 8081).
 * S'enregistre automatiquement dans Eureka (dépendance eureka-client).
 */
@SpringBootApplication
public class VehiculeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VehiculeServiceApplication.class, args);
    }
}
