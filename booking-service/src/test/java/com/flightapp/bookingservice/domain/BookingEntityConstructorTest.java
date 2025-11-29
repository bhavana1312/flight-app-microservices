package com.flightapp.bookingservice.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class BookingEntityConstructorTest {

	@Test
	void testAllArgsConstructorHitsJacoco() {
		LocalDate jd = LocalDate.now().plusDays(3);
		LocalDateTime bt = LocalDateTime.now();

		Booking b = new Booking(10L, "PNR-TEST12", "test@gmail.com", "John:M:25;Doe:F:22", 2, 7L, bt, "BOOKED", 5000.0,
				"{\"x\":1}", jd);

		assertEquals(10L, b.getId());
		assertEquals("PNR-TEST12", b.getPnr());
		assertEquals("test@gmail.com", b.getEmail());
		assertEquals("John:M:25;Doe:F:22", b.getPassengerDetails());
		assertEquals(2, b.getSeats());
		assertEquals(7L, b.getFlightId());
		assertEquals(bt, b.getBookedAt());
		assertEquals("BOOKED", b.getStatus());
		assertEquals(5000.0, b.getAmount());
		assertEquals("{\"x\":1}", b.getTicketJson());
		assertEquals(jd, b.getJourneyDate());
	}

	@Test
	void testNoArgsConstructorHitsJacoco() {
		Booking b = new Booking();
		assertNotNull(b);
	}

	@Test
	void testAllSettersAndGetters() {
		Booking b = new Booking();
		LocalDate jd = LocalDate.now().plusDays(2);
		LocalDateTime bt = LocalDateTime.now();

		b.setId(5L);
		b.setPnr("P1");
		b.setEmail("a@mail.com");
		b.setPassengerDetails("A:M:30");
		b.setSeats(3);
		b.setFlightId(8L);
		b.setBookedAt(bt);
		b.setStatus("BOOKED");
		b.setAmount(999.0);
		b.setTicketJson("{\"ok\":true}");
		b.setJourneyDate(jd);

		assertEquals(5L, b.getId());
		assertEquals("P1", b.getPnr());
		assertEquals("a@mail.com", b.getEmail());
		assertEquals("A:M:30", b.getPassengerDetails());
		assertEquals(3, b.getSeats());
		assertEquals(8L, b.getFlightId());
		assertEquals(bt, b.getBookedAt());
		assertEquals("BOOKED", b.getStatus());
		assertEquals(999.0, b.getAmount());
		assertEquals("{\"ok\":true}", b.getTicketJson());
		assertEquals(jd, b.getJourneyDate());
	}
}
