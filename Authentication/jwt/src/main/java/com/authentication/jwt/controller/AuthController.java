package com.authentication.jwt.controller;


import com.authentication.jwt.Service.AppUserDetailService;
import com.authentication.jwt.Service.EmailService;
import com.authentication.jwt.Service.ProfileService;
import com.authentication.jwt.Util.JwtUtil;
import com.authentication.jwt.io.AuthRequest;
import com.authentication.jwt.io.AuthResponse;
import com.authentication.jwt.io.ResetPasswordRequest;
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
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
try {
        authenticate(authRequest.getEmail(),
                authRequest.getPassword());
        final UserDetails userDetails = appUserDetailService.loadUserByUsername(authRequest.getEmail());
        final String token = jwtUtil.generateToken(userDetails);
    ResponseCookie cookie=ResponseCookie.
            from("token",token).httpOnly(true)
            .maxAge(Duration.ofDays(1))
            .sameSite("Strict")
            .path("/")
            .build();
    return ResponseEntity.ok().header(HttpHeaders .SET_COOKIE,cookie.toString())
            .body(new AuthResponse(authRequest.getEmail(), token));

    }
catch (BadCredentialsException e) {
    Map<String, String> error = new HashMap<>();
    error.put("error", "Invalid credentials");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    }
catch (DisabledException e) {
    Map<String, String> error = new HashMap<>();
    error.put("error", "Account is disabled");
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

}
catch (Exception e) {
    Map<String, String> error = new HashMap<>();
    error.put("error", "Authorization failed");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);

}

    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }
@GetMapping("/isAuthenticated")
    public ResponseEntity<Boolean> isAuthenticated(@CurrentSecurityContext(expression = "authentication.name") String email) {
        return ResponseEntity.ok(email != null);
    }

    @PostMapping("/sendOtp")
    public void sendResetOtp(@RequestParam String email) {
        try{
            profileService.sendResetOtp(email);
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    @PostMapping("/resetPassword")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        try{
            profileService.resetPassword(resetPasswordRequest.getEmail(),resetPasswordRequest.getOtp(),resetPasswordRequest.getNewPassword());
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
@PostMapping("/send-Otp")
    public void sendVerifyOtp(@CurrentSecurityContext(expression = "authentication.name") String email) {
        try{
            profileService.sendOtp(email);
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }
    @PostMapping("/verify-otp")
    public void verifyOtp(@RequestBody Map<String,String> request,
                          @CurrentSecurityContext(expression = "authentication.name") String email) {

       if(request.get("otp")==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "otp is required");
        }

        try{
            profileService.verifyOtp(email,request.get("otp").toString());
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }
}
