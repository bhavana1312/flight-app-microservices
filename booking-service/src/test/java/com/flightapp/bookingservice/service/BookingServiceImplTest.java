package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.domain.Booking;
import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.feign.FlightClient;
import com.flightapp.bookingservice.repository.BookingRepository;
import com.flightapp.bookingservice.service.impl.BookingServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
public class BookingServiceImplTest {

    @Mock
    private BookingRepository repo;

    @Mock
    private FlightClient flightClient;

    @InjectMocks
    private BookingServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private BookingRequest validReq() {
        return new BookingRequest(
                "user@mail.com",
                2,
                "John:M:25;Lily:F:22",
                5000.0,
                LocalDate.now().plusDays(3)
        );
    }

    @Test
    void testBookTicketSuccess() {
        BookingRequest req = validReq();

        when(flightClient.getFlight(1L)).thenReturn(new Object());
        when(flightClient.updateSeats(1L, 2)).thenReturn("Seats Updated");

        Booking saved = new Booking();
        saved.setPnr("PNR-TEST");
        when(repo.save(any())).thenReturn(saved);

        Booking result = service.bookTicket(1L, req);

        assertNotNull(result);
        assertEquals("PNR-TEST", result.getPnr());
    }

    @Test
    void testBookTicketInvalidPassengerCount() {
        BookingRequest req = new BookingRequest(
                "a@a.com", 2, "John:M:22", 2000.0, LocalDate.now().plusDays(3)
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.bookTicket(1L, req));

        assertEquals("Number of passengers must match number of seats booked", ex.getMessage());
    }

    @Test
    void testBookTicketInvalidAge() {
        BookingRequest req = new BookingRequest(
                "a@a.com", 1, "Baby:M:0", 1000.0, LocalDate.now().plusDays(3)
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.bookTicket(1L, req));

        assertTrue(ex.getMessage().contains("Invalid age"));
    }

    @Test
    void testBookTicketFlightUnavailable() {
        BookingRequest req = validReq();

        when(flightClient.getFlight(1L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.bookTicket(1L, req));

        assertEquals("Flight service unavailable. Try later.", ex.getMessage());
    }

    @Test
    void testCancelBookingSuccess() {
        Booking b = new Booking();
        b.setPnr("P1");
        b.setSeats(2);
        b.setFlightId(1L);
        b.setJourneyDate(LocalDate.now().plusDays(4));
        b.setStatus("BOOKED");

        when(repo.findByPnr("P1")).thenReturn(b);
        when(flightClient.rollbackSeats(1L, 2)).thenReturn("Seats Rolled Back");

        String resp = service.cancelBooking("P1");

        assertEquals("Cancelled: P1", resp);
    }

    @Test
    void testCancelBookingTooLate() {
        Booking b = new Booking();
        b.setJourneyDate(LocalDate.now());
        b.setStatus("BOOKED");

        when(repo.findByPnr("P1")).thenReturn(b);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.cancelBooking("P1"));

        assertEquals("Cancellation allowed only 24 hours before journey", ex.getMessage());
    }

    @Test
    void testCancelBookingNotFound() {
        when(repo.findByPnr("P1")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.cancelBooking("P1"));

        assertEquals("PNR Not Found", ex.getMessage());
    }

    @Test
    void testFallbacks() {
        assertNull(service.flightFallback(1L, new RuntimeException()));

        assertEquals("Flight Service Down - Cannot Update Seats",
                service.seatUpdateFallback(1L, 2, new RuntimeException()));

        assertEquals("Flight Service Down - Rollback Pending",
                service.rollbackFallback(1L, 2, new RuntimeException()));
    }
}
