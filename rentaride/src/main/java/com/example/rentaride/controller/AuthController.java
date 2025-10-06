package com.example.rentaride.controller;

import com.example.rentaride.domain.User;
import com.example.rentaride.dto.AuthDtos;
import com.example.rentaride.repository.UserRepository;
import com.example.rentaride.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final TokenService tokenService;

    public AuthController(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AuthDtos.SignUpRequest req) {
        if (userRepository.existsByEmail(req.email()) || userRepository.existsByUsername(req.username())) {
            return ResponseEntity.status(409).body(Map.of("succes", false, "message", "user exists"));
        }
        User u = new User();
        u.setUsername(req.username());
        u.setEmail(req.email());
        u.setPassword(encoder.encode(req.password()));
        u.setUser(true);
        userRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "newUser added successfully"));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@Valid @RequestBody AuthDtos.SignInRequest req) {
        return userRepository.findByEmail(req.email())
                .filter(u -> encoder.matches(req.password(), u.getPassword()))
                .map(u -> {
                    String access = tokenService.generateAccessToken(u.getId(), 15 * 60);
                    String refresh = tokenService.generateRefreshToken(u.getId(), 7 * 24 * 60 * 60);
                    u.setRefreshToken(refresh);
                    userRepository.save(u);
                    return ResponseEntity.ok(Map.of(
                            "accessToken", access,
                            "refreshToken", refresh,
                            "isAdmin", u.isAdmin(),
                            "isUser", u.isUser(),
                            "_id", u.getId(),
                            "username", u.getUsername(),
                            "email", u.getEmail()
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("succes", false, "message", "wrong credentials")));
    }

    @PostMapping("/google")
    public ResponseEntity<?> google(@RequestBody AuthDtos.OAuthRequest req) {
        // Minimal flow: create-or-get user by email, mark as isUser
        User u = userRepository.findByEmail(req.email()).orElseGet(() -> {
            User nu = new User();
            nu.setEmail(req.email());
            nu.setUsername(req.name());
            nu.setPassword(encoder.encode("oauth-login"));
            nu.setProfilePicture(req.photo());
            nu.setUser(true);
            return userRepository.save(nu);
        });
        String access = tokenService.generateAccessToken(u.getId(), 15 * 60);
        String refresh = tokenService.generateRefreshToken(u.getId(), 7 * 24 * 60 * 60);
        u.setRefreshToken(refresh);
        userRepository.save(u);
        return ResponseEntity.ok(Map.of(
                "accessToken", access,
                "refreshToken", refresh,
                "isUser", true,
                "_id", u.getId(),
                "username", u.getUsername(),
                "email", u.getEmail()
        ));
    }
}
