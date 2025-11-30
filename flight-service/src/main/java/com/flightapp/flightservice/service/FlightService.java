package com.flightapp.flightservice.service;

import java.util.List;

import com.flightapp.flightservice.domain.Flight;

public interface FlightService {

    Flight addInventory(Flight flight);

    List<Flight> search(String from, String to);

    Flight getFlight(Long id);

    String updateSeats(Long flightId, Integer count);

	String rollbackSeats(Long flightId, Integer count);

}
