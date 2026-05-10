package org.hexview.trainticketing.repository;

import org.hexview.trainticketing.model.Train;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class TrainRepository {

    private final ConcurrentMap<String, Train> trains = new ConcurrentHashMap<>();

    public Train save(Train train) {
        trains.put(train.getId(), train);
        return train;
    }

    public Optional<Train> findById(String id) {
        return Optional.ofNullable(trains.get(id));
    }

    public Collection<Train> findAll() {
        return trains.values();
    }

    public boolean deleteById(String id) {
        return trains.remove(id) != null;
    }

    public boolean exists(String id) {
        return trains.containsKey(id);
    }
}
