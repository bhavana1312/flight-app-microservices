package com.flightapp.flightservice.controller;

import com.flightapp.flightservice.domain.Flight;
import com.flightapp.flightservice.service.FlightService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flight")
public class FlightController {

    private final FlightService service;

    public FlightController(FlightService service) {
        this.service = service;
    }

    @PostMapping("/airline/inventory/add")
    public ResponseEntity<?> addInventory(@RequestBody Flight flight) {
        return ResponseEntity.ok(service.addInventory(flight));
    }

    @PostMapping("/search")
    public ResponseEntity<List<Flight>> search(@RequestParam String from, @RequestParam String to) {
        return ResponseEntity.ok(service.search(from, to));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getFlight(@PathVariable Long id) {
        Flight flight = service.getFlight(id);
        if (flight == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(flight);
    }

    @PutMapping("/update-seats/{flightId}/{count}")
    public ResponseEntity<?> updateSeats(@PathVariable Long flightId, @PathVariable Integer count) {
        String response = service.updateSeats(flightId, count);

        if ("Flight Not Found".equals(response)) {
            return ResponseEntity.notFound().build();
        }

        if ("Not Enough Seats".equals(response)) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}
