
package com.example.rentaride.controller;

import com.example.rentaride.domain.Vehicle;
import com.example.rentaride.repository.VehicleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final VehicleRepository vehicleRepository;

    public AdminController(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping("/signout")
    public ResponseEntity<?> signout() {
        return ResponseEntity.ok(Map.of("message", "signed out"));
    }

    @GetMapping("/showVehicles")
    public ResponseEntity<?> showVehicles() {
        return ResponseEntity.ok(vehicleRepository.findAll());
    }

    @DeleteMapping("/deleteVehicle/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable("id") Long id) {
        Optional<Vehicle> opt = vehicleRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("succes", false));
        Vehicle v = opt.get();
        v.setIsDeleted("true");
        vehicleRepository.save(v);
        return ResponseEntity.ok(Map.of("message", "deleted"));
    }

    @PutMapping("/editVehicle/{id}")
    public ResponseEntity<?> editVehicle(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        Optional<Vehicle> opt = vehicleRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("succes", false));
        Vehicle v = opt.get();
        Map<String, Object> form = (Map<String, Object>) body.getOrDefault("formData", Collections.emptyMap());
        if (form.containsKey("registeration_number")) v.setRegistrationNumber(Objects.toString(form.get("registeration_number"), v.getRegistrationNumber()));
        if (form.containsKey("company")) v.setCompany(Objects.toString(form.get("company"), v.getCompany()));
        if (form.containsKey("name")) v.setName(Objects.toString(form.get("name"), v.getName()));
        if (form.containsKey("model")) v.setModel(Objects.toString(form.get("model"), v.getModel()));
        if (form.containsKey("title")) v.setCarTitle(Objects.toString(form.get("title"), v.getCarTitle()));
        if (form.containsKey("description")) v.setCarDescription(Objects.toString(form.get("description"), v.getCarDescription()));
        if (form.containsKey("base_package")) v.setBasePackage(Objects.toString(form.get("base_package"), v.getBasePackage()));
        if (form.containsKey("price")) v.setPrice(Integer.valueOf(Objects.toString(form.get("price"))));
        if (form.containsKey("year_made")) v.setYearMade(Integer.valueOf(Objects.toString(form.get("year_made"))));
        if (form.containsKey("fuelType")) v.setFuelType(Objects.toString(form.get("fuelType"), v.getFuelType()));
        if (form.containsKey("Seats")) v.setSeats(Integer.valueOf(Objects.toString(form.get("Seats"))));
        if (form.containsKey("transmitionType")) v.setTransmission(Objects.toString(form.get("transmitionType"), v.getTransmission()));
        if (form.containsKey("insurance_end_date")) v.setInsuranceEnd(Instant.parse(Objects.toString(form.get("insurance_end_date"))));
        if (form.containsKey("Registeration_end_date")) v.setRegistrationEnd(Instant.parse(Objects.toString(form.get("Registeration_end_date"))));
        if (form.containsKey("polution_end_date")) v.setPollutionEnd(Instant.parse(Objects.toString(form.get("polution_end_date"))));
        if (form.containsKey("carType")) v.setCarType(Objects.toString(form.get("carType"), v.getCarType()));
        if (form.containsKey("vehicleLocation")) v.setLocation(Objects.toString(form.get("vehicleLocation"), v.getLocation()));
        if (form.containsKey("vehicleDistrict")) v.setDistrict(Objects.toString(form.get("vehicleDistrict"), v.getDistrict()));
        v.setUpdatedAt(Instant.now());
        v.setIsAdminApproved(false);
        v.setIsRejected(false);
        vehicleRepository.save(v);
        return ResponseEntity.ok(v);
    }

    @PostMapping("/addProduct")
    public ResponseEntity<?> addProduct(@RequestParam Map<String, String> params,
                                        @RequestParam(value = "image", required = false) List<MultipartFile> images) {
        Vehicle v = new Vehicle();
        v.setRegistrationNumber(params.get("registeration_number"));
        v.setCompany(params.get("company"));
        v.setName(params.get("name"));
        v.setModel(params.get("model"));
        v.setCarTitle(params.get("title"));
        v.setBasePackage(params.get("base_package"));
        v.setPrice(parseIntOrNull(params.get("price")));
        v.setCarDescription(params.get("description"));
        v.setYearMade(parseIntOrNull(params.get("year_made")));
        v.setFuelType(params.get("fuel_type"));
        v.setSeats(parseIntOrNull(params.get("seat")));
        v.setTransmission(params.get("transmition_type"));
        v.setLocation(params.get("location"));
        v.setDistrict(params.get("district"));
        v.setCarType(params.get("car_type"));
        v.setCreatedAt(Instant.now());
        if (images != null) {
            List<String> urls = new ArrayList<>();
            for (MultipartFile file : images) {
                // For local dev, just store original filename; production should upload to storage
                urls.add("/uploads/" + file.getOriginalFilename());
            }
            v.setImage(urls);
        }
        v.setIsAdminAdded(true);
        v.setIsAdminApproved(true);
        vehicleRepository.save(v);
        return ResponseEntity.ok(v);
    }

    private Integer parseIntOrNull(String s) {
        try { return s == null ? null : Integer.valueOf(s); } catch (Exception e) { return null; }
    }

    @GetMapping("/getVehicleModels")
    public ResponseEntity<?> getVehicleModels() {
        // Minimal LOV response combining locations and car models from vehicles
        List<Vehicle> all = vehicleRepository.findAll();
        List<Map<String, Object>> out = new ArrayList<>();
        Set<String> locKey = new HashSet<>();
        Set<String> carKey = new HashSet<>();
        for (Vehicle v : all) {
            String keyLoc = v.getDistrict() + "|" + v.getLocation();
            if (v.getDistrict() != null && v.getLocation() != null && locKey.add(keyLoc)) {
                out.add(Map.of("type", "location", "district", v.getDistrict(), "location", v.getLocation()));
            }
            if (v.getModel() != null && carKey.add(v.getModel())) {
                out.add(Map.of("type", "car", "model", v.getModel(), "brand", v.getCompany()));
            }
        }
        return ResponseEntity.ok(out);
    }

    @GetMapping("/allBookings")
    public ResponseEntity<?> allBookings() {
        // For MVP, bookings can be fetched via a dedicated repository in a future step
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/changeStatus")
    public ResponseEntity<?> changeStatus(@RequestBody Map<String, Object> body) {
        // Stub: accept and return ok; extend to update booking later
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/fetchVendorVehilceRequests")
    public ResponseEntity<?> fetchVendorVehicleRequests() {
        // Pending: add vendor flow; return all vehicles pending approval
        List<Vehicle> pending = new ArrayList<>();
        for (Vehicle v: vehicleRepository.findAll()) {
            if (Boolean.FALSE.equals(v.getIsAdminApproved())) pending.add(v);
        }
        return ResponseEntity.ok(pending);
    }

    @PostMapping("/approveVendorVehicleRequest")
    public ResponseEntity<?> approveVendorVehicleRequest(@RequestBody Map<String, Object> body) {
        // Stub
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/rejectVendorVehicleRequest")
    public ResponseEntity<?> rejectVendorVehicleRequest(@RequestBody Map<String, Object> body) {
        // Stub
        return ResponseEntity.ok(Map.of("ok", true));
    }
}


