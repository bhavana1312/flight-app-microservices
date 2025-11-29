package com.flightapp.flightservice.dto;

import jakarta.validation.constraints.*;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlightInventoryRequest {

    @NotBlank(message = "Airline name is required")
    private String airlineName;

    @NotBlank(message = "Airline code is required")
    @Size(min = 2, max = 10, message = "Airline code must be between 2 and 10 characters")
    private String airlineCode;

    @NotBlank(message = "From place is required")
    private String fromPlace;

    @NotBlank(message = "To place is required")
    private String toPlace;

    @NotBlank(message = "Departure date/time is required")
    private String departureDateTime;

    @NotBlank(message = "Arrival date/time is required")
    private String arrivalDateTime;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private Double price;

    @NotNull(message = "Available seats are required")
    @Positive(message = "Available seats must be greater than zero")
    private Integer availableSeats;
}
