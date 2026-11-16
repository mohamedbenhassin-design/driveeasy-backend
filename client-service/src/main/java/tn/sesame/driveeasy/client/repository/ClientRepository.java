package tn.sesame.driveeasy.client.repository;

import org.springframework.stereotype.Repository;
import tn.sesame.driveeasy.client.entity.Client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/** Repository en mémoire (RAM) des clients. */
@Repository
public class ClientRepository {

    private final Map<Long, Client> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public List<Client> findAll() {
        List<Client> list = new ArrayList<>(store.values());
        list.sort(Comparator.comparing(Client::getId));
        return list;
    }

    public Optional<Client> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<Client> findByCin(String cin) {
        return findBy(Client::getCin, cin);
    }

    public Optional<Client> findByEmail(String email) {
        return findBy(Client::getEmail, email);
    }

    public Optional<Client> findByNumeroPermis(String numeroPermis) {
        return findBy(Client::getNumeroPermis, numeroPermis);
    }

    public Client save(Client client) {
        if (client.getId() == null) {
            client.setId(sequence.incrementAndGet());
        }
        store.put(client.getId(), client);
        return client;
    }

    public void deleteById(Long id) {
        store.remove(id);
    }

    /** Recherche générique insensible à la casse sur un champ unique. */
    private Optional<Client> findBy(Function<Client, String> getter, String value) {
        if (value == null) return Optional.empty();
        return store.values().stream()
                .filter(c -> value.trim().equalsIgnoreCase(getter.apply(c)))
                .findFirst();
    }
}
