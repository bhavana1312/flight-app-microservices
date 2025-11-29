package com.flightapp.flightservice.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.flightapp.flightservice.domain.Flight;
import com.flightapp.flightservice.dto.FlightInventoryRequest;
import com.flightapp.flightservice.service.FlightService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.util.List;

@WebMvcTest(controllers = FlightController.class)
@AutoConfigureMockMvc(addFilters = false)
class FlightControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private FlightService service;

	@Test
	void testAddInventory() throws Exception {
		Flight f = new Flight();
		f.setId(1L);

		when(service.addInventory(any())).thenReturn(f);

		mockMvc.perform(post("/api/flight/airline/inventory/add").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "airlineName":"Indigo",
				  "airlineCode":"6E",
				  "fromPlace":"HYD",
				  "toPlace":"BLR",
				  "departureDateTime":"2025-01-01T10:00",
				  "arrivalDateTime":"2025-01-01T12:00",
				  "price":3500.0,
				  "availableSeats":100
				}
				""")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
	}

	@Test
	void testSearch() throws Exception {

		Flight f = new Flight();
		f.setId(1L);
		f.setAirlineName("Indigo");
		f.setAirlineCode("6E");
		f.setFromPlace("A");
		f.setToPlace("B");
		f.setDepartureDateTime("2025-12-01T10:00:00");
		f.setArrivalDateTime("2025-12-01T12:00:00");
		f.setPrice(3000.0);
		f.setAvailableSeats(50);

		when(service.search("A", "B")).thenReturn(List.of(f));

		String body = """
				{
				  "from": "A",
				  "to": "B"
				}
				""";

		mockMvc.perform(post("/api/flight/search").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].airlineName").value("Indigo"));
	}

	@Test
	void testSearch_validationError_hitsExceptionHandler() throws Exception {

		String invalidBody = """
				{
				  "from": "",
				  "to": ""
				}
				""";

		mockMvc.perform(post("/api/flight/search").contentType(MediaType.APPLICATION_JSON).content(invalidBody))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.from").exists())
				.andExpect(jsonPath("$.to").exists());
	}

	@Test
	void testAddInventory_runtimeError_hitsExceptionHandler() throws Exception {

		FlightInventoryRequest req = new FlightInventoryRequest("Indigo", "6E", "A", "B", "2025-12-01T10:00:00",
				"2025-12-01T12:00:00", 3000.0, 50);

		when(service.addInventory(any())).thenThrow(new RuntimeException("Boom"));

		String body = """
				{
				  "airlineName": "Indigo",
				  "airlineCode": "6E",
				  "fromPlace": "A",
				  "toPlace": "B",
				  "departureDateTime": "2025-12-01T10:00:00",
				  "arrivalDateTime": "2025-12-01T12:00:00",
				  "price": 3000.0,
				  "availableSeats": 50
				}
				""";

		mockMvc.perform(post("/api/flight/airline/inventory/add").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest()).andExpect(content().string("Boom"));
	}

	@Test
	void testGetFlight_found() throws Exception {
		Flight f = new Flight();
		f.setId(5L);
		when(service.getFlight(5L)).thenReturn(f);

		mockMvc.perform(get("/api/flight/get/5")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(5));
	}

	@Test
	void testGetFlight_notFound() throws Exception {
		when(service.getFlight(5L)).thenReturn(null);

		mockMvc.perform(get("/api/flight/get/5")).andExpect(status().isNotFound());
	}

	@Test
	void testUpdateSeatsSuccess() throws Exception {
		when(service.updateSeats(1L, 3)).thenReturn("Seats Updated");

		mockMvc.perform(put("/api/flight/update-seats/1/3")).andExpect(status().isOk())
				.andExpect(content().string("Seats Updated"));
	}

	@Test
	void testUpdateSeatsFlightNotFound() throws Exception {
		when(service.updateSeats(1L, 3)).thenReturn("Flight Not Found");

		mockMvc.perform(put("/api/flight/update-seats/1/3")).andExpect(status().isNotFound());
	}

	@Test
	void testUpdateSeatsNotEnough() throws Exception {
		when(service.updateSeats(1L, 3)).thenReturn("Not Enough Seats");

		mockMvc.perform(put("/api/flight/update-seats/1/3")).andExpect(status().isBadRequest())
				.andExpect(content().string("Not Enough Seats"));
	}
}
