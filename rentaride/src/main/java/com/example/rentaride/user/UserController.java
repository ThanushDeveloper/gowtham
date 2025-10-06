
package com.example.rentaride.user;

import com.example.rentaride.booking.Booking;
import com.example.rentaride.booking.BookingRepository;
import com.example.rentaride.vehicle.Vehicle;
import com.example.rentaride.vehicle.VehicleRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final VehicleRepository vehicleRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @GetMapping("/listAllVehicles")
    public ResponseEntity<?> listAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findByIsAdminApprovedTrueAndIsDeleted("false");
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping("/showVehicleDetails")
    public ResponseEntity<?> showVehicleDetails(@RequestBody Map<String, Object> body) {
        // In original FE, body contains { id }
        // We'll search by numeric Long id
        Long id = null;
        try { id = Long.valueOf(String.valueOf(body.get("id"))); } catch (Exception ignored) {}
        if (id == null) return ResponseEntity.badRequest().body(err("invalid id"));
        var opt = vehicleRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(err("vehicle not found"));
        return ResponseEntity.ok(opt.get());
    }

    @PostMapping("/filterVehicles")
    public ResponseEntity<?> filterVehicles(@RequestBody List<FilterItem> filters) {
        // naive filter over all approved vehicles
        List<Vehicle> all = vehicleRepository.findByIsAdminApprovedTrueAndIsDeleted("false");
        Set<Vehicle> out = new LinkedHashSet<>();
        for (FilterItem f : filters) {
            switch (Objects.toString(f.getType(), "")) {
                case "car_type" -> out.addAll(all.stream().filter(v -> f.keyMatches(v.getCarType())).toList());
                case "transmition" -> out.addAll(all.stream().filter(v -> f.keyMatches(v.getTransmition())).toList());
                default -> {}
            }
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("data", Map.of("filteredVehicles", out.isEmpty() ? all : out));
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/getVehiclesWithoutBooking")
    public ResponseEntity<?> getVehiclesWithoutBooking(@RequestBody AvailabilityQuery q) {
        // For simplicity, return all vehicles in district+location, later restrict by time window
        List<Vehicle> matches = vehicleRepository
                .findByDistrictAndLocationAndIsDeleted(q.getPickUpDistrict(), q.getPickUpLocation(), "false");
        return ResponseEntity.ok(matches);
    }

    @PostMapping("/showSingleofSameModel")
    public ResponseEntity<?> showSingleofSameModel(@RequestBody AvailabilityQuery q) {
        // Return distinct models as simple list (simulate original behavior)
        List<Vehicle> matches = vehicleRepository.findByDistrictAndLocationAndIsDeleted(
                q.getPickUpDistrict(), q.getPickUpLocation(), "false");
        Map<String, Vehicle> byModel = matches.stream()
                .collect(Collectors.toMap(Vehicle::getModel, v -> v, (a,b)->a, LinkedHashMap::new));
        return ResponseEntity.ok(new ArrayList<>(byModel.values()));
    }

    @PostMapping("/bookCar")
    public ResponseEntity<?> bookCar(@RequestBody BookingRequest req) {
        Vehicle vehicle = vehicleRepository.findById(req.getVehicle_id()).orElse(null);
        if (vehicle == null) return ResponseEntity.status(404).body(err("vehicle not found"));
        User user = userRepository.findById(req.getUser_id()).orElse(null);
        Booking b = new Booking();
        b.setVehicle(vehicle);
        b.setUser(user);
        b.setPickupDate(LocalDateTime.parse(req.getPickupDate().replace("Z","")));
        b.setDropOffDate(LocalDateTime.parse(req.getDropoffDate().replace("Z","")));
        b.setPickUpLocation(req.getPickup_district());
        b.setDropOffLocation(req.getDropoff_location());
        b.setTotalPrice(req.getTotalPrice());
        b.setRazorpayOrderId(Optional.ofNullable(req.getRazorpayOrderId()).orElse("dev-order"));
        b.setRazorpayPaymentId(Optional.ofNullable(req.getRazorpayPaymentId()).orElse("dev-pay"));
        bookingRepository.save(b);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/latestbookings")
    public ResponseEntity<?> latestBookings(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(String.valueOf(body.get("user_id")));
        User u = userRepository.findById(userId).orElse(null);
        if (u == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(bookingRepository.findTop5ByUserOrderByCreatedAtDesc(u));
    }

    @PostMapping("/findBookingsOfUser")
    public ResponseEntity<?> findBookingsOfUser(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(String.valueOf(body.get("userId")));
        User u = userRepository.findById(userId).orElse(null);
        if (u == null) return ResponseEntity.ok(List.of());
        List<Booking> bookings = bookingRepository.findByUser(u);
        // mimic original shape including nested vehicleDetails
        List<Map<String, Object>> out = new ArrayList<>();
        for (Booking b : bookings) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("_id", b.getId());
            Map<String, Object> bookingDetails = new LinkedHashMap<>();
            bookingDetails.put("_id", b.getId());
            bookingDetails.put("totalPrice", b.getTotalPrice());
            bookingDetails.put("pickupDate", b.getPickupDate());
            bookingDetails.put("dropOffDate", b.getDropOffDate());
            m.put("bookingDetails", bookingDetails);
            Map<String, Object> vehicleDetails = new LinkedHashMap<>();
            vehicleDetails.put("image", b.getVehicle().getImage());
            vehicleDetails.put("name", b.getVehicle().getName());
            m.put("vehicleDetails", vehicleDetails);
            m.put("pickUpLocation", b.getPickUpLocation());
            m.put("dropOffLocation", b.getDropOffLocation());
            out.add(m);
        }
        return ResponseEntity.ok(out);
    }

    @PostMapping("/sendBookingDetailsEamil")
    public ResponseEntity<?> sendBookingDetailsEmail(@RequestBody Map<String, Object> body) {
        // Stub OK response to satisfy FE flow
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/razorpay")
    public ResponseEntity<?> razorpay(@RequestBody Map<String, Object> order) {
        // Stub order create response matching FE expectations
        Map<String, Object> resp = new HashMap<>();
        resp.put("ok", true);
        resp.put("amount", order.getOrDefault("totalPrice", 100));
        resp.put("id", UUID.randomUUID().toString());
        resp.put("currency", "INR");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/editUserProfile/{id}")
    public ResponseEntity<?> editUserProfile(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        var opt = userRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(err("user not found"));
        var u = opt.get();
        Map<String, Object> fd = (Map<String, Object>) body.get("formData");
        if (fd.get("username") != null) u.setUsername(fd.get("username").toString());
        if (fd.get("email") != null) u.setEmail(fd.get("email").toString());
        if (fd.get("phoneNumber") != null) u.setPhoneNumber(fd.get("phoneNumber").toString());
        if (fd.get("adress") != null) u.setAdress(fd.get("adress").toString());
        userRepository.save(u);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    private Map<String, Object> err(String message) {
        Map<String, Object> m = new HashMap<>();
        m.put("succes", false);
        m.put("message", message);
        return m;
    }

    @Data
    public static class FilterItem {
        private String type;
        private Boolean suv;
        private Boolean sedan;
        private Boolean hatchback;
        private Boolean automatic;
        private Boolean manual;

        public boolean keyMatches(String value) {
            if (value == null) return false;
            if (Boolean.TRUE.equals(suv) && value.equals("suv")) return true;
            if (Boolean.TRUE.equals(sedan) && value.equals("sedan")) return true;
            if (Boolean.TRUE.equals(hatchback) && value.equals("hatchback")) return true;
            if (Boolean.TRUE.equals(automatic) && value.equals("automatic")) return true;
            if (Boolean.TRUE.equals(manual) && value.equals("manual")) return true;
            return false;
        }
    }

    @Data
    public static class AvailabilityQuery {
        private String pickUpDistrict;
        private String pickUpLocation;
        private String model;
        private String pickupDate;
        private String dropOffDate;
    }

    @Data
    public static class BookingRequest {
        private Long user_id;
        private Long vehicle_id;
        private Integer totalPrice;
        private String pickupDate;
        private String dropoffDate;
        private String pickup_district;
        private String pickup_location;
        private String dropoff_location;
        private String razorpayOrderId;
        private String razorpayPaymentId;
    }
}


