package com.flightapp.flightservice.domain;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="flights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Flight {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String airlineName;

    @Column(nullable=false, length=10)
    private String airlineCode;

    @Column(nullable=false, length=5)
    private String fromPlace;

    @Column(nullable=false, length=5)
    private String toPlace;

    @Column(nullable=false)
    private LocalDateTime departureDateTime;

    @Column(nullable=false)
    private LocalDateTime arrivalDateTime;

    @Column(nullable=false)
    private Double price;

    @Column(nullable=false)
    private Integer totalSeats;

    @OneToMany(mappedBy="flight", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER)
    private List<Seat> seats;
}
