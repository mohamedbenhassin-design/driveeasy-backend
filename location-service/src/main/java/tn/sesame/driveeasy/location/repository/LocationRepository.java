package tn.sesame.driveeasy.location.repository;

import org.springframework.stereotype.Repository;
import tn.sesame.driveeasy.location.entity.Location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Repository en mémoire (RAM) des locations. */
@Repository
public class LocationRepository {

    private final Map<Long, Location> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    /** Toutes les locations, de la plus récente à la plus ancienne. */
    public List<Location> findAll() {
        List<Location> list = new ArrayList<>(store.values());
        list.sort(Comparator.comparing(Location::getId).reversed());
        return list;
    }

    public Optional<Location> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Location> findByVehiculeId(Long vehiculeId) {
        return store.values().stream().filter(l -> l.getVehiculeId().equals(vehiculeId)).toList();
    }

    public List<Location> findByClientId(Long clientId) {
        return store.values().stream().filter(l -> l.getClientId().equals(clientId)).toList();
    }

    public Location save(Location location) {
        if (location.getId() == null) {
            location.setId(sequence.incrementAndGet());
        }
        store.put(location.getId(), location);
        return location;
    }

    public void deleteById(Long id) {
        store.remove(id);
    }
}
