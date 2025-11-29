package com.flightapp.bookingservice.controller;

import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.TicketResponse;
import com.flightapp.bookingservice.domain.Booking;
import com.flightapp.bookingservice.service.BookingService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

@ActiveProfiles("test")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService service;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void testBookSuccess() throws Exception {
        Booking b = new Booking();
        b.setPnr("PNR-123");

        when(service.bookTicket(eq(1L), any())).thenReturn(b);

        BookingRequest req = new BookingRequest("a@gmail.com", 1, "John:M:22", 1000.0, java.time.LocalDate.now().plusDays(2));

        mockMvc.perform(post("/api/flight/booking/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("PNR")));
    }

    @Test
    void testBookFailure() throws Exception {
        when(service.bookTicket(eq(1L), any())).thenThrow(new RuntimeException("Error"));

        BookingRequest req = new BookingRequest("a@gmail.com", 1, "John:M:22", 1000.0, java.time.LocalDate.now().plusDays(2));

        mockMvc.perform(post("/api/flight/booking/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testBookValidation_failure_missingFields() throws Exception {
        mockMvc.perform(post("/api/flight/booking/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testHistory_nonEmptyResponseBody() throws Exception {
        Booking b = new Booking(); b.setPnr("P-H1"); b.setEmail("a@mail.com");
        when(service.getHistory("a@mail.com")).thenReturn(List.of(b));
        mockMvc.perform(get("/api/flight/booking/history/a@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pnr").value("P-H1"));
    }

    @Test
    void testGetTicket_andFailure() throws Exception {
        when(service.getTicketJson("PNR-1")).thenReturn("ticket-json");
        mockMvc.perform(get("/api/flight/booking/ticket/PNR-1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ticket-json")));

        when(service.getTicketJson("BAD")).thenThrow(new RuntimeException("Not Found"));
        mockMvc.perform(get("/api/flight/booking/ticket/BAD"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCancel_andFailure() throws Exception {
        when(service.cancelBooking("PNR-1")).thenReturn("Cancelled");
        mockMvc.perform(delete("/api/flight/booking/cancel/PNR-1"))
                .andExpect(status().isOk());

        when(service.cancelBooking("BAD")).thenThrow(new RuntimeException("Error"));
        mockMvc.perform(delete("/api/flight/booking/cancel/BAD"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDownload_returnsJsonStructure_andFailure() throws Exception {
        TicketResponse resp = new TicketResponse();
        resp.setPnr("D1");
        resp.setEmail("a@mail.com");
        resp.setPassengerDetails("John:M:25");
        when(service.downloadTicket("D1")).thenReturn(resp);

        mockMvc.perform(get("/api/flight/booking/download/D1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pnr").value("D1"))
                .andExpect(jsonPath("$.email").value("a@mail.com"));

        when(service.downloadTicket("BAD")).thenThrow(new RuntimeException("Error"));
        mockMvc.perform(get("/api/flight/booking/download/BAD"))
                .andExpect(status().isBadRequest());
    }
}
