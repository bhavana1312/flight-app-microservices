package com.flightapp.bookingservice.controller;

import com.flightapp.bookingservice.domain.Booking;
import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.TicketResponse;
import com.flightapp.bookingservice.service.BookingService;

import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingControllerTest {

	@Mock
	private BookingService service;

	@InjectMocks
	private BookingController controller;

	BookingControllerTest() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testBookSuccess() {
		Booking booking = new Booking();
		booking.setPnr("PNR123");
		when(service.bookTicket(anyLong(), any())).thenReturn(booking);

		BookingRequest req = new BookingRequest("a@b.com", 1, "John:M:30", 1000.0, LocalDate.now().plusDays(2));

		ResponseEntity<String> resp = controller.book(1L, req);

		assertEquals(201, resp.getStatusCodeValue());
		assertEquals("PNR123", resp.getBody());
	}

	@Test
	void testBookFailure() {
		when(service.bookTicket(anyLong(), any())).thenThrow(new RuntimeException("Error"));

		BookingRequest req = new BookingRequest("a@b.com", 1, "John:M:30", 1000.0, LocalDate.now().plusDays(2));

		ResponseEntity<String> resp = controller.book(1L, req);

		assertEquals(400, resp.getStatusCodeValue());
	}

	@Test
	void testHistory() {
		controller.history("a@b.com");
		verify(service).getHistory("a@b.com");
	}

	@Test
	void testGetTicketSuccess() {
		when(service.getTicketJson("P")).thenReturn("{}");

		ResponseEntity<String> resp = controller.getTicket("P");

		assertEquals("{}", resp.getBody());
	}

	@Test
	void testCancel() {
		ResponseEntity<Void> resp = controller.cancel("P");
		assertEquals(204, resp.getStatusCodeValue());
	}

	@Test
	void testDownloadSuccess() {
		TicketResponse tr = new TicketResponse();
		tr.setPnr("X");

		when(service.downloadTicket("X")).thenReturn(tr);

		ResponseEntity<TicketResponse> resp = controller.download("X");

		assertEquals("X", resp.getBody().getPnr());
	}
}
