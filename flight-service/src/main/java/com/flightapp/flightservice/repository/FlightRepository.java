package com.flightapp.flightservice.repository;

import com.flightapp.flightservice.domain.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
	List<Flight> findByFromPlaceAndToPlace(String fromPlace, String toPlace);
}
