package com.ecommerce.service;

import com.ecommerce.dto.auth.AuthResponse;
import com.ecommerce.dto.auth.LoginRequest;
import com.ecommerce.dto.auth.RegisterRequest;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;
        private final AuthenticationManager authenticationManager;
        private final CustomUserDetailsService userDetailsService;

        public AuthResponse register(RegisterRequest request) {

                // Duplicate email check
                if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                        throw new DuplicateResourceException("Email already registered: " + request.getEmail());
                }

                Role role = roleRepository.findByName(request.getRole())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Role not found: " + request.getRole()));

                User user = User.builder()
                                .name(request.getName())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(role)
                                .build();

                userRepository.save(user);

                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                String token = jwtUtil.generateToken(userDetails);

                return new AuthResponse(token);
        }

        public AuthResponse login(LoginRequest request) {

                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getEmail(),
                                                request.getPassword()));

                UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
                String token = jwtUtil.generateToken(userDetails);

                return new AuthResponse(token);
        }
}
