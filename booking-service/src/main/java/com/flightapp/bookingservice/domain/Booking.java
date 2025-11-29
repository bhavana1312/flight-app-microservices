package com.flightapp.bookingservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String pnr; 
    private String email;
    private String passengerDetails; 
    private Integer seats;
    private String flightId; 
    private LocalDateTime bookedAt;
    private String status; 
    private Double amount;
    private String ticketJson; 
}
