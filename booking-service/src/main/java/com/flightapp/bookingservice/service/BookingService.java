package com.flightapp.bookingservice.service;

import java.util.List;

import com.flightapp.bookingservice.domain.Booking;
import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.TicketResponse;

public interface BookingService {

    Booking bookTicket(Long flightId, BookingRequest req);

    List<Booking> getHistory(String email);

    String cancelBooking(String pnr);

    String getTicketJson(String pnr);

    TicketResponse downloadTicket(String pnr);
}
