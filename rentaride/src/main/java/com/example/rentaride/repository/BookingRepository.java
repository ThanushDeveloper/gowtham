package com.example.rentaride.repository;

import com.example.rentaride.domain.Booking;
import com.example.rentaride.domain.User;
import com.example.rentaride.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserOrderByCreatedAtDesc(User user);
    List<Booking> findByVehicleAndPickupDateLessThanAndDropOffDateGreaterThan(Vehicle vehicle, Instant dropOff, Instant pickup);
}
