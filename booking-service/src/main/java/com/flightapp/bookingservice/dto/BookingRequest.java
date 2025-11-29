package com.flightapp.bookingservice.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotBlank
    private String email;

    @NotNull
    @Min(1)
    private Integer seats;

    @NotBlank
    private String passengerDetails;

    @NotNull
    private Double amount;

    @NotNull
    private LocalDate journeyDate;
}
