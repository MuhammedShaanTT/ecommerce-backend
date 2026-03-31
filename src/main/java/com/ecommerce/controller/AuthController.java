package com.ecommerce.controller;

import com.ecommerce.dto.auth.*;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.AuthService;
import com.ecommerce.service.EmailService;
import com.ecommerce.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        
        // Find user to send welcome email (non-blocking — failure won't affect registration)
        try {
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (user != null) {
                emailService.sendWelcomeEmail(user);
                emailService.sendAdminNotification("New User Registration", 
                    "A new user has registered: " + user.getName() + " (" + user.getEmail() + ") with role " + user.getRole().getName());
            }
        } catch (Exception e) {
            // Email failure should never prevent registration
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public Map<String, String> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return Map.of(
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole().getName());
    }

    @PutMapping("/profile")
    public Map<String, String> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (body.containsKey("name") && !body.get("name").isEmpty()) {
            user.setName(body.get("name"));
        }
        if (body.containsKey("password") && !body.get("password").isEmpty()) {
            user.setPassword(passwordEncoder.encode(body.get("password")));
        }

        userRepository.save(user);
        return Map.of(
                "message", "Profile updated",
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole().getName());
    }
}
