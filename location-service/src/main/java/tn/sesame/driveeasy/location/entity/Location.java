package tn.sesame.driveeasy.location.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité Location (stockée en mémoire).
 * On ne stocke que les identifiants du client et du véhicule : leurs données
 * appartiennent aux autres microservices (pas de duplication).
 */
public class Location {

    private Long id;
    private Long clientId;
    private Long vehiculeId;
    private LocalDate dateDebut;
    private LocalDate dateFinPrevue;
    private LocalDate dateRetourEffectif;
    private Integer kmDepart;
    private Integer kmRetour;
    private BigDecimal prixJournalierApplique;
    /** Remise de fidélité appliquée (montant). */
    private BigDecimal montantRemise = BigDecimal.ZERO;
    private BigDecimal montantTotal;
    private BigDecimal penalite = BigDecimal.ZERO;
    private StatutLocation statut;
    private LocalDateTime dateCreation;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public Long getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(Long vehiculeId) { this.vehiculeId = vehiculeId; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDate dateFinPrevue) { this.dateFinPrevue = dateFinPrevue; }
    public LocalDate getDateRetourEffectif() { return dateRetourEffectif; }
    public void setDateRetourEffectif(LocalDate dateRetourEffectif) { this.dateRetourEffectif = dateRetourEffectif; }
    public Integer getKmDepart() { return kmDepart; }
    public void setKmDepart(Integer kmDepart) { this.kmDepart = kmDepart; }
    public Integer getKmRetour() { return kmRetour; }
    public void setKmRetour(Integer kmRetour) { this.kmRetour = kmRetour; }
    public BigDecimal getPrixJournalierApplique() { return prixJournalierApplique; }
    public void setPrixJournalierApplique(BigDecimal prixJournalierApplique) { this.prixJournalierApplique = prixJournalierApplique; }
    public BigDecimal getMontantRemise() { return montantRemise; }
    public void setMontantRemise(BigDecimal montantRemise) { this.montantRemise = montantRemise; }
    public BigDecimal getMontantTotal() { return montantTotal; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montantTotal = montantTotal; }
    public BigDecimal getPenalite() { return penalite; }
    public void setPenalite(BigDecimal penalite) { this.penalite = penalite; }
    public StatutLocation getStatut() { return statut; }
    public void setStatut(StatutLocation statut) { this.statut = statut; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    /** Vrai si la location bloque le véhicule (réservée ou en cours). */
    public boolean estActive() {
        return statut == StatutLocation.RESERVEE || statut == StatutLocation.EN_COURS;
    }

    /** Vrai si la période [debut, fin] chevauche celle de la location. */
    public boolean chevauche(LocalDate debut, LocalDate fin) {
        return !dateDebut.isAfter(fin) && !dateFinPrevue.isBefore(debut);
    }
}
