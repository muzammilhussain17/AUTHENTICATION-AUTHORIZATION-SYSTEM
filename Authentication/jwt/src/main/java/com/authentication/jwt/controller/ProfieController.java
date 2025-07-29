package com.authentication.jwt.controller;

import com.authentication.jwt.Service.EmailService;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import com.authentication.jwt.Service.ProfileService;
import com.authentication.jwt.io.profileRequest;
import com.authentication.jwt.io.profileResponse;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfieController {
    private final ProfileService profileService;
    private final EmailService emailService;

    @PostMapping("/register")
    public profileResponse register(@RequestBody profileRequest request) {
        profileResponse response = profileService.createProfile(request);

        try {
            emailService.sendEmail(response.getEmail(), response.getName());
        } catch (Exception e) {
            System.err.println("❌ Email sending failed: " + e.getMessage());
            // Optional: Log or notify admin
        }

        return response;
    }
    @GetMapping("/profilee")
    public profileResponse getProfile(@CurrentSecurityContext(expression = "authentication.name") String email) {
        return profileService.getProfile(email);
    }


}