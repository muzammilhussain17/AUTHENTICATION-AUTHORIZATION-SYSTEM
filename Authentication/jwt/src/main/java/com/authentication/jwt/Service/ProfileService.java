package com.authentication.jwt.Service;


import com.authentication.jwt.io.profileRequest;
import com.authentication.jwt.io.profileResponse;


public interface ProfileService {
    profileResponse createProfile(profileRequest request);

    profileResponse getProfile(String email);
    void sendResetOtp(String email);
    void resetPassword(String email,String otp,String newPassword);
    void sendOtp(String email);
    void verifyOtp(String email,String otp);
    String getLoggedInUser(String email);
}
