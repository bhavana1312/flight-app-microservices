package com.flightapp.bookingservice.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotBlank
    private String email;

    @NotNull
    private Integer seats;

    @NotBlank
    private String passengerDetails;

    @NotNull
    private Double amount;
}
