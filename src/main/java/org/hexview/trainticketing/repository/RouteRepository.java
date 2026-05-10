package org.hexview.trainticketing.repository;

import org.hexview.trainticketing.model.Route;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class RouteRepository {

    private final ConcurrentMap<String, Route> routes = new ConcurrentHashMap<>();

    public Route save(Route route) {
        routes.put(route.getId(), route);
        return route;
    }

    public Optional<Route> findById(String id) {
        return Optional.ofNullable(routes.get(id));
    }

    public Collection<Route> findAll() {
        return routes.values();
    }

    public boolean deleteById(String id) {
        return routes.remove(id) != null;
    }

    public boolean exists(String id) {
        return routes.containsKey(id);
    }
}
