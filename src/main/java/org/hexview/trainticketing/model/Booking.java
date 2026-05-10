package org.hexview.trainticketing.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Booking {

    private final String id;
    private final String trainId;
    private final String fromStationId;
    private final String toStationId;
    private final String passengerName;
    private final String email;
    private final int seats;
    private final LocalDateTime bookedAt;

    public Booking(String trainId, String fromStationId, String toStationId,
                   String passengerName, String email, int seats) {
        this.id = UUID.randomUUID().toString();
        this.trainId = Objects.requireNonNull(trainId, "trainId");
        this.fromStationId = Objects.requireNonNull(fromStationId, "fromStationId");
        this.toStationId = Objects.requireNonNull(toStationId, "toStationId");
        this.passengerName = Objects.requireNonNull(passengerName, "passengerName");
        this.email = Objects.requireNonNull(email, "email");
        if (seats <= 0) {
            throw new IllegalArgumentException("Seats must be positive");
        }
        this.seats = seats;
        this.bookedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getTrainId() { return trainId; }
    public String getFromStationId() { return fromStationId; }
    public String getToStationId() { return toStationId; }
    public String getPassengerName() { return passengerName; }
    public String getEmail() { return email; }
    public int getSeats() { return seats; }
    public LocalDateTime getBookedAt() { return bookedAt; }
}
