
package com.example.rentaride.booking;

import com.example.rentaride.user.User;
import com.example.rentaride.vehicle.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findTop5ByUserOrderByCreatedAtDesc(User user);
    List<Booking> findByVehicle(Vehicle vehicle);
    List<Booking> findByUser(User user);
}

