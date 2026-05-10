package org.hexview.trainticketing.service;

import org.hexview.trainticketing.exception.InvalidRequestException;
import org.hexview.trainticketing.exception.NotFoundException;
import org.hexview.trainticketing.exception.OverbookingException;
import org.hexview.trainticketing.model.Booking;
import org.hexview.trainticketing.model.Station;
import org.hexview.trainticketing.model.Train;
import org.hexview.trainticketing.repository.BookingRepository;
import org.hexview.trainticketing.repository.StationRepository;
import org.hexview.trainticketing.repository.TrainRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TrainRepository trainRepo;
    private final StationRepository stationRepo;
    private final BookingRepository bookingRepo;
    private final EmailService emailService;

    public BookingService(TrainRepository trainRepo,
                          StationRepository stationRepo,
                          BookingRepository bookingRepo,
                          EmailService emailService) {
        this.trainRepo = trainRepo;
        this.stationRepo = stationRepo;
        this.bookingRepo = bookingRepo;
        this.emailService = emailService;
    }

    /**
     * Books one or more tickets, atomically per call. If any single ticket
     * would cause overbooking, the entire request fails and no ticket is saved.
     */
    public synchronized List<Booking> book(String trainId,
                                           String fromStationId,
                                           String toStationId,
                                           String passengerName,
                                           String email,
                                           int seats) {
        if (seats <= 0) {
            throw new InvalidRequestException("Seats must be positive");
        }

        Train train = trainRepo.findById(trainId)
                .orElseThrow(() -> new NotFoundException("Train not found: " + trainId));
        Station from = stationRepo.findById(fromStationId)
                .orElseThrow(() -> new NotFoundException("Station not found: " + fromStationId));
        Station to = stationRepo.findById(toStationId)
                .orElseThrow(() -> new NotFoundException("Station not found: " + toStationId));

        if (!train.serves(fromStationId, toStationId)) {
            throw new InvalidRequestException(
                    "Train " + trainId + " does not run from " + from.getName() + " to " + to.getName());
        }

        int fromIdx = train.indexOfStop(fromStationId);
        int toIdx = train.indexOfStop(toStationId);

        // Compute current load on every segment the new booking would occupy.
        List<Booking> existing = bookingRepo.findByTrainId(trainId);
        for (int seg = fromIdx; seg < toIdx; seg++) {
            int load = seats;
            for (Booking b : existing) {
                int bFrom = train.indexOfStop(b.getFromStationId());
                int bTo = train.indexOfStop(b.getToStationId());
                if (bFrom <= seg && seg < bTo) {
                    load += b.getSeats();
                }
            }
            if (load > train.getCapacity()) {
                throw new OverbookingException(
                        "Not enough seats on train " + train.getName()
                                + " between " + train.getStopTimes().get(seg).getStationId()
                                + " and " + train.getStopTimes().get(seg + 1).getStationId()
                                + " (capacity=" + train.getCapacity() + ", requested would yield " + load + ")");
            }
        }

        List<Booking> created = new ArrayList<>(seats);
        for (int i = 0; i < seats; i++) {
            // One Booking per seat keeps cancellation simple and bookkeeping
            // explicit. Each ticket reserves a single seat.
            Booking saved = bookingRepo.save(new Booking(
                    trainId, fromStationId, toStationId, passengerName, email, 1));
            created.add(saved);
        }

        sendConfirmation(train, from, to, passengerName, email, seats, created);
        return created;
    }

    public List<Booking> findByTrain(String trainId) {
        if (!trainRepo.exists(trainId)) {
            throw new NotFoundException("Train not found: " + trainId);
        }
        return bookingRepo.findByTrainId(trainId);
    }

    public Booking findById(String bookingId) {
        return bookingRepo.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));
    }

    public void cancel(String bookingId) {
        if (!bookingRepo.deleteById(bookingId)) {
            throw new NotFoundException("Booking not found: " + bookingId);
        }
    }

    private void sendConfirmation(Train train, Station from, Station to,
                                  String passengerName, String email, int seats,
                                  List<Booking> created) {
        String subject = "Booking confirmation — " + train.getName();
        StringBuilder body = new StringBuilder();
        body.append("Hello ").append(passengerName).append(",\n\n");
        body.append("Your booking is confirmed.\n\n");
        body.append("Train:     ").append(train.getName()).append(" (").append(train.getId()).append(")\n");
        body.append("From:      ").append(from.getName())
                .append(" at ").append(train.departureFrom(from.getId()).format(FMT)).append("\n");
        body.append("To:        ").append(to.getName())
                .append(" at ").append(train.arrivalAt(to.getId()).format(FMT)).append("\n");
        body.append("Tickets:   ").append(seats).append("\n");
        body.append("Booking IDs:\n");
        for (Booking b : created) {
            body.append("  - ").append(b.getId()).append("\n");
        }
        emailService.send(email, subject, body.toString());
    }
}
