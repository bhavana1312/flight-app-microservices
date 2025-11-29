package com.flightapp.bookingservice.controller;


import com.flightapp.bookingservice.domain.Booking;
import com.flightapp.bookingservice.repository.BookingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/flight/booking")
public class BookingController{
    private final BookingRepository repo;
    public BookingController(BookingRepository repo){this.repo=repo;}

    @PostMapping("/{flightId}")
    public ResponseEntity<Booking> bookTicket(@PathVariable String flightId,@RequestBody Booking booking){
        booking.setPnr("PNR-"+UUID.randomUUID().toString().substring(0,8).toUpperCase());
        booking.setFlightId(flightId);
        booking.setBookedAt(LocalDateTime.now());
        booking.setStatus("BOOKED");
        Booking saved=repo.save(booking);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/history/{email}")
    public ResponseEntity<List<Booking>> history(@PathVariable String email){
        return ResponseEntity.ok(repo.findByEmail(email));
    }

    @GetMapping("/ticket/{pnr}")
    public ResponseEntity<Booking> getTicket(@PathVariable String pnr){
        Booking b=repo.findByPnr(pnr);
        if(b==null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(b);
    }

    @DeleteMapping("/cancel/{pnr}")
    public ResponseEntity<String> cancel(@PathVariable String pnr){
        Booking b=repo.findByPnr(pnr);
        if(b==null) return ResponseEntity.notFound().build();
        b.setStatus("CANCELLED");
        repo.save(b);
        return ResponseEntity.ok("Cancelled:"+pnr);
    }
}
