package com.flightapp.bookingservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
	private String pnr;
	private String email;
	private String passengerDetails;
	private Integer seats;
	private Long flightId;
	private Object flightDetails;
	private String ticketJson;
}
