package tn.sesame.driveeasy.location.service;

import org.junit.jupiter.api.Test;
import tn.sesame.driveeasy.location.config.TarificationProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests unitaires des règles de tarification (sans démarrer Spring). */
class TarificationServiceTest {

    private final TarificationService service = new TarificationService(new TarificationProperties(
            new BigDecimal("1.5"), new BigDecimal("0.05"), new BigDecimal("0.10"), 5, 15,
            Map.of("ECONOMIQUE", BigDecimal.ONE, "SUV", new BigDecimal("1.3"))));

    private final LocalDate debut = LocalDate.of(2026, 1, 10);

    @Test
    void prixStandardSansRemise() {
        var c = service.calculer(debut, debut.plusDays(3), new BigDecimal("100"), "ECONOMIQUE", "STANDARD");
        assertEquals(3, c.nbJours());
        assertEquals(new BigDecimal("300.00"), c.montantTotal());
    }

    @Test
    void prixVipAvecCoefficientSuv() {
        // 2 jours x 200 x 1.3 = 520 ; remise VIP 10 % = 52 ; total = 468
        var c = service.calculer(debut, debut.plusDays(2), new BigDecimal("200"), "SUV", "VIP");
        assertEquals(new BigDecimal("520.00"), c.montantBrut());
        assertEquals(new BigDecimal("52.00"), c.montantRemise());
        assertEquals(new BigDecimal("468.00"), c.montantTotal());
    }

    @Test
    void remiseFidele() {
        var c = service.calculer(debut, debut.plusDays(1), new BigDecimal("100"), "ECONOMIQUE", "FIDELE");
        assertEquals(new BigDecimal("95.00"), c.montantTotal());
    }

    @Test
    void locationLeMemeJourFactureeUnJour() {
        assertEquals(1, service.nombreJours(debut, debut));
    }

    @Test
    void penaliteDeRetard() {
        // 2 jours de retard x 1.5 x 100 = 300
        long retard = service.joursRetard(debut, debut.plusDays(2));
        assertEquals(2, retard);
        assertEquals(new BigDecimal("300.00"), service.penalite(new BigDecimal("100"), retard));
        assertEquals(0, service.joursRetard(debut, debut.minusDays(1)));
    }

    @Test
    void categorieFidelite() {
        assertEquals("STANDARD", service.categorieFidelite(4));
        assertEquals("FIDELE", service.categorieFidelite(5));
        assertEquals("VIP", service.categorieFidelite(15));
    }
}
