package com.flightapp.bookingservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flightapp.bookingservice.domain.Booking;
import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.TicketResponse;
import com.flightapp.bookingservice.exception.BookingException;
import com.flightapp.bookingservice.feign.FlightClient;
import com.flightapp.bookingservice.repository.BookingRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

	@Mock
	private BookingRepository repo;

	@Mock
	private FlightClient flightClient;

	@InjectMocks
	private BookingServiceImpl service;

	BookingRequest req;

	@BeforeEach
	void setup() {
		req = new BookingRequest("abc@gmail.com", 2, "John:M:30;Amy:F:20", 5000.0, LocalDate.now().plusDays(5));
	}

	@Test
	void testBookTicketSuccess() {
		when(flightClient.getFlight(1L)).thenReturn(new Object());
		when(flightClient.updateSeats(1L, 2)).thenReturn("Seats Updated");

		Booking saved = new Booking();
		saved.setPnr("SUCCESS123");

		when(repo.save(any())).thenReturn(saved);

		Booking result = service.bookTicket(1L, req);

		assertEquals("SUCCESS123", result.getPnr());
		verify(repo).save(any());
	}

	@Test
	void testBookTicketFailsSeatCountMismatch() {
		req.setSeats(1);
		assertThrows(BookingException.class, () -> service.bookTicket(1L, req));
	}

	@Test
	void testBookTicketInvalidAge() {
		req.setPassengerDetails("John:M:-5");
		assertThrows(BookingException.class, () -> service.bookTicket(1L, req));
	}

	@Test
	void testBookTicketFlightServiceDown() {
		when(flightClient.getFlight(1L)).thenThrow(new RuntimeException("down"));
		assertThrows(RuntimeException.class, () -> service.bookTicket(1L, req));
	}

	@Test
	void testBookTicketSeatUpdateFailed() {
		when(flightClient.getFlight(1L)).thenReturn(new Object());
		when(flightClient.updateSeats(1L, 2)).thenReturn("Error");
		assertThrows(BookingException.class, () -> service.bookTicket(1L, req));
	}

	@Test
	void testGetHistory() {
		when(repo.findByEmail("abc@gmail.com")).thenReturn(List.of(new Booking()));
		List<Booking> result = service.getHistory("abc@gmail.com");
		assertEquals(1, result.size());
	}

	@Test
	void testGetTicketJsonSuccess() {
		Booking b = new Booking();
		b.setTicketJson("{}");
		when(repo.findByPnr("X")).thenReturn(b);
		assertEquals("{}", service.getTicketJson("X"));
	}

	@Test
	void testGetTicketJsonNotFound() {
		when(repo.findByPnr("X")).thenReturn(null);
		assertThrows(BookingException.class, () -> service.getTicketJson("X"));
	}

	@Test
	void testCancelBookingSuccess() {
		Booking b = new Booking(1L, "P123", "abc@gmail.com", "John:M:20", 2, 10L, LocalDateTime.now(), "BOOKED", 1000.0,
				"{}", LocalDate.now().plusDays(3));

		when(repo.findByPnr("P123")).thenReturn(b);
		when(flightClient.rollbackSeats(10L, 2)).thenReturn("OK");

		String result = service.cancelBooking("P123");

		assertEquals("Cancelled: P123", result);
		assertEquals("CANCELLED", b.getStatus());
	}

	@Test
	void testCancelBookingNotFound() {
		when(repo.findByPnr("X")).thenReturn(null);
		assertThrows(BookingException.class, () -> service.cancelBooking("X"));
	}

	@Test
	void testCancelBookingOnlyBookedAllowed() {
		Booking b = new Booking();
		b.setStatus("CANCELLED");
		when(repo.findByPnr("P")).thenReturn(b);
		assertThrows(BookingException.class, () -> service.cancelBooking("P"));
	}

	@Test
	void testCancelBookingTooLate() {
		Booking b = new Booking(1L, "P123", "abc@gmail.com", "John:M:20", 1, 10L, LocalDateTime.now(), "BOOKED", 1000.0,
				"json", LocalDate.now());

		when(repo.findByPnr("P123")).thenReturn(b);

		assertThrows(BookingException.class, () -> service.cancelBooking("P123"));
	}

	@Test
	void testDownloadTicketSuccess() {
		Booking b = new Booking(1L, "PNR1", "abc@gmail.com", "P", 1, 10L, LocalDateTime.now(), "BOOKED", 1000.0, "{}",
				LocalDate.now().plusDays(1));

		when(repo.findByPnr("PNR1")).thenReturn(b);
		when(flightClient.getFlight(10L)).thenReturn(new Object());

		TicketResponse resp = service.downloadTicket("PNR1");

		assertEquals("PNR1", resp.getPnr());
	}

	@Test
	void testDownloadTicketNotFound() {
		when(repo.findByPnr("X")).thenReturn(null);
		assertThrows(BookingException.class, () -> service.downloadTicket("X"));
	}

	@Test
	void testFlightFallback() {
		Object result = service.flightFallback(10L, new RuntimeException("x"));
		assertEquals(null, result);
	}

	@Test
	void testSeatUpdateFallback() {
		String result = service.seatUpdateFallback(10L, 2, new RuntimeException("x"));
		assertEquals("Flight Service Down - Cannot Update Seats", result);
	}

	@Test
	void testRollbackFallback() {
		String result = service.rollbackFallback(10L, 2, new RuntimeException("x"));
		assertEquals("Flight Service Down - Rollback Pending", result);
	}

}
