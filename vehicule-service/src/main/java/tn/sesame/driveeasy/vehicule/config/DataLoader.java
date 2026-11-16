package tn.sesame.driveeasy.vehicule.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tn.sesame.driveeasy.vehicule.entity.*;
import tn.sesame.driveeasy.vehicule.repository.VehiculeRepository;

import java.math.BigDecimal;

/**
 * Jeu de données initial chargé en RAM au démarrage.
 * Activé par la propriété externalisée driveeasy.datasource.seed-data=true (Config Server).
 */
@Component
@ConditionalOnProperty(name = "driveeasy.datasource.seed-data", havingValue = "true", matchIfMissing = true)
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private final VehiculeRepository repository;

    public DataLoader(VehiculeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        add("123 TU 4567", "Renault", "Clio 5", 2022, "Blanc", Carburant.ESSENCE, Boite.MANUELLE,
                Categorie.ECONOMIQUE, 5, 35000, "90", StatutVehicule.DISPONIBLE,
                "https://images.unsplash.com/photo-1609521263047-f8f205293f24?w=600");
        add("201 TU 1234", "Peugeot", "208", 2023, "Rouge", Carburant.ESSENCE, Boite.MANUELLE,
                Categorie.ECONOMIQUE, 5, 18000, "95", StatutVehicule.DISPONIBLE,
                "https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=600");
        add("198 TU 8899", "Volkswagen", "Golf 8", 2021, "Gris", Carburant.DIESEL, Boite.AUTOMATIQUE,
                Categorie.COMPACTE, 5, 62000, "130", StatutVehicule.LOUE,
                "https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=600");
        add("220 TU 5050", "Toyota", "Corolla Hybride", 2024, "Noir", Carburant.HYBRIDE, Boite.AUTOMATIQUE,
                Categorie.BERLINE, 5, 9000, "160", StatutVehicule.DISPONIBLE,
                "https://images.unsplash.com/photo-1623869675781-80aa31012a5a?w=600");
        add("215 TU 7777", "Hyundai", "Tucson", 2023, "Bleu", Carburant.DIESEL, Boite.AUTOMATIQUE,
                Categorie.SUV, 5, 27000, "210", StatutVehicule.DISPONIBLE,
                "https://images.unsplash.com/photo-1633695632011-6b2c8a4d6a1b?w=600");
        add("230 TU 1010", "Tesla", "Model 3", 2024, "Blanc", Carburant.ELECTRIQUE, Boite.AUTOMATIQUE,
                Categorie.LUXE, 5, 12000, "320", StatutVehicule.DISPONIBLE,
                "https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=600");
        add("190 TU 3344", "Dacia", "Duster", 2020, "Orange", Carburant.DIESEL, Boite.MANUELLE,
                Categorie.SUV, 5, 98000, "120", StatutVehicule.MAINTENANCE,
                "https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?w=600");
        add("225 TU 2468", "Mercedes", "Classe C", 2024, "Noir", Carburant.DIESEL, Boite.AUTOMATIQUE,
                Categorie.LUXE, 5, 15000, "350", StatutVehicule.DISPONIBLE,
                "https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=600");
        log.info("Jeu de données véhicules chargé en mémoire");
    }

    private void add(String immat, String marque, String modele, int annee, String couleur, Carburant carburant,
                     Boite boite, Categorie categorie, int places, int km, String prix, StatutVehicule statut,
                     String image) {
        Vehicule v = new Vehicule();
        v.setImmatriculation(immat);
        v.setMarque(marque);
        v.setModele(modele);
        v.setAnnee(annee);
        v.setCouleur(couleur);
        v.setCarburant(carburant);
        v.setBoite(boite);
        v.setCategorie(categorie);
        v.setNbPlaces(places);
        v.setKilometrage(km);
        v.setPrixJournalier(new BigDecimal(prix));
        v.setStatut(statut);
        v.setImageUrl(image);
        repository.save(v);
    }
}
