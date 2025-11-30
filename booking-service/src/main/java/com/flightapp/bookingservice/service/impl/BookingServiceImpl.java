package com.flightapp.bookingservice.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import com.flightapp.bookingservice.domain.Booking;
import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.FlightResponse;
import com.flightapp.bookingservice.dto.SeatResponse;
import com.flightapp.bookingservice.dto.TicketResponse;
import com.flightapp.bookingservice.feign.FlightClient;
import com.flightapp.bookingservice.repository.BookingRepository;
import com.flightapp.bookingservice.service.BookingService;

@Service
public class BookingServiceImpl implements BookingService {

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

	public Object flightFallback(Long flightId, Throwable t) {
		return null;
	}

	public String seatUpdateFallback(Long flightId, Integer seats, Throwable t) {
		return "Flight Service Down - Cannot Update Seats";
	}

	public String rollbackFallback(Long flightId, Integer seats, Throwable t) {
		return "Flight Service Down - Rollback Pending";
	}

	@Override
	public Booking bookTicket(Long flightId, BookingRequest req) {

		FlightResponse flight = flightClient.getFlight(flightId);
		if (flight == null)
			throw new RuntimeException("Flight service unavailable");

		for (String seatNum : req.getSelectedSeats()) {
			SeatResponse seat = flight.getSeats().stream().filter(s -> s.getSeatNumber().equals(seatNum)).findFirst()
					.orElse(null);

			if (seat == null)
				throw new RuntimeException("Seat not found: " + seatNum);
			if (seat.isBooked())
				throw new RuntimeException("Seat already booked: " + seatNum);
		}

		String update = flightClient.bookSeats(flightId, req.getSelectedSeats());
		if (!"BOOKING_SUCCESS".equals(update)) {
			throw new RuntimeException(update);
		}

		Booking b = new Booking();
		b.setEmail(req.getEmail());
		b.setSeats(req.getSelectedSeats().size());
		b.setPassengerDetails(req.getPassengerDetails());
		b.setAmount(req.getAmount());
		b.setJourneyDate(req.getJourneyDate());
		b.setPnr("PNR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
		b.setFlightId(flightId);
		b.setBookedAt(LocalDateTime.now());
		b.setStatus("BOOKED");

		return repo.save(b);
	}

	@Override
	public List<Booking> getHistory(String email) {
		return repo.findByEmail(email);
	}

	@Override
	public String getTicketJson(String pnr) {
		Booking b = repo.findByPnr(pnr);
		if (b == null)
			throw new RuntimeException("PNR not found");
		return b.getTicketJson();
	}

	@Override
	public String cancelBooking(String pnr) {
		Booking b = repo.findByPnr(pnr);
		if (b == null) {
			throw new RuntimeException("PNR Not Found");
		}

		if (!b.getStatus().equals("BOOKED")) {
			throw new RuntimeException("Only booked tickets can be cancelled");
		}

		if (b.getJourneyDate() == null) {
			throw new RuntimeException("Journey date not set");
		}

		LocalDateTime now = LocalDateTime.now();
		LocalDate journey = b.getJourneyDate();
		long hoursUntilJourney = Duration.between(now, journey.atStartOfDay()).toHours();

		if (hoursUntilJourney < 24) {
			throw new RuntimeException("Cancellation allowed only 24 hours before journey");
		}

		b.setStatus("CANCELLED");
		repo.save(b);

		String rollbackResponse = safeRollbackSeats(b.getFlightId(), b.getSeats());
		System.out.println("Rollback response: " + rollbackResponse);

		return "Cancelled: " + pnr;
	}

	@Override
	public TicketResponse downloadTicket(String pnr) {

		Booking b = repo.findByPnr(pnr);
		if (b == null)
			throw new RuntimeException("PNR not found");

		Object flightDetails = safeGetFlight(b.getFlightId());

		TicketResponse resp = new TicketResponse();
		resp.setPnr(b.getPnr());
		resp.setEmail(b.getEmail());
		resp.setPassengerDetails(b.getPassengerDetails());
		resp.setSeats(b.getSeats());
		resp.setFlightId(b.getFlightId());
		resp.setFlightDetails(flightDetails);
		resp.setTicketJson(b.getTicketJson());

		return resp;
	}
}
