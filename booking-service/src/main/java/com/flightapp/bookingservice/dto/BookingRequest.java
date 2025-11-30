package com.flightapp.bookingservice.dto;

import lombok.*;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

	@NotBlank
	@Email
	private String email;

	@NotEmpty(message = "Selected seats required")
	private List<String> selectedSeats;

	@NotBlank(message = "Passenger details required")
	private String passengerDetails;

	@NotNull
	@Positive
	private Double amount;

	@NotNull
	@Future
	private LocalDate journeyDate;
}
