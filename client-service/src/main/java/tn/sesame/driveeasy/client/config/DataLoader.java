package tn.sesame.driveeasy.client.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tn.sesame.driveeasy.client.entity.CategorieClient;
import tn.sesame.driveeasy.client.entity.Client;
import tn.sesame.driveeasy.client.repository.ClientRepository;

import java.time.LocalDate;

/** Jeu de données initial des clients (en RAM). */
@Component
@ConditionalOnProperty(name = "driveeasy.datasource.seed-data", havingValue = "true", matchIfMissing = true)
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private final ClientRepository repository;

    public DataLoader(ClientRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        LocalDate today = LocalDate.now();
        add("09876543", "Ben Ali", "Sami", today.minusYears(35), "sami.benali@mail.tn", "+216 22 111 222",
                "Tunis", "P-100001", today.plusYears(4), CategorieClient.FIDELE, today.minusYears(2));
        add("11223344", "Trabelsi", "Ines", today.minusYears(28), "ines.trabelsi@mail.tn", "+216 98 333 444",
                "Sousse", "P-100002", today.plusYears(2), CategorieClient.STANDARD, today.minusMonths(8));
        add("55667788", "Gharbi", "Youssef", today.minusYears(45), "youssef.gharbi@mail.tn", "+216 55 555 666",
                "Sfax", "P-100003", today.plusYears(6), CategorieClient.VIP, today.minusYears(5));
        // Client non éligible : moins de 21 ans
        add("12121212", "Mansour", "Amira", today.minusYears(19), "amira.mansour@mail.tn", "+216 20 777 888",
                "Nabeul", "P-100004", today.plusYears(8), CategorieClient.STANDARD, today.minusMonths(1));
        // Client non éligible : permis expiré
        add("34343434", "Jaziri", "Karim", today.minusYears(40), "karim.jaziri@mail.tn", "+216 50 999 000",
                "Bizerte", "P-100005", today.minusMonths(3), CategorieClient.STANDARD, today.minusYears(1));
        add("56565656", "Hamdi", "Leila", today.minusYears(31), "leila.hamdi@mail.tn", "+216 27 123 456",
                "Ariana", "P-100006", today.plusYears(3), CategorieClient.STANDARD, today.minusMonths(4));
        log.info("Jeu de données clients chargé en mémoire");
    }

    private void add(String cin, String nom, String prenom, LocalDate naissance, String email, String tel,
                     String adresse, String permis, LocalDate expiration, CategorieClient cat, LocalDate inscription) {
        Client c = new Client();
        c.setCin(cin);
        c.setNom(nom);
        c.setPrenom(prenom);
        c.setDateNaissance(naissance);
        c.setEmail(email);
        c.setTelephone(tel);
        c.setAdresse(adresse);
        c.setNumeroPermis(permis);
        c.setDateExpirationPermis(expiration);
        c.setCategorieClient(cat);
        c.setDateInscription(inscription);
        repository.save(c);
    }
}
