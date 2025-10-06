
package com.example.rentaride.controller;

import com.example.rentaride.domain.MasterData;
import com.example.rentaride.repository.MasterDataRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class MasterDataController {
    private final MasterDataRepository repository;

    public MasterDataController(MasterDataRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/getVehicleModels")
    public ResponseEntity<?> getVehicleModels() {
        List<MasterData> locations = repository.findByType("location");
        List<MasterData> cars = repository.findByType("car");
        // Flatten into a single array as the frontend expects a single list
        List<MasterData> combined = new java.util.ArrayList<>();
        combined.addAll(cars);
        combined.addAll(locations);
        return ResponseEntity.ok(combined);
    }
}

