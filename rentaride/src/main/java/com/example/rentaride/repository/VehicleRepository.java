package com.example.rentaride.repository;

import com.example.rentaride.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);
    List<Vehicle> findByIsDeleted(String isDeleted);
    List<Vehicle> findByDistrictAndLocationAndIsDeleted(String district, String location, String isDeleted);
    List<Vehicle> findByModelAndIsDeleted(String model, String isDeleted);
}
