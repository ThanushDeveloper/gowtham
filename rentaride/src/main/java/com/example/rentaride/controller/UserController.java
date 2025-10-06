
package com.example.rentaride.controller;

import com.example.rentaride.domain.Booking;
import com.example.rentaride.domain.User;
import com.example.rentaride.domain.Vehicle;
import com.example.rentaride.dto.UserDtos;
import com.example.rentaride.repository.BookingRepository;
import com.example.rentaride.repository.UserRepository;
import com.example.rentaride.repository.VehicleRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final BookingRepository bookingRepository;

    public UserController(UserRepository userRepository, VehicleRepository vehicleRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/listAllVehicles")
    public ResponseEntity<?> listAllVehicles() {
        return ResponseEntity.ok(vehicleRepository.findAll());
    }

    @PostMapping("/showVehicleDetails")
    public ResponseEntity<?> showVehicleDetails(@RequestBody Map<String, Object> req) {
        Long id = Long.valueOf(String.valueOf(req.get("id")));
        return vehicleRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("succes", false, "message", "no vehicles found")));
    }

    @PostMapping("/editUserProfile/{id}")
    public ResponseEntity<?> editUserProfile(@PathVariable("id") Long id, @Valid @RequestBody UserDtos.ProfileEditRequest req) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("succes", false, "message", "user not found"));
        User u = opt.get();
        if (req.username() != null) u.setUsername(req.username());
        if (req.email() != null) u.setEmail(req.email());
        if (req.phoneNumber() != null) u.setPhoneNumber(req.phoneNumber());
        if (req.adress() != null) u.setAddress(req.adress());
        userRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "updated"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        if (!userRepository.existsById(id)) return ResponseEntity.status(404).body(Map.of("succes", false));
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "deleted"));
    }

    @PostMapping("/filterVehicles")
    public ResponseEntity<?> filterVehicles(@RequestBody List<Map<String, Object>> transformedData) {
        // Minimal implementation: filter by car_type and transmission locally
        List<Vehicle> all = vehicleRepository.findByIsDeleted("false");
        Set<String> carTypes = new HashSet<>();
        Set<String> transmissions = new HashSet<>();
        for (Map<String, Object> cur: transformedData) {
            String type = String.valueOf(cur.get("type"));
            if ("car_type".equals(type)) {
                cur.keySet().stream().filter(k -> !"type".equals(k) && Boolean.TRUE.equals(cur.get(k))).forEach(carTypes::add);
            }
            if ("transmition".equals(type)) {
                cur.keySet().stream().filter(k -> !"type".equals(k) && Boolean.TRUE.equals(cur.get(k))).forEach(transmissions::add);
            }
        }
        List<Vehicle> filtered = new ArrayList<>();
        for (Vehicle v : all) {
            boolean ok = true;
            if (!carTypes.isEmpty()) ok &= carTypes.contains(Optional.ofNullable(v.getCarType()).orElse(""));
            if (!transmissions.isEmpty()) ok &= transmissions.contains(Optional.ofNullable(v.getTransmission()).orElse(""));
            if (ok) filtered.add(v);
        }
        return ResponseEntity.ok(Map.of("status", "success", "data", Map.of("filteredVehicles", filtered)));
    }

    @PostMapping("/getVehiclesWithoutBooking")
    public ResponseEntity<?> getVehiclesWithoutBooking(@RequestBody Map<String, Object> body) {
        String pickUpDistrict = Objects.toString(body.get("pickUpDistrict"), null);
        String pickUpLocation = Objects.toString(body.get("pickUpLocation"), null);
        String pickupDate = Objects.toString(body.get("pickupDate"), null);
        String dropOffDate = Objects.toString(body.get("dropOffDate"), null);
        String model = Objects.toString(body.get("model"), null);
        if (pickUpDistrict == null || pickUpLocation == null || pickupDate == null || dropOffDate == null) {
            return ResponseEntity.status(409).body(Map.of("message", "Missing Required Fields"));
        }
        List<Vehicle> candidates = vehicleRepository.findByDistrictAndLocationAndIsDeleted(pickUpDistrict, pickUpLocation, "false");
        // Not checking overlaps now; assume all available for MVP
        List<Vehicle> available = candidates;
        if (model != null) {
            List<Vehicle> sameModel = new ArrayList<>();
            for (Vehicle v : available) if (model.equals(v.getModel())) sameModel.add(v);
            return ResponseEntity.ok(sameModel);
        }
        return ResponseEntity.ok(available);
    }

    @PostMapping("/showSingleofSameModel")
    public ResponseEntity<?> showSingleOfSameModel(@RequestBody Map<String, Object> body) {
        String pickUpDistrict = Objects.toString(body.get("pickUpDistrict"), null);
        String pickUpLocation = Objects.toString(body.get("pickUpLocation"), null);
        String pickupDate = Objects.toString(body.get("pickupDate"), null);
        String dropOffDate = Objects.toString(body.get("dropOffDate"), null);
        if (pickUpDistrict == null || pickUpLocation == null || pickupDate == null || dropOffDate == null) {
            return ResponseEntity.status(409).body(Map.of("message", "Missing Required Fields"));
        }
        List<Vehicle> available = vehicleRepository.findByDistrictAndLocationAndIsDeleted(pickUpDistrict, pickUpLocation, "false");
        Map<String, Vehicle> uniqueByModel = new LinkedHashMap<>();
        for (Vehicle v : available) uniqueByModel.putIfAbsent(Optional.ofNullable(v.getModel()).orElse(""), v);
        return ResponseEntity.ok(uniqueByModel.values());
    }

    @PostMapping("/bookCar")
    public ResponseEntity<?> bookCar(@Valid @RequestBody UserDtos.BookingRequest req) {
        Long userId = Long.valueOf(req.user_id());
        Long vehicleId = Long.valueOf(req.vehicle_id());
        Optional<User> u = userRepository.findById(userId);
        Optional<Vehicle> v = vehicleRepository.findById(vehicleId);
        if (u.isEmpty() || v.isEmpty()) return ResponseEntity.status(404).body(Map.of("succes", false));
        Booking b = new Booking();
        b.setUser(u.get());
        b.setVehicle(v.get());
        b.setPickupDate(Instant.parse(req.pickupDate()));
        b.setDropOffDate(Instant.parse(req.dropoffDate()));
        b.setPickUpLocation(req.pickup_location());
        b.setDropOffLocation(req.dropoff_location());
        b.setTotalPrice(req.totalPrice());
        b.setRazorpayPaymentId(req.razorpayPaymentId());
        b.setRazorpayOrderId(req.razorpayOrderId());
        b.setStatus("booked");
        bookingRepository.save(b);
        return ResponseEntity.ok(Map.of("message", "car booked successfully", "booked", b));
    }

    @PostMapping("/latestbookings")
    public ResponseEntity<?> latestBookings(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(String.valueOf(body.get("user_id")));
        return userRepository.findById(userId).map(user -> {
            List<Booking> bookings = bookingRepository.findByUserOrderByCreatedAtDesc(user);
            if (bookings.isEmpty()) return ResponseEntity.ok(List.of());
            Booking latest = bookings.get(0);
            Map<String, Object> dto = Map.of(
                    "bookingDetails", latest,
                    "vehicleDetails", latest.getVehicle()
            );
            return ResponseEntity.ok(List.of(dto));
        }).orElseGet(() -> ResponseEntity.ok(List.of()));
    }

    @PostMapping("/findBookingsOfUser")
    public ResponseEntity<?> findBookingsOfUser(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(String.valueOf(body.get("userId")));
        return userRepository.findById(userId).map(user -> {
            List<Booking> bookings = bookingRepository.findByUserOrderByCreatedAtDesc(user);
            List<Map<String, Object>> result = new ArrayList<>();
            for (Booking b : bookings) {
                result.add(Map.of(
                        "bookingDetails", b,
                        "vehicleDetails", b.getVehicle()
                ));
            }
            return ResponseEntity.ok(result);
        }).orElseGet(() -> ResponseEntity.ok(List.of()));
    }

    @PostMapping("/sendBookingDetailsEamil")
    public ResponseEntity<?> sendBookingDetailsEmail(@RequestBody Map<String, Object> body) {
        // Stub: accept payload and return ok so frontend proceeds
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/signout")
    public ResponseEntity<?> signout() {
        return ResponseEntity.ok(Map.of("message", "signed out"));
    }
}

