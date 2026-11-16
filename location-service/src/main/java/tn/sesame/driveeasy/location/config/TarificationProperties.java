package tn.sesame.driveeasy.location.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Propriétés métier externalisées dans le Config Server (location-service.yml) :
 * driveeasy.tarification.*
 */
@ConfigurationProperties(prefix = "driveeasy.tarification")
public record TarificationProperties(
        BigDecimal coefficientPenalite,
        BigDecimal remiseFidele,
        BigDecimal remiseVip,
        Integer seuilFidele,
        Integer seuilVip,
        Map<String, BigDecimal> coefficientsCategorie
) {
    /** Valeurs par défaut si une propriété est absente. */
    public TarificationProperties {
        if (coefficientPenalite == null) coefficientPenalite = new BigDecimal("1.5");
        if (remiseFidele == null) remiseFidele = new BigDecimal("0.05");
        if (remiseVip == null) remiseVip = new BigDecimal("0.10");
        if (seuilFidele == null) seuilFidele = 5;
        if (seuilVip == null) seuilVip = 15;
        if (coefficientsCategorie == null) coefficientsCategorie = Map.of();
    }

    public BigDecimal coefficientPour(String categorieVehicule) {
        if (categorieVehicule == null) return BigDecimal.ONE;
        // Recherche insensible à la casse (le binding YAML peut modifier la casse des clés)
        return coefficientsCategorie.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(categorieVehicule))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(BigDecimal.ONE);
    }

    public BigDecimal remisePour(String categorieClient) {
        if ("VIP".equals(categorieClient)) return remiseVip;
        if ("FIDELE".equals(categorieClient)) return remiseFidele;
        return BigDecimal.ZERO;
    }
}
