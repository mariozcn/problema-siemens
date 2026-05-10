package org.hexview.trainticketing.service;

import org.hexview.trainticketing.exception.InvalidRequestException;
import org.hexview.trainticketing.exception.NotFoundException;
import org.hexview.trainticketing.model.Booking;
import org.hexview.trainticketing.model.Route;
import org.hexview.trainticketing.model.Station;
import org.hexview.trainticketing.model.StopTime;
import org.hexview.trainticketing.model.Train;
import org.hexview.trainticketing.repository.BookingRepository;
import org.hexview.trainticketing.repository.RouteRepository;
import org.hexview.trainticketing.repository.StationRepository;
import org.hexview.trainticketing.repository.TrainRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final StationRepository stationRepo;
    private final RouteRepository routeRepo;
    private final TrainRepository trainRepo;
    private final BookingRepository bookingRepo;
    private final EmailService emailService;

    public AdminService(StationRepository stationRepo,
                        RouteRepository routeRepo,
                        TrainRepository trainRepo,
                        BookingRepository bookingRepo,
                        EmailService emailService) {
        this.stationRepo = stationRepo;
        this.routeRepo = routeRepo;
        this.trainRepo = trainRepo;
        this.bookingRepo = bookingRepo;
        this.emailService = emailService;
    }

    // -------- Stations ---------------------------------------------------

    public Station addStation(Station station) {
        if (stationRepo.exists(station.getId())) {
            throw new InvalidRequestException("Station already exists: " + station.getId());
        }
        return stationRepo.save(station);
    }

    public Station updateStation(String id, String newName) {
        Station s = stationRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Station not found: " + id));
        s.setName(newName);
        return s;
    }

    public void removeStation(String id) {
        boolean inUse = routeRepo.findAll().stream().anyMatch(r -> r.contains(id));
        if (inUse) {
            throw new InvalidRequestException("Cannot remove station " + id + ": referenced by a route");
        }
        if (!stationRepo.deleteById(id)) {
            throw new NotFoundException("Station not found: " + id);
        }
    }

    // -------- Routes -----------------------------------------------------

    public Route addRoute(Route route) {
        if (routeRepo.exists(route.getId())) {
            throw new InvalidRequestException("Route already exists: " + route.getId());
        }
        validateStations(route.getStationIds());
        return routeRepo.save(route);
    }

    public Route updateRoute(String id, String newName, List<String> newStationIds) {
        Route r = routeRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Route not found: " + id));
        if (newStationIds != null) {
            validateStations(newStationIds);
            r.setStationIds(newStationIds);
        }
        if (newName != null) {
            r.setName(newName);
        }
        return r;
    }

    public void removeRoute(String id) {
        boolean inUse = trainRepo.findAll().stream().anyMatch(t -> t.getRouteId().equals(id));
        if (inUse) {
            throw new InvalidRequestException("Cannot remove route " + id + ": referenced by a train");
        }
        if (!routeRepo.deleteById(id)) {
            throw new NotFoundException("Route not found: " + id);
        }
    }

    private void validateStations(List<String> stationIds) {
        Set<String> seen = new HashSet<>();
        for (String s : stationIds) {
            if (!stationRepo.exists(s)) {
                throw new InvalidRequestException("Unknown station referenced: " + s);
            }
            if (!seen.add(s)) {
                throw new InvalidRequestException("Duplicate station in route: " + s);
            }
        }
    }

    // -------- Trains -----------------------------------------------------

    public Train addTrain(Train train) {
        if (trainRepo.exists(train.getId())) {
            throw new InvalidRequestException("Train already exists: " + train.getId());
        }
        validateTrainAgainstRoute(train);
        return trainRepo.save(train);
    }

    public Train updateTrain(String id, String newName, Integer newCapacity, List<StopTime> newStopTimes) {
        Train t = trainRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Train not found: " + id));
        if (newName != null) {
            t.setName(newName);
        }
        if (newCapacity != null) {
            int existingMax = bookingRepo.findByTrainId(id).stream()
                    .mapToInt(Booking::getSeats).sum();
            if (newCapacity < existingMax) {
                throw new InvalidRequestException(
                        "New capacity " + newCapacity + " is below seats already booked (" + existingMax + ")");
            }
            t.setCapacity(newCapacity);
        }
        if (newStopTimes != null) {
            t.setStopTimes(newStopTimes);
            validateTrainAgainstRoute(t);
        }
        return t;
    }

    public void removeTrain(String id) {
        if (!bookingRepo.findByTrainId(id).isEmpty()) {
            throw new InvalidRequestException("Cannot remove train " + id + ": has active bookings");
        }
        if (!trainRepo.deleteById(id)) {
            throw new NotFoundException("Train not found: " + id);
        }
    }

    private void validateTrainAgainstRoute(Train train) {
        Route route = routeRepo.findById(train.getRouteId())
                .orElseThrow(() -> new InvalidRequestException("Unknown route: " + train.getRouteId()));
        List<String> routeStations = route.getStationIds();
        List<StopTime> stops = train.getStopTimes();
        if (stops.size() != routeStations.size()) {
            throw new InvalidRequestException(
                    "Train must have one stop per route station (" + routeStations.size() + " expected, "
                            + stops.size() + " provided)");
        }
        for (int i = 0; i < stops.size(); i++) {
            if (!stops.get(i).getStationId().equals(routeStations.get(i))) {
                throw new InvalidRequestException(
                        "Stop #" + i + " station mismatch: expected " + routeStations.get(i)
                                + ", got " + stops.get(i).getStationId());
            }
        }
        // Times must be strictly increasing.
        java.time.LocalDateTime prev = null;
        for (StopTime s : stops) {
            java.time.LocalDateTime arr = s.getArrival();
            java.time.LocalDateTime dep = s.getDeparture();
            if (arr != null) {
                if (prev != null && arr.isBefore(prev)) {
                    throw new InvalidRequestException("Arrival at " + s.getStationId() + " is before previous time");
                }
                prev = arr;
            }
            if (dep != null) {
                if (prev != null && dep.isBefore(prev)) {
                    throw new InvalidRequestException("Departure at " + s.getStationId() + " is before previous time");
                }
                prev = dep;
            }
        }
    }

    // -------- Delays -----------------------------------------------------

    public Train applyDelay(String trainId, Duration extraDelay) {
        Train train = trainRepo.findById(trainId)
                .orElseThrow(() -> new NotFoundException("Train not found: " + trainId));
        train.addDelay(extraDelay);
        notifyDelayedPassengers(train, extraDelay);
        return train;
    }

    private void notifyDelayedPassengers(Train train, Duration extraDelay) {
        List<Booking> affected = bookingRepo.findByTrainId(train.getId());
        Set<String> emailedAddresses = new HashSet<>();
        long minutes = extraDelay.toMinutes();

        for (Booking b : affected) {
            if (!emailedAddresses.add(b.getEmail())) {
                continue; // Email each address only once per delay event.
            }
            String subject = "Delay notice — " + train.getName();
            String body = "Hello " + b.getPassengerName() + ",\n\n"
                    + "Train " + train.getName() + " (" + train.getId() + ") has been delayed by "
                    + minutes + " minute" + (minutes == 1 ? "" : "s") + ".\n"
                    + "New scheduled departure from " + b.getFromStationId()
                    + ": " + train.departureFrom(b.getFromStationId()).format(FMT) + "\n"
                    + "New scheduled arrival at " + b.getToStationId()
                    + ": " + train.arrivalAt(b.getToStationId()).format(FMT) + "\n\n"
                    + "We apologise for the inconvenience.";
            emailService.send(b.getEmail(), subject, body);
        }
    }
}
