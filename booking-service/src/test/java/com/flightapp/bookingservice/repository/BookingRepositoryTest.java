package com.flightapp.bookingservice.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.flightapp.bookingservice.domain.Booking;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
public class BookingRepositoryTest {

	@Autowired
	private BookingRepository repo;

	@Test
	void testSaveAndFindByEmail() {
		Booking b = new Booking();
		b.setPnr("P1");
		b.setEmail("test@mail.com");
		b.setSeats(1);
		b.setJourneyDate(LocalDate.now().plusDays(3));
		b.setPassengerDetails("John:M:25");
		b.setBookedAt(LocalDateTime.now());
		b.setAmount(2000.0);
		b.setStatus("BOOKED");
		b.setFlightId(1L);
		b.setTicketJson("{}");

		repo.save(b);

		List<Booking> list = repo.findByEmail("test@mail.com");

		assertEquals(1, list.size());
		assertEquals("P1", list.get(0).getPnr());
	}

	@Test
	void testFindByPnr() {
		Booking b = new Booking();
		b.setPnr("XYZ");
		b.setEmail("x@y.com");
		b.setSeats(1);
		b.setJourneyDate(LocalDate.now().plusDays(3));
		b.setPassengerDetails("A:M:22");
		b.setBookedAt(LocalDateTime.now());
		b.setAmount(1500.0);
		b.setStatus("BOOKED");
		b.setFlightId(2L);
		b.setTicketJson("{}");

		repo.save(b);

		Booking result = repo.findByPnr("XYZ");

		assertNotNull(result);
		assertEquals("XYZ", result.getPnr());
	}
}
