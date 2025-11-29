package com.flightapp.bookingservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.flightapp.bookingservice.domain.Booking;
import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.feign.FlightClient;
import com.flightapp.bookingservice.service.BookingService;

@WebMvcTest(BookingController.class)
@ActiveProfiles("test")
public class BookingControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    BookingService service;

    BookingRequest req;
    
    @MockBean
    private FlightClient flightClient;

    @BeforeEach
    void init() {
        req = new BookingRequest(
            "test@gmail.com",
            1,
            "John:M:25",
            2000.0,
            LocalDate.now().plusDays(3)
        );
    }

    @Test
    void testBookSuccess() throws Exception {
        when(service.bookTicket(eq(1L), any())).thenReturn(new Booking());

        mvc.perform(post("/api/flight/booking/1")
                .contentType("application/json")
                .content("""
                        {
                            "email":"test@gmail.com",
                            "seats":1,
                            "passengerDetails":"John:M:25",
                            "amount":2000,
                            "journeyDate":"2030-01-01"
                        }
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void testHistory() throws Exception {
        when(service.getHistory("a@mail.com")).thenReturn(new ArrayList<>());

        mvc.perform(get("/api/flight/booking/history/a@mail.com"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTicket() throws Exception {
        when(service.getTicketJson("PNR123")).thenReturn("{}");

        mvc.perform(get("/api/flight/booking/ticket/PNR123"))
                .andExpect(status().isOk());
    }

    @Test
    void testCancel() throws Exception {
        when(service.cancelBooking("PNR123")).thenReturn("Cancelled");

        mvc.perform(delete("/api/flight/booking/cancel/PNR123"))
                .andExpect(status().isOk());
    }

    @Test
    void testDownload() throws Exception {
        when(service.downloadTicket("PNR123")).thenReturn(null);

        mvc.perform(get("/api/flight/booking/download/PNR123"))
                .andExpect(status().isOk());
    }
}
