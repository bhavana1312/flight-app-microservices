package com.flightapp.bookingservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.flightapp.bookingservice.domain.Booking;
import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.TicketResponse;
import com.flightapp.bookingservice.service.BookingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/flight/booking")
public class BookingController {

    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping("/{flightId}")
    public ResponseEntity<?> book(@PathVariable Long flightId, @Valid @RequestBody BookingRequest req) {
        try {
            return ResponseEntity.ok(service.bookTicket(flightId, req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history/{email}")
    public ResponseEntity<List<Booking>> history(@PathVariable String email) {
        return ResponseEntity.ok(service.getHistory(email));
    }

    @GetMapping("/ticket/{pnr}")
    public ResponseEntity<?> getTicket(@PathVariable String pnr) {
        try {
            return ResponseEntity.ok(service.getTicketJson(pnr));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/cancel/{pnr}")
    public ResponseEntity<?> cancel(@PathVariable String pnr) {
        try {
            return ResponseEntity.ok(service.cancelBooking(pnr));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/download/{pnr}")
    public ResponseEntity<?> download(@PathVariable String pnr) {
        try {
            TicketResponse resp = service.downloadTicket(pnr);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
