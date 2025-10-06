package com.example.rentaride.auth;

import com.example.rentaride.user.User;
import com.example.rentaride.user.UserRepository;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setUser(true);
        userRepository.save(user);
        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "newUser added successfully");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@Valid @RequestBody SigninRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null || !BCrypt.checkpw(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(err("wrong credentials"));
        }
        // For now, mimic token response expected by frontend
        Map<String, Object> resp = new HashMap<>();
        resp.put("accessToken", "dev-access-token");
        resp.put("refreshToken", "dev-refresh-token");
        resp.put("isAdmin", user.isAdmin());
        resp.put("isUser", user.isUser());
        resp.put("_id", user.getId());
        resp.put("username", user.getUsername());
        resp.put("email", user.getEmail());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/google")
    public ResponseEntity<?> google(@RequestBody Map<String, Object> body) {
        // Minimal stub that creates user if needed and returns basic profile
        String email = (String) body.get("email");
        String name = (String) body.get("name");
        String photo = (String) body.get("photo");
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setUsername(name.replaceAll("\\s+", "").toLowerCase());
            u.setPasswordHash(BCrypt.hashpw("oauth-google", BCrypt.gensalt()));
            u.setProfilePicture(photo);
            u.setUser(true);
            return userRepository.save(u);
        });
        Map<String, Object> resp = new HashMap<>();
        resp.put("_id", user.getId());
        resp.put("username", user.getUsername());
        resp.put("email", user.getEmail());
        resp.put("isUser", user.isUser());
        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> err(String message) {
        Map<String, Object> m = new HashMap<>();
        m.put("succes", false);
        m.put("message", message);
        return m;
    }

    @Data
    public static class SignupRequest {
        private String username;
        private String email;
        private String password;
    }

    @Data
    public static class SigninRequest {
        private String email;
        private String password;
    }
}

