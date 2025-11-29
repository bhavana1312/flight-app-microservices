package com.flightapp.bookingservice.repository;

import com.flightapp.bookingservice.domain.Booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@ActiveProfiles("test")
@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository repo;

    @Test
    void testSaveAndFindByEmail() {
        Booking b = new Booking();
        b.setEmail("test@gmail.com");
        b.setSeats(1);
        b.setPassengerDetails("John:M:22");
        b.setAmount(1000.0);
        b.setPnr("PNR-111");
        b.setJourneyDate(LocalDate.now().plusDays(2));
        b.setBookedAt(LocalDateTime.now());

        repo.save(b);

        List<Booking> list = repo.findByEmail("test@gmail.com");
        assertEquals(1, list.size());
        assertEquals("PNR-111", list.get(0).getPnr());
    }

    @Test
    void testFindByPnr_andUpdateStatus() {
        Booking b = new Booking();
        b.setEmail("a@gmail.com");
        b.setSeats(1);
        b.setPassengerDetails("John:M:22");
        b.setAmount(1000.0);
        b.setPnr("PNR-XYZ");
        b.setJourneyDate(LocalDate.now().plusDays(2));
        repo.save(b);

        Booking found = repo.findByPnr("PNR-XYZ");
        assertNotNull(found);
        assertEquals("PNR-XYZ", found.getPnr());

        found.setStatus("CANCELLED");
        repo.save(found);

        Booking updated = repo.findByPnr("PNR-XYZ");
        assertEquals("CANCELLED", updated.getStatus());
    }

    @Test
    void testSaveMultipleAndFindByEmail() {
        Booking b1 = new Booking(); b1.setPnr("P1"); b1.setEmail("multi@mail.com"); b1.setSeats(1);
        Booking b2 = new Booking(); b2.setPnr("P2"); b2.setEmail("multi@mail.com"); b2.setSeats(2);
        repo.save(b1); repo.save(b2);

        List<Booking> found = repo.findByEmail("multi@mail.com");
        assertEquals(2, found.size());
    }
}
