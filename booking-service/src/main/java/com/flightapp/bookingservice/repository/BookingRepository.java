package com.flightapp.bookingservice.repository;

import com.flightapp.bookingservice.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking,Long>{
    List<Booking> findByEmail(String email);
    Booking findByPnr(String pnr);
}
