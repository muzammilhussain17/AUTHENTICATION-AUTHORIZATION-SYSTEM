package com.authentication.jwt.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String email;
    private String token;
    private String role;
}
