package com.flightapp.flightservice.controller;

import com.flightapp.flightservice.domain.Flight;
import com.flightapp.flightservice.repository.FlightRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/flight")
public class FlightController{
    private final FlightRepository repo;
    public FlightController(FlightRepository repo){this.repo=repo;}

    @PostMapping("/airline/inventory/add")
    public ResponseEntity<Flight> addInventory(@RequestBody Flight flight){
        Flight saved=repo.save(flight);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/search")
    public ResponseEntity<List<Flight>> search(@RequestParam String from, @RequestParam String to){
        List<Flight> list=repo.findByFromPlaceAndToPlace(from,to);
        return ResponseEntity.ok(list);
    }
    
    @GetMapping("/get/{id}")
    public ResponseEntity<Flight> getFlight(@PathVariable Long id){
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update-seats/{flightId}/{count}")
    public ResponseEntity<String> updateSeats(@PathVariable Long flightId,@PathVariable Integer count){
        Flight f = repo.findById(flightId).orElse(null);
        if(f==null) return ResponseEntity.notFound().build();

        if(f.getAvailableSeats() < count) {
            return ResponseEntity.badRequest().body("Not Enough Seats");
        }

        f.setAvailableSeats(f.getAvailableSeats() - count);
        repo.save(f);
        return ResponseEntity.ok("Seats Updated");
    }

}
