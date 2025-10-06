package com.example.rentaride.vehicle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByRegisterationNumber(String registerationNumber);
    List<Vehicle> findByIsDeleted(String isDeleted);
    List<Vehicle> findByIsAdminApprovedTrueAndIsDeleted(String isDeleted);
    List<Vehicle> findByModelAndIsDeleted(String model, String isDeleted);
    List<Vehicle> findByDistrictAndLocationAndIsDeleted(String district, String location, String isDeleted);
}
