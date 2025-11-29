package com.flightapp.bookingservice.controller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.bookingservice.domain.Booking;
import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.TicketResponse;
import com.flightapp.bookingservice.feign.FlightClient;
import com.flightapp.bookingservice.repository.BookingRepository;

@RestController
@RequestMapping("/api/v1.0/flight/booking")
public class BookingController {

	private final BookingRepository repo;
	private final FlightClient flightClient;

	public BookingController(BookingRepository repo, FlightClient flightClient) {
		this.repo = repo;
		this.flightClient = flightClient;
	}

	@PostMapping("/{flightId}")
	public ResponseEntity<?> bookTicket(@PathVariable Long flightId, @RequestBody BookingRequest req) {

		Object flight = flightClient.getFlight(flightId);
		if (flight == null) {
			return ResponseEntity.badRequest().body("Invalid Flight ID");
		}

		String seatUpdateStatus = flightClient.updateSeats(flightId, req.getSeats());
		if (!"Seats Updated".equals(seatUpdateStatus)) {
			return ResponseEntity.badRequest().body(seatUpdateStatus);
		}

		Booking booking = new Booking();
		booking.setEmail(req.getEmail());
		booking.setSeats(req.getSeats());
		booking.setPassengerDetails(req.getPassengerDetails());
		booking.setAmount(req.getAmount());
		booking.setJourneyDate(req.getJourneyDate());

		booking.setPnr("PNR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
		booking.setFlightId(flightId);
		booking.setBookedAt(LocalDateTime.now());
		booking.setStatus("BOOKED");

		String ticketJson = String.format(
				"{\"pnr\":\"%s\",\"flightId\":%d,\"journeyDate\":\"%s\",\"passengers\":\"%s\"}", booking.getPnr(),
				flightId, req.getJourneyDate().toString(), req.getPassengerDetails());
		booking.setTicketJson(ticketJson);

		Booking saved = repo.save(booking);
		return ResponseEntity.ok(saved);
	}

	@GetMapping("/history/{email}")
	public ResponseEntity<List<Booking>> history(@PathVariable String email) {
		return ResponseEntity.ok(repo.findByEmail(email));
	}

	@GetMapping("/ticket/{pnr}")
	public ResponseEntity<?> getTicket(@PathVariable String pnr) {
		Booking b = repo.findByPnr(pnr);
		if (b == null)
			return ResponseEntity.notFound().build();

		return ResponseEntity.ok(b.getTicketJson());
	}

	@DeleteMapping("/cancel/{pnr}")
	public ResponseEntity<?> cancel(@PathVariable String pnr) {
		Booking b = repo.findByPnr(pnr);
		if (b == null) {
			return ResponseEntity.notFound().build();
		}

		if (b.getJourneyDate() == null) {
			return ResponseEntity.badRequest().body("Journey date not set; cannot validate cancellation window");
		}

		LocalDateTime now = LocalDateTime.now();
		LocalDate journey = b.getJourneyDate();
		LocalDateTime journeyStart = journey.atStartOfDay();

		long hoursUntilJourney = Duration.between(now, journeyStart).toHours();
		if (hoursUntilJourney < 24) {
			return ResponseEntity.badRequest().body("Cancellation allowed only 24 hours before journey");
		}

		b.setStatus("CANCELLED");
		repo.save(b);

		return ResponseEntity.ok("Cancelled: " + pnr);
	}
	
	@GetMapping("/download/{pnr}")
	public ResponseEntity<?> downloadTicket(@PathVariable String pnr){
	    Booking b = repo.findByPnr(pnr);
	    if(b==null) return ResponseEntity.notFound().build();

	    Object flightDetails = flightClient.getFlight(b.getFlightId());
	    TicketResponse resp = new TicketResponse();
	    resp.setPnr(b.getPnr());
	    resp.setEmail(b.getEmail());
	    resp.setPassengerDetails(b.getPassengerDetails());
	    resp.setSeats(b.getSeats());
	    resp.setFlightId(b.getFlightId());
	    resp.setFlightDetails(flightDetails);
	    resp.setTicketJson(b.getTicketJson());

	    return ResponseEntity.ok(resp);
	}
}
