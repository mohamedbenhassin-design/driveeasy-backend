package tn.sesame.driveeasy.vehicule.repository;

import org.springframework.stereotype.Repository;
import tn.sesame.driveeasy.vehicule.entity.Vehicule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repository en mémoire (RAM) : aucune base de données n'est utilisée.
 * ConcurrentHashMap garantit la sûreté en cas d'accès concurrents ;
 * AtomicLong génère les identifiants.
 */
@Repository
public class VehiculeRepository {

    private final Map<Long, Vehicule> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public List<Vehicule> findAll() {
        List<Vehicule> list = new ArrayList<>(store.values());
        list.sort(Comparator.comparing(Vehicule::getId));
        return list;
    }

    public Optional<Vehicule> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<Vehicule> findByImmatriculation(String immatriculation) {
        return store.values().stream()
                .filter(v -> v.getImmatriculation().equalsIgnoreCase(immatriculation.trim()))
                .findFirst();
    }

    public Vehicule save(Vehicule vehicule) {
        if (vehicule.getId() == null) {
            vehicule.setId(sequence.incrementAndGet());
        }
        store.put(vehicule.getId(), vehicule);
        return vehicule;
    }

    public void deleteById(Long id) {
        store.remove(id);
    }
}
