package com.flightapp.flightservice.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.flightapp.flightservice.domain.SeatType;

import jakarta.validation.constraints.*;

import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlightInventoryRequest {

    @NotBlank
    private String airlineName;

    @Pattern(regexp = "^[A-Z0-9]{2,3}$", message = "Invalid airline code")
    private String airlineCode;

    @Pattern(regexp = "^[A-Z]{3}$", message = "From place must be 3-letter IATA code")
    private String fromPlace;

    @Pattern(regexp = "^[A-Z]{3}$", message = "To place must be 3-letter IATA code")
    private String toPlace;

    @NotNull
    private LocalDateTime departureDateTime;

    @NotNull
    private LocalDateTime arrivalDateTime;

    @DecimalMin(value="500.00", message="Minimum price is ₹500")
    private Double price;

    @Min(value=10, message="Minimum 10 seats required")
    private Integer totalSeats;

    @NotEmpty(message="Seat numbers required")
    private List<String> seatNumbers;

    @NotEmpty(message="Seat types required for each seat")
    private List<SeatType> seatTypes;
}
