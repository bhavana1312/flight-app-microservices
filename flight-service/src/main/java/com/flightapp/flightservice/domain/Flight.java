package com.flightapp.flightservice.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="flights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Flight{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String airlineName;
    private String airlineCode;
    private String fromPlace;
    private String toPlace;
    private String departureDateTime;
    private String arrivalDateTime;
    private Double price;
    private Integer availableSeats;
}
