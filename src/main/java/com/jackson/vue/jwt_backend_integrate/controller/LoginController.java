package com.jackson.vue.jwt_backend_integrate.controller;

import com.jackson.vue.jwt_backend_integrate.dto.AuthRequest;
import com.jackson.vue.jwt_backend_integrate.dto.AuthResponse;
import com.jackson.vue.jwt_backend_integrate.model.UserEntity;
import com.jackson.vue.jwt_backend_integrate.repository.UserRepository;
import com.jackson.vue.jwt_backend_integrate.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepo;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request, HttpServletResponse response) {
        System.out.println("Login endpoint hit");
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()
                    )
            );
            UserEntity user = userRepo.findByUsername(request.getUsername()).orElseThrow();
            System.out.println("User role " + user.getRole());
            String accessToken = jwtService.generateToken(user.getUsername(), user.getRole());
            String refreshToken = jwtService.generateRefreshToken(request.getUsername());

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(7 * 24 * 60 * 60)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            System.out.println("Access token " + accessToken);
            return ResponseEntity.ok(Map.of("accessToken", accessToken));
        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/refreshNewToken")
    public ResponseEntity<?> getNewToken(HttpServletRequest request) throws Exception {

        System.out.println("reach call refresh token");

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new Exception("No cookies found in request");
        }

        String refreshToken = Arrays.stream(cookies)
                .filter(c -> "refreshToken".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new Exception("No refresh token"));


        String username = jwtService.extractUsername(refreshToken);
        UserEntity user = userRepo.findByUsername(username).orElseThrow();
        String newAccessToken = jwtService.generateToken(user.getUsername(), user.getRole());

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));

    }

    @PostMapping("/logout")
    public ResponseEntity<?> processLogout( HttpServletResponse response){

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.ok("Logout successfully");
    }


}
