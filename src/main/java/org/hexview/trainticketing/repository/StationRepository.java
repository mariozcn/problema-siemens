package org.hexview.trainticketing.repository;

import org.hexview.trainticketing.model.Station;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class StationRepository {

    private final ConcurrentMap<String, Station> stations = new ConcurrentHashMap<>();

    public Station save(Station station) {
        stations.put(station.getId(), station);
        return station;
    }

    public Optional<Station> findById(String id) {
        return Optional.ofNullable(stations.get(id));
    }

    public Collection<Station> findAll() {
        return stations.values();
    }

    public boolean deleteById(String id) {
        return stations.remove(id) != null;
    }

    public boolean exists(String id) {
        return stations.containsKey(id);
    }
}
