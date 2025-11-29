package com.flightapp.bookingservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "flight-service")
public interface FlightClient {

	@PutMapping("/api/flight/update-seats/{flightId}/{count}")
	String updateSeats(@PathVariable("flightId") Long flightId, @PathVariable("count") Integer count);

	@GetMapping("/api/flight/get/{flightId}")
	Object getFlight(@PathVariable("flightId") Long flightId);
}
