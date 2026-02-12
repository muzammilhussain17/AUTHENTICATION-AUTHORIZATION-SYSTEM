package com.authentication.jwt.controller;

import com.authentication.jwt.Service.AppUserDetailService;
import com.authentication.jwt.Service.EmailService;
import com.authentication.jwt.Service.ProfileService;
import com.authentication.jwt.Service.RefreshTokenService;
import com.authentication.jwt.Service.TokenBlacklistService;
import com.authentication.jwt.Util.JwtUtil;
import com.authentication.jwt.io.AuthRequest;
import com.authentication.jwt.io.AuthResponse;
import com.authentication.jwt.io.ResetPasswordRequest;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailService appUserDetailService;
    private final JwtUtil jwtUtil;
    private final ProfileService profileService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            authenticate(authRequest.getEmail(), authRequest.getPassword());
            final UserDetails userDetails = appUserDetailService.loadUserByUsername(authRequest.getEmail());
            final String accessToken = jwtUtil.generateToken(userDetails);
            final String role = jwtUtil.extractRole(accessToken);

            // Create refresh token and store in Redis
            final String refreshToken = refreshTokenService.createRefreshToken(authRequest.getEmail());

            // Access token cookie (short-lived, 30 min)
            ResponseCookie accessCookie = ResponseCookie
                    .from("token", accessToken)
                    .httpOnly(true)
                    .maxAge(Duration.ofMinutes(30))
                    .sameSite("Strict")
                    .path("/")
                    .build();

            // Refresh token cookie (long-lived, 7 days)
            ResponseCookie refreshCookie = ResponseCookie
                    .from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .maxAge(Duration.ofDays(7))
                    .sameSite("Strict")
                    .path("/")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(AuthResponse.builder()
                            .email(authRequest.getEmail())
                            .token(accessToken)
                            .role(role)
                            .build());

        } catch (BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (DisabledException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Account is disabled");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authorization failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * Refresh access token using the refresh token stored in Redis.
     * The refresh token is read from the HttpOnly cookie.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        // Extract refresh token from cookie
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Refresh token not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Validate refresh token from Redis
        String email = refreshTokenService.validateRefreshToken(refreshToken);
        if (email == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid or expired refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Generate new access token
        final UserDetails userDetails = appUserDetailService.loadUserByUsername(email);
        final String newAccessToken = jwtUtil.generateToken(userDetails);
        final String role = jwtUtil.extractRole(newAccessToken);

        ResponseCookie accessCookie = ResponseCookie
                .from("token", newAccessToken)
                .httpOnly(true)
                .maxAge(Duration.ofMinutes(30))
                .sameSite("Strict")
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(AuthResponse.builder()
                        .email(email)
                        .token(newAccessToken)
                        .role(role)
                        .build());
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    @GetMapping("/isAuthenticated")
    public ResponseEntity<Boolean> isAuthenticated(
            @CurrentSecurityContext(expression = "authentication.name") String email) {
        return ResponseEntity.ok(email != null);
    }

    @PostMapping("/sendOtp")
    public void sendResetOtp(@RequestParam String email) {
        try {
            profileService.sendResetOtp(email);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/resetPassword")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            profileService.resetPassword(
                    resetPasswordRequest.getEmail(),
                    resetPasswordRequest.getOtp(),
                    resetPasswordRequest.getNewPassword());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/send-Otp")
    public void sendVerifyOtp(
            @CurrentSecurityContext(expression = "authentication.name") String email) {
        try {
            profileService.sendOtp(email);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @RequestBody Map<String, String> request,
            @CurrentSecurityContext(expression = "authentication.name") String email) {

        if (request.get("otp") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "otp is required");
        }

        try {
            profileService.verifyOtp(email, request.get("otp").toString());
            return ResponseEntity.ok("Email verified successfully");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Blacklist the current access token
        String jwt = extractJwtFromRequest(request);
        if (jwt != null) {
            try {
                long expiry = jwtUtil.extractExpiration(jwt).getTime();
                tokenBlacklistService.blacklistToken(jwt, expiry);
            } catch (Exception e) {
                // Token may already be invalid, proceed with logout
            }
        }

        // Delete refresh token from Redis
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        if (refreshToken != null) {
            refreshTokenService.deleteRefreshToken(refreshToken);
        }

        // Clear both cookies
        ResponseCookie accessCookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(false)
                .maxAge(0)
                .sameSite("Strict")
                .path("/")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .maxAge(0)
                .sameSite("Strict")
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("logout successful");
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
