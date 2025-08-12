package com.jackson.vue.jwt_backend_integrate.controller;

import com.jackson.vue.jwt_backend_integrate.dto.auth.AuthRequest;
import com.jackson.vue.jwt_backend_integrate.model.UserEntity;
import com.jackson.vue.jwt_backend_integrate.repository.UserRepository;
import com.jackson.vue.jwt_backend_integrate.service.JwtService;
import com.jackson.vue.jwt_backend_integrate.service.RedisTokenBlacklistService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final RedisTokenBlacklistService blacklistService;

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
    public ResponseEntity<?> getNewToken(HttpServletRequest request, HttpServletResponse response) throws Exception {

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

        if (refreshToken == null || !jwtService.isValid(refreshToken)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        // Verify this is actually a refresh token
        if (!jwtService.isRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token type");
        }

        // Extract username and validate user exists
        String username = jwtService.extractUsername(refreshToken);
        UserEntity user = userRepo.findByUsername(username).orElseThrow();

        // Generate new tokens
        String newAccessToken = jwtService.generateToken(user.getUsername(), user.getRole());
        String newRefreshToken = jwtService.generateRefreshToken(username);
        
        System.out.println("Generated new tokens successfully");
        System.out.println("New access token JTI: " + jwtService.extractJti(newAccessToken));
        System.out.println("New refresh token JTI: " + jwtService.extractJti(newRefreshToken));
        
        // Set new refresh cookie (rotation)
        ResponseCookie newCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, newCookie.toString());
        System.out.println("New refresh token cookie set successfully");

        // Don't blacklist the refresh token during normal refresh operations
        // Only blacklist during logout to prevent replay attacks
        System.out.println("Token rotation completed successfully. Refresh token remains valid for future use.");

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));

    }

    @PostMapping("/logout")
    public ResponseEntity<?> processLogout( HttpServletResponse response, HttpServletRequest request) {

        //check the request route if include token so blacklist that jti token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            if (jwtService.isValid(accessToken)) {
                String accessJti = jwtService.extractJti(accessToken);
                Instant accessExp = jwtService.extractExpirationTime(accessToken).toInstant();
                blacklistService.blacklist(accessJti, accessExp);
            }
        }

        //check if cookie present so blacklist the cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Arrays.stream(cookies)
                    .filter(c -> "refreshToken".equals(c.getName()))
                    .findFirst()
                    .ifPresent(c -> {
                        String refresh = c.getValue();
                        if (jwtService.isValid(refresh)) {
                            String cJti = jwtService.extractJti(refresh);
                            Instant cExp = jwtService.extractExpirationTime(refresh).toInstant();
                            blacklistService.blacklist(cJti, cExp);
                        }
                    });
        }

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
