package tn.sesame.driveeasy.location.service;

import org.springframework.stereotype.Service;
import tn.sesame.driveeasy.location.config.TarificationProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Calculs de prix (sans appel distant : facile à tester unitairement).
 *
 * Prix   = nbJours x prixJournalier x coefficientCategorie - remise fidélité
 * Remise = 5 % (FIDELE) ou 10 % (VIP)
 * Pénalité = coefficientPenalite (1,5) x prixJournalier x jours de retard
 */
@Service
public class TarificationService {

    /** Résultat d'un calcul de prix. */
    public record Calcul(long nbJours, BigDecimal coefficient, BigDecimal montantBrut,
                         BigDecimal tauxRemise, BigDecimal montantRemise, BigDecimal montantTotal) {}

    private final TarificationProperties props;

    public TarificationService(TarificationProperties props) {
        this.props = props;
    }

    /** Nombre de jours facturés : au minimum 1 (location sur la journée). */
    public long nombreJours(LocalDate debut, LocalDate fin) {
        return Math.max(1, ChronoUnit.DAYS.between(debut, fin));
    }

    public Calcul calculer(LocalDate debut, LocalDate fin, BigDecimal prixJournalier,
                           String categorieVehicule, String categorieClient) {
        long nbJours = nombreJours(debut, fin);
        BigDecimal coefficient = props.coefficientPour(categorieVehicule);
        BigDecimal brut = prixJournalier.multiply(BigDecimal.valueOf(nbJours)).multiply(coefficient);
        BigDecimal taux = props.remisePour(categorieClient);
        BigDecimal remise = brut.multiply(taux);
        BigDecimal total = brut.subtract(remise);
        return new Calcul(nbJours, coefficient, arrondi(brut), taux, arrondi(remise), arrondi(total));
    }

    /** Jours de retard = jours entre la date de fin prévue et la date de retour effective. */
    public long joursRetard(LocalDate dateFinPrevue, LocalDate dateRetour) {
        return Math.max(0, ChronoUnit.DAYS.between(dateFinPrevue, dateRetour));
    }

    public BigDecimal penalite(BigDecimal prixJournalier, long joursRetard) {
        return arrondi(prixJournalier.multiply(props.coefficientPenalite()).multiply(BigDecimal.valueOf(joursRetard)));
    }

    /** Catégorie cible selon le nombre de locations terminées. */
    public String categorieFidelite(long locationsTerminees) {
        if (locationsTerminees >= props.seuilVip()) return "VIP";
        if (locationsTerminees >= props.seuilFidele()) return "FIDELE";
        return "STANDARD";
    }

    private static BigDecimal arrondi(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
