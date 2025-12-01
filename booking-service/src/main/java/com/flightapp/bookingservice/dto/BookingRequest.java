package com.flightapp.bookingservice.dto;

import lombok.*;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

	@NotBlank(message = "Email is required")
	@Email(message = "Email must be valid")
	private String email;

	@NotNull(message = "Seat count is required")
	@Min(value = 1, message = "At least 1 seat must be booked")
	@Max(value = 10, message = "You cannot book more than 10 seats at once")
	private Integer seats;

	@NotBlank(message = "Passenger details are required")
	@Pattern(regexp = "([A-Za-z ]+:[MF]:\\d{1,3})(;[A-Za-z ]+:[MF]:\\d{1,3})*", message = "Passenger details must follow NAME:GENDER:AGE format, separated by semicolons")
	private String passengerDetails;

	@NotNull(message = "Amount is required")
	@Positive(message = "Amount must be greater than zero")
	private Double amount;

	@NotNull(message = "Journey date is required")
	@Future(message = "Journey date must be in the future")
	private LocalDate journeyDate;
}
