package com.flightapp.bookingservice.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.flightapp.bookingservice.dto.FlightResponse;

@FeignClient(name = "flight-service")
public interface FlightClient {
	
	@GetMapping("/api/flight/get/{id}")
    FlightResponse getFlight(@PathVariable("id") Long id);

    @PostMapping("/api/flight/book-seats/{id}")
    String bookSeats(@PathVariable("id") Long id,
                     @RequestBody List<String> seats);


    @PutMapping("/api/flight/update-seats/{flightId}/{count}")
    String updateSeats(@PathVariable("flightId") Long flightId, @PathVariable("count") Integer count);

    @PutMapping("/api/flight/rollback-seats/{flightId}/{count}")
    String rollbackSeats(@PathVariable("flightId") Long flightId, @PathVariable("count") Integer count);
    


}
