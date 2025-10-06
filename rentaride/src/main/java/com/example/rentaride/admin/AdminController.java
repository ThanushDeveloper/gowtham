package com.example.rentaride.admin;

import com.example.rentaride.booking.Booking;
import com.example.rentaride.booking.BookingRepository;
import com.example.rentaride.master.MasterData;
import com.example.rentaride.master.MasterDataRepository;
import com.example.rentaride.user.User;
import com.example.rentaride.user.UserRepository;
import com.example.rentaride.vehicle.Vehicle;
import com.example.rentaride.vehicle.VehicleRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final VehicleRepository vehicleRepository;
    private final BookingRepository bookingRepository;
    private final MasterDataRepository masterDataRepository;
    private final UserRepository userRepository;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @GetMapping("/showVehicles")
    public ResponseEntity<?> showVehicles() {
        return ResponseEntity.ok(vehicleRepository.findByIsDeleted("false"));
    }

    @PostMapping(value = "/addProduct", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(
            @RequestParam String registeration_number,
            @RequestParam String company,
            @RequestParam String name,
            @RequestParam String model,
            @RequestParam(required = false) String title,
            @RequestParam(required = false, name = "base_package") String basePackage,
            @RequestParam Integer price,
            @RequestParam String description,
            @RequestParam Integer year_made,
            @RequestParam(name = "fuel_type") String fuelType,
            @RequestParam(name = "seat") Integer seats,
            @RequestParam(name = "transmition_type") String transmition,
            @RequestParam(name = "insurance_end_date") String insuranceEndDate,
            @RequestParam(name = "registeration_end_date") String registerationEndDate,
            @RequestParam(name = "polution_end_date") String polutionEndDate,
            @RequestParam(name = "car_type") String carType,
            @RequestParam String location,
            @RequestParam String district,
            @RequestParam(required = false) MultipartFile[] image
    ) throws IOException {
        Vehicle v = new Vehicle();
        v.setRegisterationNumber(registeration_number);
        v.setCompany(company);
        v.setName(name);
        v.setModel(model);
        v.setTitle(title);
        v.setBasePackage(basePackage);
        v.setPrice(price);
        v.setDescription(description);
        v.setYearMade(year_made);
        v.setFuelType(fuelType);
        v.setSeats(seats);
        v.setTransmition(transmition);
        v.setInsuranceEnd(LocalDate.parse(toIsoDate(insuranceEndDate)));
        v.setRegisterationEnd(LocalDate.parse(toIsoDate(registerationEndDate)));
        v.setPollutionEnd(LocalDate.parse(toIsoDate(polutionEndDate)));
        v.setCarType(carType);
        v.setLocation(location);
        v.setDistrict(district);
        // Store images locally and save URLs
        List<String> urls = saveFiles(image);
        v.setImage(urls);
        vehicleRepository.save(v);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/deleteVehicle/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id) {
        return vehicleRepository.findById(id).map(v -> {
            v.setIsDeleted("true");
            vehicleRepository.save(v);
            return ResponseEntity.ok(Map.of("ok", true));
        }).orElse(ResponseEntity.status(404).body(err("vehicle not found")));
    }

    @PutMapping("/editVehicle/{id}")
    public ResponseEntity<?> editVehicle(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return vehicleRepository.findById(id).map(v -> {
            Map<String, Object> fd = (Map<String, Object>) body.get("formData");
            if (fd == null) return ResponseEntity.badRequest().body(err("invalid body"));
            putIf(fd, "registeration_number", s -> v.setRegisterationNumber(s));
            putIf(fd, "company", v::setCompany);
            putIf(fd, "name", v::setName);
            putIf(fd, "model", v::setModel);
            putIf(fd, "title", v::setTitle);
            putIf(fd, "base_package", s -> v.setBasePackage(s));
            putIf(fd, "price", o -> v.setPrice(Integer.valueOf(o)));
            putIf(fd, "year_made", o -> v.setYearMade(Integer.valueOf(o)));
            putIf(fd, "fuelType", v::setFuelType);
            putIf(fd, "Seats", o -> v.setSeats(Integer.valueOf(o)));
            putIf(fd, "transmitionType", v::setTransmition);
            putIf(fd, "vehicleLocation", v::setLocation);
            putIf(fd, "vehicleDistrict", v::setDistrict);
            putIf(fd, "carType", v::setCarType);
            putIf(fd, "description", v::setDescription);
            vehicleRepository.save(v);
            return ResponseEntity.ok(Map.of("ok", true));
        }).orElse(ResponseEntity.status(404).body(err("vehicle not found")));
    }

    @GetMapping("/getVehicleModels")
    public ResponseEntity<?> getVehicleModels() {
        return ResponseEntity.ok(masterDataRepository.findAll());
    }

    // Vendor requests endpoints (simplified)
    @GetMapping("/fetchVendorVehilceRequests")
    public ResponseEntity<?> fetchVendorVehilceRequests() {
        // vehicles where isAdminAdded=false and not yet approved
        List<Vehicle> all = vehicleRepository.findByIsDeleted("false");
        return ResponseEntity.ok(all.stream().filter(v -> !Boolean.TRUE.equals(v.getIsAdminAdded())).toList());
    }

    @PostMapping("/approveVendorVehicleRequest")
    public ResponseEntity<?> approveVendorVehicleRequest(@RequestBody Map<String, Object> body) {
        Long id = toLong(body.get("_id"));
        return vehicleRepository.findById(id).map(v -> {
            v.setIsAdminApproved(true);
            v.setIsRejected(false);
            vehicleRepository.save(v);
            return ResponseEntity.ok(Map.of("ok", true));
        }).orElse(ResponseEntity.status(404).body(err("vehicle not found")));
    }

    @PostMapping("/rejectVendorVehicleRequest")
    public ResponseEntity<?> rejectVendorVehicleRequest(@RequestBody Map<String, Object> body) {
        Long id = toLong(body.get("_id"));
        return vehicleRepository.findById(id).map(v -> {
            v.setIsRejected(true);
            v.setIsAdminApproved(false);
            vehicleRepository.save(v);
            return ResponseEntity.ok(Map.of("ok", true));
        }).orElse(ResponseEntity.status(404).body(err("vehicle not found")));
    }

    @GetMapping("/allBookings")
    public ResponseEntity<?> allBookings() {
        return ResponseEntity.ok(bookingRepository.findAll());
    }

    @PostMapping("/changeStatus")
    public ResponseEntity<?> changeStatus(@RequestBody Map<String, Object> body) {
        Long id = toLong(body.get("id"));
        String status = Objects.toString(body.get("status"), "booked");
        return bookingRepository.findById(id).map(b -> {
            try {
                b.setStatus(Booking.Status.valueOf(status));
            } catch (Exception ignored) {}
            bookingRepository.save(b);
            return ResponseEntity.ok(Map.of("ok", true));
        }).orElse(ResponseEntity.status(404).body(err("booking not found")));
    }

    @GetMapping("/signout")
    public ResponseEntity<?> signout() {
        return ResponseEntity.ok(Map.of("ok", true));
    }

    private List<String> saveFiles(MultipartFile[] files) throws IOException {
        if (files == null || files.length == 0) return List.of();
        Path root = Paths.get(uploadDir);
        Files.createDirectories(root);
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String clean = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            Path dest = root.resolve(UUID.randomUUID() + "-" + clean);
            Files.copy(file.getInputStream(), dest);
            urls.add("/uploads/" + dest.getFileName());
        }
        return urls;
    }

    private String toIsoDate(String v) {
        // Accepts formats like "Mon Jan 01 2024 ..." or ISO; simplify by splitting
        try {
            return v.substring(0, 10);
        } catch (Exception e) {
            return "1970-01-01";
        }
    }

    private Map<String, Object> err(String message) {
        Map<String, Object> m = new HashMap<>();
        m.put("succes", false);
        m.put("message", message);
        return m;
    }

    private void putIf(Map<String, Object> fd, String key, java.util.function.Consumer<String> setter) {
        if (fd.containsKey(key) && fd.get(key) != null) setter.accept(fd.get(key).toString());
    }

    private void putIf(Map<String, Object> fd, String key, java.util.function.Consumer<Integer> setterInt) {
        if (fd.containsKey(key) && fd.get(key) != null) setterInt.accept(Integer.valueOf(fd.get(key).toString()));
    }

    private Long toLong(Object o) {
        try { return Long.valueOf(String.valueOf(o)); } catch (Exception e) { return null; }
    }
}

