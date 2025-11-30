package com.flightapp.flightservice.dto;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlightResponse {
    private Long id;
    private String airlineName;
    private String airlineCode;
    private String fromPlace;
    private String toPlace;
    private String departureDateTime;
    private String arrivalDateTime;
    private Double price;
    private Integer totalSeats;
    private List<SeatResponse> seats;
}
