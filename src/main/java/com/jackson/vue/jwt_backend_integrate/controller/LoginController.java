package com.jackson.vue.jwt_backend_integrate.controller;

import com.jackson.vue.jwt_backend_integrate.dto.AuthRequest;
import com.jackson.vue.jwt_backend_integrate.dto.AuthResponse;
import com.jackson.vue.jwt_backend_integrate.model.UserEntity;
import com.jackson.vue.jwt_backend_integrate.repository.UserRepository;
import com.jackson.vue.jwt_backend_integrate.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepo;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        System.out.println("reach login");
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );

        UserEntity user = userRepo.findByUserName(authRequest.getUsername())
                .orElseThrow();

        String jwt = jwtService.generateToken(user.getUserName());
        System.out.println("reach jwt");
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

}
