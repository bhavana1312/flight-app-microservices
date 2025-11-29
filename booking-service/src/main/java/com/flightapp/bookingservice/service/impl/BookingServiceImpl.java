package com.flightapp.bookingservice.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.flightapp.bookingservice.domain.Booking;
import com.flightapp.bookingservice.dto.BookingRequest;
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

	@Override
	public Booking bookTicket(Long flightId, BookingRequest req) {

		String[] passengers = req.getPassengerDetails().split(";");
		if (passengers.length != req.getSeats()) {
			throw new RuntimeException("Number of passengers must match number of seats booked");
		}

		for (String p : passengers) {
			String[] parts = p.split(":");
			int age = Integer.parseInt(parts[2]);
			if (age <= 0 || age > 120) {
				throw new RuntimeException("Invalid age for passenger: " + p);
			}
		}

		Object flight = flightClient.getFlight(flightId);
		if (flight == null) {
			throw new RuntimeException("Invalid Flight ID");
		}

		String seatUpdateStatus = flightClient.updateSeats(flightId, req.getSeats());
		if (!"Seats Updated".equals(seatUpdateStatus)) {
			throw new RuntimeException(seatUpdateStatus);
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
			throw new RuntimeException("PNR not found");
		return b.getTicketJson();
	}

	@Override
	public String cancelBooking(String pnr) {
		Booking b = repo.findByPnr(pnr);
		if (b == null) {
			throw new RuntimeException("PNR Not Found");
		}

		if (b.getJourneyDate() == null) {
			throw new RuntimeException("Journey date not set");
		}
		
		if (!b.getStatus().equals("BOOKED")) {
		    throw new RuntimeException("Only booked tickets can be cancelled");
		}

		LocalDateTime now = LocalDateTime.now();
		LocalDate journey = b.getJourneyDate();
		long hoursUntilJourney = Duration.between(now, journey.atStartOfDay()).toHours();

		if (hoursUntilJourney < 24) {
			throw new RuntimeException("Cancellation allowed only 24 hours before journey");
		}

		b.setStatus("CANCELLED");
		repo.save(b);

		return "Cancelled: " + pnr;
	}

	@Override
	public TicketResponse downloadTicket(String pnr) {

		Booking b = repo.findByPnr(pnr);
		if (b == null)
			throw new RuntimeException("PNR not found");

		Object flightDetails = flightClient.getFlight(b.getFlightId());

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
