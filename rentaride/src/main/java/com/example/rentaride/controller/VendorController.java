
package com.example.rentaride.controller;

import com.example.rentaride.domain.User;
import com.example.rentaride.domain.Vehicle;
import com.example.rentaride.repository.UserRepository;
import com.example.rentaride.repository.VehicleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/vendor")
public class VendorController {
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public VendorController(UserRepository userRepository, VehicleRepository vehicleRepository) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @PostMapping("/vendorsignup")
    public ResponseEntity<?> vendorSignup(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String email = req.get("email");
        String password = req.get("password");
        if (userRepository.existsByEmail(email)) return ResponseEntity.status(409).body(Map.of("succes", false));
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(encoder.encode(password));
        u.setVendor(true);
        userRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "vendor created"));
    }

    @PostMapping("/vendorsignin")
    public ResponseEntity<?> vendorSignin(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String password = req.get("password");
        return userRepository.findByEmail(email)
                .filter(u -> u.isVendor() && encoder.matches(password, u.getPassword()))
                .map(u -> ResponseEntity.ok(Map.of("isVendor", true, "_id", u.getId(), "username", u.getUsername(), "email", u.getEmail())))
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("succes", false)));
    }

    @PostMapping("/vendorgoogle")
    public ResponseEntity<?> vendorGoogle(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String name = req.get("name");
        String photo = req.get("photo");
        User u = userRepository.findByEmail(email).orElseGet(() -> {
            User nu = new User();
            nu.setEmail(email);
            nu.setUsername(name);
            nu.setPassword(encoder.encode("oauth-login"));
            nu.setProfilePicture(photo);
            nu.setVendor(true);
            return userRepository.save(nu);
        });
        return ResponseEntity.ok(Map.of("isVendor", true, "_id", u.getId(), "username", u.getUsername(), "email", u.getEmail()));
    }

    @PostMapping("/vendorAddVehicle")
    public ResponseEntity<?> vendorAddVehicle(@RequestParam Map<String, String> params,
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
        v.setIsAdminAdded(false);
        v.setAddedBy(params.get("addedBy"));
        v.setIsAdminApproved(false);
        if (images != null) {
            List<String> urls = new ArrayList<>();
            for (MultipartFile file : images) {
                urls.add("/uploads/" + file.getOriginalFilename());
            }
            v.setImage(urls);
        }
        vehicleRepository.save(v);
        return ResponseEntity.ok(Map.of("message", "request send to admin"));
    }

    @PostMapping("/showVendorVehilces")
    public ResponseEntity<?> showVendorVehicles(@RequestBody Map<String, Object> body) {
        String id = Objects.toString(body.get("_id"));
        List<Vehicle> all = vehicleRepository.findByIsDeleted("false");
        List<Vehicle> out = new ArrayList<>();
        for (Vehicle v: all) {
            if (Objects.toString(v.getAddedBy(), "").equals(id) && Boolean.FALSE.equals(v.getIsAdminAdded())) out.add(v);
        }
        return ResponseEntity.ok(out);
    }

    @PutMapping("/vendorEditVehicles/{id}")
    public ResponseEntity<?> vendorEditVehicles(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        return vehicleRepository.findById(id).map(v -> {
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
        }).orElseGet(() -> ResponseEntity.status(404).body(Map.of("succes", false)));
    }

    @DeleteMapping("/vendorDeleteVehicles/{id}")
    public ResponseEntity<?> vendorDeleteVehicles(@PathVariable("id") Long id) {
        return vehicleRepository.findById(id).map(v -> {
            v.setIsDeleted("true");
            vehicleRepository.save(v);
            return ResponseEntity.ok(Map.of("message", "deleted successfully"));
        }).orElseGet(() -> ResponseEntity.status(404).body(Map.of("succes", false)));
    }

    private Integer parseIntOrNull(String s) {
        try { return s == null ? null : Integer.valueOf(s); } catch (Exception e) { return null; }
    }
}

