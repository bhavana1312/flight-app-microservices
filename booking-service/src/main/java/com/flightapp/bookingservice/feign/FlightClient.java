package com.flightapp.bookingservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "flight-service")
public interface FlightClient {

	@GetMapping("/api/flight/get/{id}")
	Object getFlight(@PathVariable("id") Long id);

	@PutMapping("/api/flight/update-seats/{flightId}/{count}")
	String updateSeats(@PathVariable("flightId") Long flightId, @PathVariable("count") Integer count);

	@PutMapping("/api/flight/rollback-seats/{flightId}/{count}")
	String rollbackSeats(@PathVariable("flightId") Long flightId, @PathVariable("count") Integer count);

}