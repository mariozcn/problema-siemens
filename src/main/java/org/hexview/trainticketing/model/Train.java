package org.hexview.trainticketing.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A scheduled train service: a route, a per-stop timetable, and capacity.
 * Each Train instance represents one specific trip.
 */
public class Train {

    private final String id;
    private String name;
    private String routeId;
    private int capacity;
    private final List<StopTime> stopTimes;
    private Duration delay = Duration.ZERO;

    public Train(String id, String name, String routeId, int capacity, List<StopTime> stopTimes) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.routeId = Objects.requireNonNull(routeId, "routeId");
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
        if (stopTimes == null || stopTimes.size() < 2) {
            throw new IllegalArgumentException("Train needs at least two stop times");
        }
        this.stopTimes = new ArrayList<>(stopTimes);
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

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = Objects.requireNonNull(routeId, "routeId");
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
    }

    public List<StopTime> getStopTimes() {
        return Collections.unmodifiableList(stopTimes);
    }

    public void setStopTimes(List<StopTime> stopTimes) {
        if (stopTimes == null || stopTimes.size() < 2) {
            throw new IllegalArgumentException("Train needs at least two stop times");
        }
        this.stopTimes.clear();
        this.stopTimes.addAll(stopTimes);
    }

    public Duration getDelay() {
        return delay;
    }

    /** Apply (or extend) a delay; shifts all stop times by the increment. */
    public void addDelay(Duration extraDelay) {
        Objects.requireNonNull(extraDelay, "extraDelay");
        if (extraDelay.isNegative()) {
            throw new IllegalArgumentException("Delay must be non-negative");
        }
        if (extraDelay.isZero()) {
            return;
        }
        this.delay = this.delay.plus(extraDelay);
        for (StopTime stop : stopTimes) {
            stop.shift(extraDelay);
        }
    }

    public LocalDateTime departureFrom(String stationId) {
        for (StopTime st : stopTimes) {
            if (st.getStationId().equals(stationId)) {
                return st.getDeparture();
            }
        }
        return null;
    }

    public LocalDateTime arrivalAt(String stationId) {
        for (StopTime st : stopTimes) {
            if (st.getStationId().equals(stationId)) {
                return st.getArrival();
            }
        }
        return null;
    }

    public int indexOfStop(String stationId) {
        for (int i = 0; i < stopTimes.size(); i++) {
            if (stopTimes.get(i).getStationId().equals(stationId)) {
                return i;
            }
        }
        return -1;
    }

    /** True when both stations are on the train AND from comes before to. */
    public boolean serves(String fromStationId, String toStationId) {
        int from = indexOfStop(fromStationId);
        int to = indexOfStop(toStationId);
        return from >= 0 && to > from;
    }
}
