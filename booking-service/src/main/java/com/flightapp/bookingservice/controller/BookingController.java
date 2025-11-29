package com.flightapp.bookingservice.controller;

import com.flightapp.bookingservice.domain.Booking;
import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.feign.FlightClient;
import com.flightapp.bookingservice.repository.BookingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/flight/booking")
public class BookingController {

    private final BookingRepository repo;
    private final FlightClient flightClient;

    public BookingController(BookingRepository repo, FlightClient flightClient) {
        this.repo = repo;
        this.flightClient = flightClient;
    }

    @PostMapping("/{flightId}")
    public ResponseEntity<?> bookTicket(
            @PathVariable Long flightId,
            @RequestBody BookingRequest req) {

        Object flight = flightClient.getFlight(flightId);
        if (flight == null) {
            return ResponseEntity.badRequest().body("Invalid Flight ID");
        }

        String seatUpdateStatus = flightClient.updateSeats(flightId, req.getSeats());
        if (!seatUpdateStatus.equals("Seats Updated")) {
            return ResponseEntity.badRequest().body(seatUpdateStatus);
        }

        Booking booking = new Booking();
        booking.setEmail(req.getEmail());
        booking.setSeats(req.getSeats());
        booking.setPassengerDetails(req.getPassengerDetails());
        booking.setAmount(req.getAmount());

        booking.setPnr("PNR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        booking.setFlightId(String.valueOf(flightId));
        booking.setBookedAt(LocalDateTime.now());
        booking.setStatus("BOOKED");

        Booking saved = repo.save(booking);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/history/{email}")
    public ResponseEntity<List<Booking>> history(@PathVariable String email) {
        return ResponseEntity.ok(repo.findByEmail(email));
    }

    @GetMapping("/ticket/{pnr}")
    public ResponseEntity<Booking> getTicket(@PathVariable String pnr) {
        Booking b = repo.findByPnr(pnr);
        if (b == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(b);
    }

    @DeleteMapping("/cancel/{pnr}")
    public ResponseEntity<?> cancel(@PathVariable String pnr) {
        Booking b = repo.findByPnr(pnr);
        if (b == null) {
            return ResponseEntity.notFound().build();
        }

        b.setStatus("CANCELLED");
        repo.save(b);

        return ResponseEntity.ok("Cancelled: " + pnr);
    }
}
