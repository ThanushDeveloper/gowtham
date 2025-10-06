
package com.example.rentaride.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class RazorpayController {
    @PostMapping("/razorpay")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        // Stub a response that matches expected fields for frontend
        Integer totalPrice = (Integer) body.getOrDefault("totalPrice", 100);
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "amount", totalPrice * 100,
                "id", "order_DEV123",
                "currency", "INR"
        ));
    }
}

