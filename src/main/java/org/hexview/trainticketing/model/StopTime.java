package org.hexview.trainticketing.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Arrival/departure timing for a single station on a train's trip.
 * The first stop has a null arrival, the last stop has a null departure.
 */
public class StopTime {

    private final String stationId;
    private LocalDateTime arrival;
    private LocalDateTime departure;

    public StopTime(String stationId, LocalDateTime arrival, LocalDateTime departure) {
        this.stationId = Objects.requireNonNull(stationId, "stationId");
        this.arrival = arrival;
        this.departure = departure;
        if (arrival == null && departure == null) {
            throw new IllegalArgumentException("Stop must have at least an arrival or a departure time");
        }
        if (arrival != null && departure != null && arrival.isAfter(departure)) {
            throw new IllegalArgumentException("Arrival cannot be after departure");
        }
    }

    public String getStationId() {
        return stationId;
    }

    public LocalDateTime getArrival() {
        return arrival;
    }

    public LocalDateTime getDeparture() {
        return departure;
    }

    public void shift(java.time.Duration delay) {
        if (arrival != null) {
            arrival = arrival.plus(delay);
        }
        if (departure != null) {
            departure = departure.plus(delay);
        }
    }
}
