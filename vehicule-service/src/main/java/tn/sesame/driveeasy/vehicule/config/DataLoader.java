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
 * Les images sont des illustrations servies par le frontend (dossier public/images/vehicules).
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
                "/images/vehicules/clio.svg");
        add("201 TU 1234", "Peugeot", "208", 2023, "Rouge", Carburant.ESSENCE, Boite.MANUELLE,
                Categorie.ECONOMIQUE, 5, 18000, "95", StatutVehicule.DISPONIBLE,
                "/images/vehicules/208.svg");
        add("198 TU 8899", "Volkswagen", "Golf 8", 2021, "Gris", Carburant.DIESEL, Boite.AUTOMATIQUE,
                Categorie.COMPACTE, 5, 62000, "130", StatutVehicule.LOUE,
                "/images/vehicules/golf.svg");
        add("220 TU 5050", "Toyota", "Corolla Hybride", 2024, "Noir", Carburant.HYBRIDE, Boite.AUTOMATIQUE,
                Categorie.BERLINE, 5, 9000, "160", StatutVehicule.DISPONIBLE,
                "/images/vehicules/corolla.svg");
        add("215 TU 7777", "Hyundai", "Tucson", 2023, "Bleu", Carburant.DIESEL, Boite.AUTOMATIQUE,
                Categorie.SUV, 5, 27000, "210", StatutVehicule.DISPONIBLE,
                "/images/vehicules/tucson.svg");
        add("230 TU 1010", "Tesla", "Model 3", 2024, "Blanc", Carburant.ELECTRIQUE, Boite.AUTOMATIQUE,
                Categorie.LUXE, 5, 12000, "320", StatutVehicule.DISPONIBLE,
                "/images/vehicules/model3.svg");
        add("190 TU 3344", "Dacia", "Duster", 2020, "Orange", Carburant.DIESEL, Boite.MANUELLE,
                Categorie.SUV, 5, 98000, "120", StatutVehicule.MAINTENANCE,
                "/images/vehicules/duster.svg");
        add("225 TU 2468", "Mercedes", "Classe C", 2024, "Noir", Carburant.DIESEL, Boite.AUTOMATIQUE,
                Categorie.LUXE, 5, 15000, "350", StatutVehicule.DISPONIBLE,
                "/images/vehicules/classe-c.svg");
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
