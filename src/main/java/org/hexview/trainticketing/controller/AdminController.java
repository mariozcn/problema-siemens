package org.hexview.trainticketing.controller;

import jakarta.validation.Valid;
import org.hexview.trainticketing.dto.BookingResponse;
import org.hexview.trainticketing.dto.DelayRequest;
import org.hexview.trainticketing.dto.RouteDto;
import org.hexview.trainticketing.dto.RouteUpdateRequest;
import org.hexview.trainticketing.dto.StationDto;
import org.hexview.trainticketing.dto.StopTimeDto;
import org.hexview.trainticketing.dto.TrainDto;
import org.hexview.trainticketing.dto.TrainUpdateRequest;
import org.hexview.trainticketing.model.Route;
import org.hexview.trainticketing.model.Station;
import org.hexview.trainticketing.model.Train;
import org.hexview.trainticketing.repository.RouteRepository;
import org.hexview.trainticketing.repository.StationRepository;
import org.hexview.trainticketing.repository.TrainRepository;
import org.hexview.trainticketing.service.AdminService;
import org.hexview.trainticketing.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final BookingService bookingService;
    private final StationRepository stationRepo;
    private final RouteRepository routeRepo;
    private final TrainRepository trainRepo;

    public AdminController(AdminService adminService,
                           BookingService bookingService,
                           StationRepository stationRepo,
                           RouteRepository routeRepo,
                           TrainRepository trainRepo) {
        this.adminService = adminService;
        this.bookingService = bookingService;
        this.stationRepo = stationRepo;
        this.routeRepo = routeRepo;
        this.trainRepo = trainRepo;
    }

    // -------- Stations ---------------------------------------------------

    @GetMapping("/stations")
    public List<StationDto> listStations() {
        return stationRepo.findAll().stream().map(StationDto::from).toList();
    }

    @PostMapping("/stations")
    public ResponseEntity<StationDto> addStation(@Valid @RequestBody StationDto dto) {
        Station saved = adminService.addStation(dto.toModel());
        return ResponseEntity.status(HttpStatus.CREATED).body(StationDto.from(saved));
    }

    @PutMapping("/stations/{id}")
    public StationDto updateStation(@PathVariable String id, @Valid @RequestBody StationDto dto) {
        return StationDto.from(adminService.updateStation(id, dto.name()));
    }

    @DeleteMapping("/stations/{id}")
    public ResponseEntity<Void> removeStation(@PathVariable String id) {
        adminService.removeStation(id);
        return ResponseEntity.noContent().build();
    }

    // -------- Routes -----------------------------------------------------

    @GetMapping("/routes")
    public List<RouteDto> listRoutes() {
        return routeRepo.findAll().stream().map(RouteDto::from).toList();
    }

    @PostMapping("/routes")
    public ResponseEntity<RouteDto> addRoute(@Valid @RequestBody RouteDto dto) {
        Route saved = adminService.addRoute(dto.toModel());
        return ResponseEntity.status(HttpStatus.CREATED).body(RouteDto.from(saved));
    }

    @PutMapping("/routes/{id}")
    public RouteDto updateRoute(@PathVariable String id, @RequestBody RouteUpdateRequest req) {
        return RouteDto.from(adminService.updateRoute(id, req.name(), req.stationIds()));
    }

    @DeleteMapping("/routes/{id}")
    public ResponseEntity<Void> removeRoute(@PathVariable String id) {
        adminService.removeRoute(id);
        return ResponseEntity.noContent().build();
    }

    // -------- Trains -----------------------------------------------------

    @GetMapping("/trains")
    public List<TrainDto> listTrains() {
        return trainRepo.findAll().stream().map(TrainDto::from).toList();
    }

    @PostMapping("/trains")
    public ResponseEntity<TrainDto> addTrain(@Valid @RequestBody TrainDto dto) {
        Train saved = adminService.addTrain(dto.toModel());
        return ResponseEntity.status(HttpStatus.CREATED).body(TrainDto.from(saved));
    }

    @PutMapping("/trains/{id}")
    public TrainDto updateTrain(@PathVariable String id, @Valid @RequestBody TrainUpdateRequest req) {
        List<org.hexview.trainticketing.model.StopTime> newStops = req.stopTimes() == null
                ? null : req.stopTimes().stream().map(StopTimeDto::toModel).toList();
        return TrainDto.from(adminService.updateTrain(id, req.name(), req.capacity(), newStops));
    }

    @DeleteMapping("/trains/{id}")
    public ResponseEntity<Void> removeTrain(@PathVariable String id) {
        adminService.removeTrain(id);
        return ResponseEntity.noContent().build();
    }

    // -------- Bookings per train ----------------------------------------

    @GetMapping("/trains/{id}/bookings")
    public List<BookingResponse> trainBookings(@PathVariable String id) {
        return BookingResponse.from(bookingService.findByTrain(id));
    }

    // -------- Delays -----------------------------------------------------

    @PostMapping("/trains/{id}/delays")
    public TrainDto applyDelay(@PathVariable String id, @Valid @RequestBody DelayRequest req) {
        return TrainDto.from(adminService.applyDelay(id, Duration.ofMinutes(req.minutes())));
    }
}
