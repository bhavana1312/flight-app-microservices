package com.flightapp.flightservice.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class FlightEntityConstructorTest {

    @Test
    void testAllArgsConstructor() {
        Flight f = new Flight(
                1L,
                "Indigo",
                "6E",
                "HYD",
                "BLR",
                "2025-01-01T10:00",
                "2025-01-01T12:00",
                3000.0,
                100
        );

        assertEquals(1L, f.getId());
        assertEquals("Indigo", f.getAirlineName());
        assertEquals("6E", f.getAirlineCode());
    }

    @Test
    void testNoArgsConstructor() {
        Flight f = new Flight();
        assertNotNull(f);
    }

    @Test
    void testSettersAndGetters() {
        Flight f = new Flight();
        f.setId(9L);
        f.setAirlineName("TestAir");
        f.setAvailableSeats(50);

        assertEquals(9L, f.getId());
        assertEquals("TestAir", f.getAirlineName());
        assertEquals(50, f.getAvailableSeats());
    }
}
