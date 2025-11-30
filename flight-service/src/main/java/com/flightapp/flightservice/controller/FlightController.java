package com.flightapp.flightservice.controller;

import com.flightapp.flightservice.domain.Flight;

import com.flightapp.flightservice.dto.FlightInventoryRequest;
import com.flightapp.flightservice.dto.FlightSearchRequest;
import com.flightapp.flightservice.service.FlightService;

import jakarta.validation.Valid;

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
	public ResponseEntity<?> addInventory(@Valid @RequestBody FlightInventoryRequest req) {
		Flight flight = new Flight();
		flight.setAirlineName(req.getAirlineName());
		flight.setAirlineCode(req.getAirlineCode());
		flight.setFromPlace(req.getFromPlace());
		flight.setToPlace(req.getToPlace());
		flight.setDepartureDateTime(req.getDepartureDateTime());
		flight.setArrivalDateTime(req.getArrivalDateTime());
		flight.setPrice(req.getPrice());
		flight.setAvailableSeats(req.getAvailableSeats());

		return ResponseEntity.ok(service.addInventory(flight));
	}

	@PostMapping("/search")
	public ResponseEntity<List<Flight>> search(@Valid @RequestBody FlightSearchRequest req) {
		return ResponseEntity.ok(service.search(req.getFrom(), req.getTo()));
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

	@PutMapping("/rollback-seats/{flightId}/{count}")
	public ResponseEntity<?> rollbackSeats(@PathVariable Long flightId, @PathVariable Integer count) {

		String response = service.rollbackSeats(flightId, count);

		if ("Flight Not Found".equals(response)) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(response);
	}
}