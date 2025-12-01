package com.flightapp.bookingservice.controller;

import com.flightapp.bookingservice.domain.Booking;
import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.TicketResponse;
import com.flightapp.bookingservice.service.BookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

	@Mock
	BookingService service;

	@InjectMocks
	BookingController controller;

	@Test
	void testBookSuccess() {
		BookingRequest req = new BookingRequest("a@gmail.com", 1, "John:M:20", 100.0, LocalDate.now().plusDays(1));
		Booking booking = new Booking();
		booking.setPnr("PNR1");
		when(service.bookTicket(1L, req)).thenReturn(booking);
		ResponseEntity<String> resp = controller.book(1L, req);
		assertEquals(201, resp.getStatusCode().value());
		assertEquals("PNR1", resp.getBody());
	}

	@Test
	void testBookFailure() {
		BookingRequest req = new BookingRequest("a@gmail.com", 1, "John:M:20", 100.0, LocalDate.now().plusDays(1));
		when(service.bookTicket(1L, req)).thenThrow(new RuntimeException("err"));
		ResponseEntity<String> resp = controller.book(1L, req);
		assertEquals(400, resp.getStatusCode().value());
		assertEquals("err", resp.getBody());
	}

	@Test
	void testHistory() {
		when(service.getHistory("a@gmail.com")).thenReturn(List.of(new Booking()));
		ResponseEntity<List<Booking>> resp = controller.history("a@gmail.com");
		assertEquals(1, resp.getBody().size());
	}

	@Test
	void testGetTicketSuccess() {
		when(service.getTicketJson("PNR1")).thenReturn("json");
		ResponseEntity<String> resp = controller.getTicket("PNR1");
		assertEquals("json", resp.getBody());
	}

	@Test
	void testGetTicketFailure() {
		when(service.getTicketJson("PNR1")).thenThrow(new RuntimeException("not found"));
		ResponseEntity<String> resp = controller.getTicket("PNR1");
		assertEquals(400, resp.getStatusCode().value());
		assertEquals("not found", resp.getBody());
	}

	@Test
	void testCancelSuccess() {
		ResponseEntity<Void> resp = controller.cancel("PNR1");
		assertEquals(204, resp.getStatusCode().value());
	}

	@Test
	void testCancelFailure() {
		doThrow(new RuntimeException()).when(service).cancelBooking("PNR1");
		ResponseEntity<Void> resp = controller.cancel("PNR1");
		assertEquals(400, resp.getStatusCode().value());
	}

	@Test
	void testDownloadSuccess() {
		TicketResponse t = new TicketResponse("P", "e", "d", 1, 1L, new Object(), "j");
		when(service.downloadTicket("P")).thenReturn(t);
		ResponseEntity<TicketResponse> resp = controller.download("P");
		assertEquals("P", resp.getBody().getPnr());
	}

	@Test
	void testDownloadFailure() {
		when(service.downloadTicket("P")).thenThrow(new RuntimeException());
		ResponseEntity<TicketResponse> resp = controller.download("P");
		assertEquals(400, resp.getStatusCode().value());
	}
}
