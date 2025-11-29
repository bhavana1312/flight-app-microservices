package com.flightapp.flightservice.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.flightapp.flightservice.domain.Flight;
import com.flightapp.flightservice.repository.FlightRepository;
import com.flightapp.flightservice.service.FlightService;

@Service
public class FlightServiceImpl implements FlightService {

    private final FlightRepository repo;

    public FlightServiceImpl(FlightRepository repo) {
        this.repo = repo;
    }

    @Override
    public Flight addInventory(Flight flight) {
        return repo.save(flight);
    }

    @Override
    public List<Flight> search(String from, String to) {
        return repo.findByFromPlaceAndToPlace(from, to);
    }

    @Override
    public Flight getFlight(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public String updateSeats(Long flightId, Integer count) {
        Flight f = repo.findById(flightId).orElse(null);
        if (f == null) {
            return "Flight Not Found";
        }

        if (f.getAvailableSeats() < count) {
            return "Not Enough Seats";
        }

        f.setAvailableSeats(f.getAvailableSeats() - count);
        repo.save(f);

        return "Seats Updated";
    }
}
