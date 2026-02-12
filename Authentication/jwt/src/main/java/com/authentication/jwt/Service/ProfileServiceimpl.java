package com.authentication.jwt.Service;

import com.authentication.jwt.Entity.UserEntity;
import com.authentication.jwt.Repository.UserRepository;
import com.authentication.jwt.io.profileRequest;
import com.authentication.jwt.io.profileResponse;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProfileServiceimpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public profileResponse createProfile(profileRequest request) {
        UserEntity newProfile = convertToUserEntity(request);
        if (!userRepository.existsByEmail(newProfile.getEmail())) {
            newProfile = userRepository.save(newProfile);
            return convertToProfileResponse(newProfile);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");

    }

    @Override
    public profileResponse getProfile(String email) {
        UserEntity exsisitingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));
        return convertToProfileResponse(exsisitingUser);
    }

    @Override
    public void sendResetOtp(String email) {
        UserEntity exsisitingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));
        String otp = String.valueOf((int) ((Math.random() * 9000) + 1000));

        // calculate expiry time
        Long expiryTime = System.currentTimeMillis() + (15 * 60 * 1000);
        exsisitingUser.setResetOTP(otp);
        exsisitingUser.setResetOTPExpireAt(expiryTime);
        userRepository.save(exsisitingUser);
        try {
            emailService.sendResetOtp(email, otp);
        } catch (Exception e) {
            throw new RuntimeException("unable to send email");
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        UserEntity exsisitingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user not found" + email));
        if (exsisitingUser.getResetOTP() == null
                || exsisitingUser.getResetOTPExpireAt() <= System.currentTimeMillis()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP Expired");
        }
        if (!exsisitingUser.getResetOTP().equals(otp)) {
            throw new RuntimeException("invalid otp");
        }
        if (exsisitingUser.getResetOTPExpireAt() <= System.currentTimeMillis()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP Expired");
        }

        exsisitingUser.setPassword(passwordEncoder.encode(newPassword));
        exsisitingUser.setResetOTP(null);
        exsisitingUser.setResetOTPExpireAt(0L);
        userRepository.save(exsisitingUser);
    }

    @Override
    public void sendOtp(String email) {
        UserEntity exsisitingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user not found" + email));

        if (exsisitingUser.getIsAccountVerified() != null && exsisitingUser.getIsAccountVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account already verified");
        }

        // generete otp
        String otp = String.valueOf((int) ((Math.random() * 9000) + 1000));

        // calculate expiry time
        Long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
        exsisitingUser.setVerifyotp(otp);
        exsisitingUser.setVerifyOTPExpireAt(expiryTime);

        // saving in database
        userRepository.save(exsisitingUser);

        try {
            emailService.sendOtp(email, otp);
        } catch (Exception e) {
            throw new RuntimeException("unable to send email");
        }

    }

    @Override
    public void verifyOtp(String email, String otp) {
        UserEntity exsisitingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user not found" + email));
        if (exsisitingUser.getVerifyotp() == null || !exsisitingUser.getVerifyotp().equals(otp)) {
            throw new RuntimeException("invalid otp");
        }
        if (exsisitingUser.getVerifyOTPExpireAt() <= System.currentTimeMillis()) {
            throw new RuntimeException("otp expired");
        }
        exsisitingUser.setIsAccountVerified(true);
        exsisitingUser.setVerifyotp(null);
        exsisitingUser.setVerifyOTPExpireAt(0L);
        userRepository.save(exsisitingUser);
    }

    @Override
    public String getLoggedInUser(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user not found" + email));
        return user.getUserID();
    }

    private profileResponse convertToProfileResponse(UserEntity newProfile) {
        return profileResponse.builder()
                .email(newProfile.getEmail())
                .name(newProfile.getName())
                .userID(newProfile.getUserID())
                .isAccountVerified(newProfile.getIsAccountVerified())
                .build();
    }

    private UserEntity convertToUserEntity(profileRequest request) {
        return UserEntity.builder()
                .email(request.getEmail())
                .userID(UUID.randomUUID().toString())
                .Name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .isAccountVerified(false)
                .resetOTPExpireAt(0L)
                .Verifyotp(null)
                .VerifyOTPExpireAt(0L)
                .build();
    }

}
