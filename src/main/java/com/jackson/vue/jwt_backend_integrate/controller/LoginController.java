package com.jackson.vue.jwt_backend_integrate.controller;

import com.jackson.vue.jwt_backend_integrate.dto.AuthRequest;
import com.jackson.vue.jwt_backend_integrate.dto.AuthResponse;
import com.jackson.vue.jwt_backend_integrate.model.UserEntity;
import com.jackson.vue.jwt_backend_integrate.repository.UserRepository;
import com.jackson.vue.jwt_backend_integrate.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepo;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        System.out.println("Login endpoint hit");
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()
                    )
            );
            String accessToken = jwtService.generateToken(request.getUsername());
            String refreshToken = jwtService.generateRefreshToken(request.getUsername());
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }


}
