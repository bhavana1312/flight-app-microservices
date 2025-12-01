package com.flightapp.bookingservice.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.flightapp.bookingservice.domain.Booking;
import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.TicketResponse;
import com.flightapp.bookingservice.exception.BookingException;
import com.flightapp.bookingservice.feign.FlightClient;
import com.flightapp.bookingservice.repository.BookingRepository;
import com.flightapp.bookingservice.service.BookingService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class BookingServiceImpl implements BookingService {

	private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

	private final BookingRepository repo;
	private final FlightClient flightClient;

	public BookingServiceImpl(BookingRepository repo, FlightClient flightClient) {
		this.repo = repo;
		this.flightClient = flightClient;
	}

	@CircuitBreaker(name = "flightService", fallbackMethod = "flightFallback")
	public Object safeGetFlight(Long flightId) {
		return flightClient.getFlight(flightId);
	}

	@CircuitBreaker(name = "flightService", fallbackMethod = "seatUpdateFallback")
	@Retry(name = "flightRetry")
	public String safeUpdateSeats(Long flightId, Integer seats) {
		return flightClient.updateSeats(flightId, seats);
	}

	@CircuitBreaker(name = "flightService", fallbackMethod = "rollbackFallback")
	@Retry(name = "flightRetry")
	public String safeRollbackSeats(Long flightId, Integer seats) {
		return flightClient.rollbackSeats(flightId, seats);
	}

	public Object flightFallback(Long id, Throwable t) {
		log.error("Flight service unavailable for flightId={}, error={}", id, t.getMessage());
		return null;
	}

	public String seatUpdateFallback(Long id, Integer seatCount, Throwable t) {
		log.error("Seat update failed for flightId={}, seats={}, error={}", id, seatCount, t.getMessage());
		return "Flight Service Down - Cannot Update Seats";
	}

	public String rollbackFallback(Long id, Integer seatCount, Throwable t) {
		log.error("Rollback failed for flightId={}, seats={}, error={}", id, seatCount, t.getMessage());
		return "Flight Service Down - Rollback Pending";
	}

	@Override
	public Booking bookTicket(Long flightId, BookingRequest req) {

		String[] passengers = req.getPassengerDetails().split(";");
		if (passengers.length != req.getSeats()) {
			throw new BookingException("Number of passengers must match number of seats booked");
		}

		for (String p : passengers) {
			String[] parts = p.split(":");
			int age = Integer.parseInt(parts[2]);
			if (age <= 0 || age > 120) {
				throw new BookingException("Invalid age: " + p);
			}
		}

		Object flight = safeGetFlight(flightId);
		if (flight == null)
			throw new BookingException("Flight service unavailable. Try later.");

		String seatUpdate = safeUpdateSeats(flightId, req.getSeats());
		if (!"Seats Updated".equals(seatUpdate))
			throw new BookingException(seatUpdate);

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
				flightId, req.getJourneyDate(), req.getPassengerDetails());

		booking.setTicketJson(ticketJson);

		return repo.save(booking);
	}

	@Override
	public List<Booking> getHistory(String email) {
		return repo.findByEmail(email);
	}

	@Override
	public String getTicketJson(String pnr) {
		Booking b = repo.findByPnr(pnr);
		if (b == null)
			throw new BookingException("PNR not found");
		return b.getTicketJson();
	}

	@Override
	public String cancelBooking(String pnr) {

		Booking b = repo.findByPnr(pnr);
		if (b == null)
			throw new BookingException("PNR not found");

		if (!b.getStatus().equals("BOOKED"))
			throw new BookingException("Only booked tickets can be cancelled");

		if (b.getJourneyDate() == null)
			throw new BookingException("Journey date missing");

		long hours = Duration.between(LocalDateTime.now(), b.getJourneyDate().atStartOfDay()).toHours();
		if (hours < 24)
			throw new BookingException("Cancellation allowed only 24 hours before journey");

		b.setStatus("CANCELLED");
		repo.save(b);

		String rollback = safeRollbackSeats(b.getFlightId(), b.getSeats());
		log.info("Rollback response for pnr {}: {}", pnr, rollback);

		return "Cancelled: " + pnr;
	}

	@Override
	public TicketResponse downloadTicket(String pnr) {

		Booking b = repo.findByPnr(pnr);
		if (b == null)
			throw new BookingException("PNR not found");

		Object flight = safeGetFlight(b.getFlightId());

		TicketResponse resp = new TicketResponse();
		resp.setPnr(b.getPnr());
		resp.setEmail(b.getEmail());
		resp.setPassengerDetails(b.getPassengerDetails());
		resp.setSeats(b.getSeats());
		resp.setFlightId(b.getFlightId());
		resp.setFlightDetails(flight);
		resp.setTicketJson(b.getTicketJson());

		return resp;
	}
}
