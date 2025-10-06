package com.example.rentaride.vendor;

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
@RequestMapping("/api/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/vendorsignup")
    public ResponseEntity<?> vendorSignup(@RequestBody Map<String, Object> body) {
        String username = Objects.toString(body.get("username"), "");
        String email = Objects.toString(body.get("email"), "");
        String password = Objects.toString(body.get("password"), "");
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(password);
        u.setVendor(true);
        userRepository.save(u);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/vendorsignin")
    public ResponseEntity<?> vendorSignin(@RequestBody Map<String, Object> body) {
        String email = Objects.toString(body.get("email"), "");
        User u = userRepository.findByEmail(email).orElse(null);
        if (u == null) return ResponseEntity.status(401).body(err("invalid credentials"));
        Map<String, Object> resp = new HashMap<>();
        resp.put("isVendor", true);
        resp.put("_id", u.getId());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/vendorgoogle")
    public ResponseEntity<?> vendorGoogle(@RequestBody Map<String, Object> body) {
        String email = Objects.toString(body.get("email"), "");
        String name = Objects.toString(body.get("name"), "");
        User u = userRepository.findByEmail(email).orElseGet(() -> {
            User nu = new User();
            nu.setEmail(email);
            nu.setUsername(name.replaceAll("\\s+", "").toLowerCase());
            nu.setVendor(true);
            return userRepository.save(nu);
        });
        Map<String, Object> resp = new HashMap<>();
        resp.put("isVendor", true);
        resp.put("_id", u.getId());
        return ResponseEntity.ok(resp);
    }

    @PostMapping(value = "/vendorAddVehicle", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> vendorAddVehicle(
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
            @RequestParam(name = "addedBy", required = false) Long addedBy,
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
        v.setIsAdminAdded(false);
        v.setIsAdminApproved(false);
        v.setAddedBy(addedBy == null ? "vendor" : String.valueOf(addedBy));
        v.setImage(saveFiles(image));
        vehicleRepository.save(v);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/showVendorVehilces")
    public ResponseEntity<?> showVendorVehicles(@RequestBody Map<String, Object> body) {
        // For simplicity, show vehicles added by this vendor
        String id = Objects.toString(body.get("_id"), "");
        List<Vehicle> all = vehicleRepository.findByIsDeleted("false");
        return ResponseEntity.ok(all.stream().filter(v -> Objects.equals(v.getAddedBy(), id)).toList());
    }

    @PutMapping("/vendorEditVehicles/{id}")
    public ResponseEntity<?> vendorEditVehicles(@PathVariable Long id, @RequestBody Map<String, Object> body) {
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

    @DeleteMapping("/vendorDeleteVehicles/{id}")
    public ResponseEntity<?> vendorDeleteVehicles(@PathVariable Long id) {
        return vehicleRepository.findById(id).map(v -> {
            v.setIsDeleted("true");
            vehicleRepository.save(v);
            return ResponseEntity.ok(Map.of("ok", true));
        }).orElse(ResponseEntity.status(404).body(err("vehicle not found")));
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
        try { return v.substring(0, 10); } catch (Exception e) { return "1970-01-01"; }
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
}
