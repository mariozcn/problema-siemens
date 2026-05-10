package org.hexview.trainticketing.dto;

import org.hexview.trainticketing.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        String id,
        String trainId,
        String fromStationId,
        String toStationId,
        String passengerName,
        String email,
        int seats,
        LocalDateTime bookedAt
) {
    public static BookingResponse from(Booking b) {
        return new BookingResponse(b.getId(), b.getTrainId(), b.getFromStationId(),
                b.getToStationId(), b.getPassengerName(), b.getEmail(),
                b.getSeats(), b.getBookedAt());
    }

    public static List<BookingResponse> from(List<Booking> bookings) {
        return bookings.stream().map(BookingResponse::from).toList();
    }
}
