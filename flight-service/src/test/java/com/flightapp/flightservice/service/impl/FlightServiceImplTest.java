package com.flightapp.flightservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.flightapp.flightservice.domain.Flight;
import com.flightapp.flightservice.repository.FlightRepository;

class FlightServiceImplTest {

    @Mock
    private FlightRepository repo;

    @InjectMocks
    private FlightServiceImpl service;

    public FlightServiceImplTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddInventory() {
        Flight f = new Flight();
        f.setId(1L);
        when(repo.save(f)).thenReturn(f);

        Flight saved = service.addInventory(f);
        assertEquals(1L, saved.getId());
    }

    @Test
    void testSearch() {
        Flight f = new Flight();
        when(repo.findByFromPlaceAndToPlace("A", "B")).thenReturn(List.of(f));

        List<Flight> result = service.search("A", "B");
        assertEquals(1, result.size());
    }

    @Test
    void testGetFlight_found() {
        Flight f = new Flight();
        f.setId(10L);
        when(repo.findById(10L)).thenReturn(Optional.of(f));

        Flight result = service.getFlight(10L);
        assertNotNull(result);
    }

    @Test
    void testGetFlight_notFound() {
        when(repo.findById(100L)).thenReturn(Optional.empty());
        Flight result = service.getFlight(100L);
        assertNull(result);
    }

    @Test
    void testUpdateSeats_success() {
        Flight f = new Flight();
        f.setAvailableSeats(10);
        when(repo.findById(1L)).thenReturn(Optional.of(f));

        String res = service.updateSeats(1L, 5);
        assertEquals("Seats Updated", res);
        assertEquals(5, f.getAvailableSeats());
    }

    @Test
    void testUpdateSeats_notEnoughSeats() {
        Flight f = new Flight();
        f.setAvailableSeats(3);
        when(repo.findById(1L)).thenReturn(Optional.of(f));

        String res = service.updateSeats(1L, 5);
        assertEquals("Not Enough Seats", res);
    }

    @Test
    void testUpdateSeats_flightNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        String res = service.updateSeats(99L, 2);
        assertEquals("Flight Not Found", res);
    }

    @Test
    void testRollbackSeats_success() {
        Flight f = new Flight();
        f.setAvailableSeats(5);
        when(repo.findById(2L)).thenReturn(Optional.of(f));

        String res = service.rollbackSeats(2L, 3);
        assertEquals("Seats Rolled Back", res);
        assertEquals(8, f.getAvailableSeats());
    }

    @Test
    void testRollbackSeats_flightNotFound() {
        when(repo.findById(10L)).thenReturn(Optional.empty());
        String res = service.rollbackSeats(10L, 2);
        assertEquals("Flight Not Found", res);
    }
}
