package tn.sesame.driveeasy.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway (port 8080) : point d'entrée unique du frontend.
 * Les routes lb://nom-du-service sont résolues par Eureka avec répartition de charge.
 */
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
