package com.flightapp.bookingservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.mockito.MockitoAnnotations;
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

	private BookingRequest req;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);

		req = new BookingRequest("abc@gmail.com", 2, "John:M:30;Amy:F:20", 5000.0, LocalDate.now().plusDays(5));
	}

	@Test
	void testBookTicketSuccess() {
		when(flightClient.getFlight(1L)).thenReturn(new Object());
		when(flightClient.updateSeats(1L, 2)).thenReturn("Seats Updated");

		Booking saved = new Booking();
		saved.setId(1L);
		saved.setPnr("PNR-TEST1234");

		when(repo.save(any())).thenReturn(saved);

		Booking result = service.bookTicket(1L, req);

		assertNotNull(result);
		assertEquals("PNR-TEST1234", result.getPnr());
		verify(flightClient).updateSeats(1L, 2);
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
		when(flightClient.getFlight(1L)).thenThrow(new RuntimeException("Down"));

		assertThrows(BookingException.class, () -> service.bookTicket(1L, req));
	}

	@Test
	void testBookTicketSeatUpdateFailed() {
		when(flightClient.getFlight(1L)).thenReturn(new Object());
		when(flightClient.updateSeats(1L, 2)).thenReturn("Flight Service Down");

		assertThrows(BookingException.class, () -> service.bookTicket(1L, req));
	}

	@Test
	void testGetHistory() {
		List<Booking> list = List.of(new Booking());
		when(repo.findByEmail("abc@gmail.com")).thenReturn(list);

		List<Booking> result = service.getHistory("abc@gmail.com");

		assertEquals(1, result.size());
	}

	@Test
	void testGetTicketJsonSuccess() {
		Booking b = new Booking();
		b.setTicketJson("{json}");

		when(repo.findByPnr("123")).thenReturn(b);

		assertEquals("{json}", service.getTicketJson("123"));
	}

	@Test
	void testGetTicketJsonNotFound() {
		when(repo.findByPnr("X")).thenReturn(null);

		assertThrows(BookingException.class, () -> service.getTicketJson("X"));
	}

	@Test
	void testCancelBookingSuccess() {
		Booking b = new Booking(1L, "PNR123", "abc@gmail.com", "John:M:20", 2, 10L, LocalDateTime.now(), "BOOKED",
				5000.0, "json", LocalDate.now().plusDays(3));

		when(repo.findByPnr("PNR123")).thenReturn(b);
		when(flightClient.rollbackSeats(10L, 2)).thenReturn("OK");

		String result = service.cancelBooking("PNR123");

		assertEquals("Cancelled: PNR123", result);
		assertEquals("CANCELLED", b.getStatus());
		verify(repo).save(b);
	}

	@Test
	void testCancelBookingTooLate() {
		Booking b = new Booking(1L, "PNR123", "abc@gmail.com", "John:M:20", 2, 10L, LocalDateTime.now(), "BOOKED",
				5000.0, "json", LocalDate.now());

		when(repo.findByPnr("PNR123")).thenReturn(b);

		assertThrows(BookingException.class, () -> service.cancelBooking("PNR123"));
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
	void testDownloadTicketSuccess() {
		Booking b = new Booking(1L, "PNR1", "abc@gmail.com", "A", 1, 10L, LocalDateTime.now(), "BOOKED", 1000.0, "{}",
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
}
