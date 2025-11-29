package com.flightapp.flightservice.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.flightapp.flightservice.domain.Flight;
import com.flightapp.flightservice.service.FlightService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

@WebMvcTest(FlightController.class)
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightService service;

    @Test
    void testAddInventory() throws Exception {
        Flight f = new Flight();
        f.setId(1L);

        when(service.addInventory(Mockito.any())).thenReturn(f);

        mockMvc.perform(post("/api/flight/airline/inventory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"airlineName\":\"Test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testSearch() throws Exception {
        when(service.search("A", "B")).thenReturn(List.of(new Flight()));

        mockMvc.perform(post("/api/flight/search?from=A&to=B"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testGetFlight_found() throws Exception {
        Flight f = new Flight();
        f.setId(5L);
        when(service.getFlight(5L)).thenReturn(f);

        mockMvc.perform(get("/api/flight/get/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void testGetFlight_notFound() throws Exception {
        when(service.getFlight(5L)).thenReturn(null);

        mockMvc.perform(get("/api/flight/get/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateSeatsSuccess() throws Exception {
        when(service.updateSeats(1L, 3)).thenReturn("Seats Updated");

        mockMvc.perform(put("/api/flight/update-seats/1/3"))
                .andExpect(status().isOk())
                .andExpect(content().string("Seats Updated"));
    }

    @Test
    void testUpdateSeatsFlightNotFound() throws Exception {
        when(service.updateSeats(1L, 3)).thenReturn("Flight Not Found");

        mockMvc.perform(put("/api/flight/update-seats/1/3"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateSeatsNotEnough() throws Exception {
        when(service.updateSeats(1L, 3)).thenReturn("Not Enough Seats");

        mockMvc.perform(put("/api/flight/update-seats/1/3"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Not Enough Seats"));
    }
}
