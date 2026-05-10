package org.hexview.trainticketing.controller;

import jakarta.validation.Valid;
import org.hexview.trainticketing.dto.BookingRequest;
import org.hexview.trainticketing.dto.BookingResponse;
import org.hexview.trainticketing.model.Booking;
import org.hexview.trainticketing.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<List<BookingResponse>> book(@Valid @RequestBody BookingRequest req) {
        List<Booking> created = bookingService.book(
                req.trainId(), req.fromStationId(), req.toStationId(),
                req.passengerName(), req.email(), req.seats());
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(created));
    }

    @GetMapping("/{id}")
    public BookingResponse getById(@PathVariable String id) {
        return BookingResponse.from(bookingService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable String id) {
        bookingService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
