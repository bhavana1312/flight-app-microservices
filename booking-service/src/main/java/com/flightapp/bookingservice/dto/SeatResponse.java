package com.flightapp.bookingservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {
	private String seatNumber;
	private String seatType;
	private boolean booked;
}
