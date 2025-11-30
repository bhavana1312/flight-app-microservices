package com.flightapp.flightservice.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "flight_seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String seatNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SeatType seatType;

	@Column(nullable = false)
	private boolean booked;

	@ManyToOne
	@JoinColumn(name = "flight_id")
	private Flight flight;
}
