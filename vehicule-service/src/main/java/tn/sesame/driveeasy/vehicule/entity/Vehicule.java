package tn.sesame.driveeasy.vehicule.entity;

import java.math.BigDecimal;

/**
 * Entité métier Vehicule (stockée en mémoire, jamais exposée directement par l'API :
 * le contrôleur échange des DTO VehiculeRequest / VehiculeResponse).
 */
public class Vehicule {

    private Long id;
    private String immatriculation;
    private String marque;
    private String modele;
    private Integer annee;
    private String couleur;
    private Carburant carburant;
    private Boite boite;
    private Categorie categorie;
    private Integer nbPlaces;
    private Integer kilometrage;
    private BigDecimal prixJournalier;
    private StatutVehicule statut;
    private String imageUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }
    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }
    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }
    public Integer getAnnee() { return annee; }
    public void setAnnee(Integer annee) { this.annee = annee; }
    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }
    public Carburant getCarburant() { return carburant; }
    public void setCarburant(Carburant carburant) { this.carburant = carburant; }
    public Boite getBoite() { return boite; }
    public void setBoite(Boite boite) { this.boite = boite; }
    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }
    public Integer getNbPlaces() { return nbPlaces; }
    public void setNbPlaces(Integer nbPlaces) { this.nbPlaces = nbPlaces; }
    public Integer getKilometrage() { return kilometrage; }
    public void setKilometrage(Integer kilometrage) { this.kilometrage = kilometrage; }
    public BigDecimal getPrixJournalier() { return prixJournalier; }
    public void setPrixJournalier(BigDecimal prixJournalier) { this.prixJournalier = prixJournalier; }
    public StatutVehicule getStatut() { return statut; }
    public void setStatut(StatutVehicule statut) { this.statut = statut; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
