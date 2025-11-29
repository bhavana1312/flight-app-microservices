package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.domain.Booking;
import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.TicketResponse;
import com.flightapp.bookingservice.feign.FlightClient;
import com.flightapp.bookingservice.repository.BookingRepository;
import com.flightapp.bookingservice.service.impl.BookingServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

	@Mock
	private BookingRepository repo;

	@Mock
	private FlightClient flightClient;

	@InjectMocks
	private BookingServiceImpl service;

	private BookingRequest validReq;

	@BeforeEach
	void init() {
		validReq = new BookingRequest("test@gmail.com", 1, "John:M:25", 5000.0, LocalDate.now().plusDays(3));
	}

	@Test
	void testBookTicket_Success() {
		when(flightClient.getFlight(1L)).thenReturn(new Object());
		when(flightClient.updateSeats(1L, 1)).thenReturn("Seats Updated");

		Booking saved = new Booking();
		saved.setPnr("PNR-1234");
		when(repo.save(any())).thenReturn(saved);

		Booking result = service.bookTicket(1L, validReq);

		assertNotNull(result);
		assertNotNull(result.getPnr());
		verify(repo, times(1)).save(any());
	}

	@Test
	void testBookTicket_repoReturnsNull() {
		when(flightClient.getFlight(1L)).thenReturn(new Object());
		when(flightClient.updateSeats(1L, 1)).thenReturn("Seats Updated");
		when(repo.save(any())).thenReturn(null);

		Booking result = service.bookTicket(1L, validReq);
		assertNull(result);
		verify(repo, times(1)).save(any());
	}

	@Test
	void testBookTicket_repoThrowsException() {
		when(flightClient.getFlight(1L)).thenReturn(new Object());
		when(flightClient.updateSeats(1L, 1)).thenReturn("Seats Updated");
		when(repo.save(any())).thenThrow(new RuntimeException("DB down"));

		BookingRequest req = new BookingRequest("a@mail.com", 1, "John:M:25", 1000.0, LocalDate.now().plusDays(3));
		RuntimeException ex = assertThrows(RuntimeException.class, () -> service.bookTicket(1L, req));
		assertTrue(ex.getMessage().contains("DB down"));
	}

	@Test
	void testBookTicket_FlightUnavailable() {
		when(flightClient.getFlight(1L)).thenReturn(null);
		assertThrows(RuntimeException.class, () -> service.bookTicket(1L, validReq));
	}

	@Test
	void testBookTicket_SeatsNotUpdated() {
		when(flightClient.getFlight(1L)).thenReturn(new Object());
		when(flightClient.updateSeats(1L, 1)).thenReturn("Not Enough Seats");
		assertThrows(RuntimeException.class, () -> service.bookTicket(1L, validReq));
	}

	@Test
	void testBookTicket_PassengerCountMismatch() {
		BookingRequest badReq = new BookingRequest("a@gmail.com", 2, "John:M:30", 5000.0, LocalDate.now().plusDays(3));
		assertThrows(RuntimeException.class, () -> service.bookTicket(1L, badReq));
	}

	@Test
	void testBookTicket_InvalidAge() {
		BookingRequest badReq = new BookingRequest("a@gmail.com", 1, "John:M:-5", 5000.0, LocalDate.now().plusDays(3));
		assertThrows(RuntimeException.class, () -> service.bookTicket(1L, badReq));
	}

	@Test
	void testGetTicket_PositiveAndNegative() {
		Booking b = new Booking();
		b.setPnr("PNR-X");
		b.setTicketJson("{\"a\":1}");
		when(repo.findByPnr("PNR-X")).thenReturn(b);

		String json = service.getTicketJson("PNR-X");
		assertNotNull(json);
		assertTrue(json.contains("\"a\":1"));

		when(repo.findByPnr("NOPE")).thenReturn(null);
		assertThrows(RuntimeException.class, () -> service.getTicketJson("NOPE"));
	}

	@Test
	void testDownloadTicket_nullAndNonNullFlightDetails() {
		Booking b = new Booking();
		b.setPnr("P1");
		b.setFlightId(1L);
		b.setSeats(1);
		b.setPassengerDetails("A:M:30");
		b.setTicketJson("{}");
		when(repo.findByPnr("P1")).thenReturn(b);

		when(flightClient.getFlight(1L)).thenReturn(null);
		TicketResponse r1 = service.downloadTicket("P1");
		assertNotNull(r1);
		assertNull(r1.getFlightDetails());

		Object fd = new Object();
		when(flightClient.getFlight(1L)).thenReturn(fd);
		TicketResponse r2 = service.downloadTicket("P1");
		assertNotNull(r2.getFlightDetails());
	}

	@Test
	void testGetHistory_nonEmptyAndEmpty() {
		Booking b = new Booking();
		b.setPnr("P2");
		b.setEmail("x@y.com");

		when(repo.findByEmail("x@y.com")).thenReturn(List.of(b));
		List<Booking> l1 = service.getHistory("x@y.com");
		assertEquals(1, l1.size());

		when(repo.findByEmail("empty@mail.com")).thenReturn(List.of());
		List<Booking> l2 = service.getHistory("empty@mail.com");
		assertTrue(l2.isEmpty());
	}

	@Test
	void testCancelBooking_Success_and_repositorySaveThrows() {
		Booking b = new Booking();
		b.setPnr("C1");
		b.setStatus("BOOKED");
		b.setJourneyDate(LocalDate.now().plusDays(5));
		b.setSeats(2);
		b.setFlightId(1L);

		when(repo.findByPnr("C1")).thenReturn(b);
		when(flightClient.rollbackSeats(1L, 2)).thenReturn("Seats Rolled Back");

		String res = service.cancelBooking("C1");
		assertEquals("Cancelled: C1", res);
		assertEquals("CANCELLED", b.getStatus());
		verify(repo, times(1)).save(b);

		Booking b2 = new Booking();
		b2.setPnr("C2");
		b2.setStatus("BOOKED");
		b2.setJourneyDate(LocalDate.now().plusDays(5));
		b2.setSeats(1);
		b2.setFlightId(1L);

		when(repo.findByPnr("C2")).thenReturn(b2);
		doThrow(new RuntimeException("DB fail")).when(repo).save(b2);

		RuntimeException ex = assertThrows(RuntimeException.class, () -> service.cancelBooking("C2"));
		assertTrue(ex.getMessage().contains("DB fail"));
	}

	@Test
	void testCancelBooking_TooLateAnd_WrongStatusAnd_NotFound() {
		Booking b1 = new Booking();
		b1.setPnr("LATE");
		b1.setStatus("BOOKED");
		b1.setJourneyDate(LocalDate.now());
		when(repo.findByPnr("LATE")).thenReturn(b1);
		assertThrows(RuntimeException.class, () -> service.cancelBooking("LATE"));

		Booking b2 = new Booking();
		b2.setPnr("WS");
		b2.setStatus("CANCELLED");
		b2.setJourneyDate(LocalDate.now().plusDays(3));
		when(repo.findByPnr("WS")).thenReturn(b2);
		assertThrows(RuntimeException.class, () -> service.cancelBooking("WS"));

		when(repo.findByPnr("NF")).thenReturn(null);
		assertThrows(RuntimeException.class, () -> service.cancelBooking("NF"));
	}

	@Test
	void testFallbacksDirect() {
		assertNull(service.flightFallback(1L, new RuntimeException("down")));
		assertEquals("Flight Service Down - Rollback Pending", service.rollbackFallback(1L, 2, new RuntimeException()));
		assertEquals("Flight Service Down - Cannot Update Seats",
				service.seatUpdateFallback(1L, 2, new RuntimeException()));
	}
}
