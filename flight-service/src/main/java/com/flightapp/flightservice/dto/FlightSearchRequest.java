package com.flightapp.flightservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlightSearchRequest {

    @NotBlank(message = "From place is required")
    private String from;

    @NotBlank(message = "To place is required")
    private String to;
}
