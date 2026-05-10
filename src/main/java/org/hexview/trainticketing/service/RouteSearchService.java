package org.hexview.trainticketing.service;

import org.hexview.trainticketing.exception.NoRouteException;
import org.hexview.trainticketing.exception.NotFoundException;
import org.hexview.trainticketing.model.Journey;
import org.hexview.trainticketing.model.JourneyLeg;
import org.hexview.trainticketing.model.StopTime;
import org.hexview.trainticketing.model.Train;
import org.hexview.trainticketing.repository.StationRepository;
import org.hexview.trainticketing.repository.TrainRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Finds journeys (direct or with up to {@link #MAX_CHANGEOVERS} changeovers)
 * via DFS over stop-times. Pruning by visited stations keeps the search bounded.
 */
@Service
public class RouteSearchService {

    public static final int MAX_CHANGEOVERS = 2;
    public static final Duration MIN_TRANSFER = Duration.ofMinutes(5);

    private final TrainRepository trainRepo;
    private final StationRepository stationRepo;

    public RouteSearchService(TrainRepository trainRepo, StationRepository stationRepo) {
        this.trainRepo = trainRepo;
        this.stationRepo = stationRepo;
    }

    /**
     * Returns all journeys from {@code fromStationId} to {@code toStationId}
     * departing at or after {@code earliestDeparture} (null = no lower bound).
     * Sorted by arrival time ascending. Throws {@link NoRouteException} when none exist.
     */
    public List<Journey> findJourneys(String fromStationId, String toStationId, LocalDateTime earliestDeparture) {
        if (!stationRepo.exists(fromStationId)) {
            throw new NotFoundException("Station not found: " + fromStationId);
        }
        if (!stationRepo.exists(toStationId)) {
            throw new NotFoundException("Station not found: " + toStationId);
        }
        if (fromStationId.equals(toStationId)) {
            throw new NoRouteException("Origin and destination are the same");
        }

        Collection<Train> allTrains = trainRepo.findAll();
        List<Journey> results = new ArrayList<>();
        List<JourneyLeg> path = new ArrayList<>();
        List<String> visitedStations = new ArrayList<>();
        visitedStations.add(fromStationId);

        explore(fromStationId, toStationId, earliestDeparture, allTrains, path, visitedStations, results);

        if (results.isEmpty()) {
            throw new NoRouteException(
                    "No route found from " + fromStationId + " to " + toStationId
                            + (earliestDeparture != null ? " after " + earliestDeparture : ""));
        }
        results.sort(Comparator.comparing(Journey::arrival).thenComparingInt(Journey::changeovers));
        return results;
    }

    private void explore(String currentStation,
                         String destination,
                         LocalDateTime earliestDeparture,
                         Collection<Train> allTrains,
                         List<JourneyLeg> path,
                         List<String> visitedStations,
                         List<Journey> results) {

        if (path.size() > MAX_CHANGEOVERS + 1) {
            return;
        }

        for (Train train : allTrains) {
            int boardIdx = train.indexOfStop(currentStation);
            if (boardIdx < 0) {
                continue;
            }
            StopTime boardStop = train.getStopTimes().get(boardIdx);
            LocalDateTime boardDeparture = boardStop.getDeparture();
            if (boardDeparture == null) {
                continue; // Last stop on this train; can't board.
            }
            if (earliestDeparture != null && boardDeparture.isBefore(earliestDeparture)) {
                continue;
            }

            // Try alighting at every later stop.
            for (int alightIdx = boardIdx + 1; alightIdx < train.getStopTimes().size(); alightIdx++) {
                StopTime alightStop = train.getStopTimes().get(alightIdx);
                String alightStation = alightStop.getStationId();
                LocalDateTime arrival = alightStop.getArrival();

                if (visitedStations.contains(alightStation) && !alightStation.equals(destination)) {
                    continue;
                }

                JourneyLeg leg = new JourneyLeg(
                        train.getId(), train.getName(),
                        currentStation, alightStation,
                        boardDeparture, arrival);
                path.add(leg);

                if (alightStation.equals(destination)) {
                    results.add(new Journey(List.copyOf(path)));
                } else {
                    visitedStations.add(alightStation);
                    LocalDateTime nextEarliest = arrival.plus(MIN_TRANSFER);
                    explore(alightStation, destination, nextEarliest,
                            allTrains, path, visitedStations, results);
                    visitedStations.remove(visitedStations.size() - 1);
                }
                path.remove(path.size() - 1);
            }
        }
    }
}
