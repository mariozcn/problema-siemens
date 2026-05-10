package org.hexview.trainticketing.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Ordered sequence of stations a train follows. Holds station IDs only — the
 * timing for a specific service lives on {@link Train}.
 */
public class Route {

    private final String id;
    private String name;
    private final List<String> stationIds;

    public Route(String id, String name, List<String> stationIds) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        if (stationIds == null || stationIds.size() < 2) {
            throw new IllegalArgumentException("A route needs at least two stations");
        }
        this.stationIds = new ArrayList<>(stationIds);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public List<String> getStationIds() {
        return Collections.unmodifiableList(stationIds);
    }

    public void setStationIds(List<String> stationIds) {
        if (stationIds == null || stationIds.size() < 2) {
            throw new IllegalArgumentException("A route needs at least two stations");
        }
        this.stationIds.clear();
        this.stationIds.addAll(stationIds);
    }

    public int indexOf(String stationId) {
        return stationIds.indexOf(stationId);
    }

    public boolean contains(String stationId) {
        return stationIds.contains(stationId);
    }
}
