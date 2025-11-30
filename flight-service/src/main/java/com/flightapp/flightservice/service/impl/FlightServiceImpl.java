package com.flightapp.flightservice.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flightapp.flightservice.domain.Flight;
import com.flightapp.flightservice.domain.Seat;
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
        return "SEAT_COUNT_UPDATE_NOT_SUPPORTED";
    }

    @Override
    public String rollbackSeats(Long flightId, Integer count) {
        return "ROLLBACK_NOT_SUPPORTED_FOR_COUNT";
    }


    @Override
    @Transactional
    public String bookSeats(Long flightId, List<String> seatsToBook) {

        Flight flight = repo.findById(flightId).orElse(null);
        if (flight == null) {
            return "FLIGHT_NOT_FOUND";
        }

        for (String seatNum : seatsToBook) {
            Seat seat = flight.getSeats()
                    .stream()
                    .filter(s -> s.getSeatNumber().equalsIgnoreCase(seatNum))
                    .findFirst()
                    .orElse(null);

            if (seat == null) return "SEAT_NOT_FOUND";
            if (seat.isBooked()) return "SEAT_ALREADY_BOOKED";
        }

        for (String seatNum : seatsToBook) {
            Seat seat = flight.getSeats()
                    .stream()
                    .filter(s -> s.getSeatNumber().equalsIgnoreCase(seatNum))
                    .findFirst()
                    .orElseThrow();

            seat.setBooked(true);
        }

        repo.save(flight);
        return "BOOKING_SUCCESS";
    }

    @Override
    @Transactional
    public String rollbackSeatBooking(Long flightId, List<String> seatsToRelease) {

        Flight flight = repo.findById(flightId).orElse(null);
        if (flight == null) {
            return "FLIGHT_NOT_FOUND";
        }

        for (String seatNum : seatsToRelease) {
            Seat seat = flight.getSeats()
                    .stream()
                    .filter(s -> s.getSeatNumber().equalsIgnoreCase(seatNum))
                    .findFirst()
                    .orElse(null);

            if (seat == null) return "SEAT_NOT_FOUND";
            if (!seat.isBooked()) return "SEAT_ALREADY_FREE";

            seat.setBooked(false);
        }

        repo.save(flight);
        return "ROLLBACK_SUCCESS";
    }
}
