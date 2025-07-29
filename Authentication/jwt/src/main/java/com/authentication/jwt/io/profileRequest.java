package com.authentication.jwt.io;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
public class profileRequest {

        @NotBlank(message = "Name cannot be blank")
        private String name;
        @Email(message="enter correct email")
        @NotBlank(message = "Email cannot be blank")
        private String email;
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Size(max = 20, message = "Password must not exceed 20 characters")
        private String password;

}
