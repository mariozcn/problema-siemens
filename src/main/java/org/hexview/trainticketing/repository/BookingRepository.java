package org.hexview.trainticketing.repository;

import org.hexview.trainticketing.model.Booking;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class BookingRepository {

    private final ConcurrentMap<String, Booking> bookings = new ConcurrentHashMap<>();

    public Booking save(Booking booking) {
        bookings.put(booking.getId(), booking);
        return booking;
    }

    public Optional<Booking> findById(String id) {
        return Optional.ofNullable(bookings.get(id));
    }

    public Collection<Booking> findAll() {
        return bookings.values();
    }

    public List<Booking> findByTrainId(String trainId) {
        return bookings.values().stream()
                .filter(b -> b.getTrainId().equals(trainId))
                .toList();
    }

    public boolean deleteById(String id) {
        return bookings.remove(id) != null;
    }
}
