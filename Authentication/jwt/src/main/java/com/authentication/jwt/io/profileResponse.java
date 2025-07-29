package com.authentication.jwt.io;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class profileResponse {
    private String userID;
    private String name;
    private String email;
    private boolean isAccountVerified;
}
