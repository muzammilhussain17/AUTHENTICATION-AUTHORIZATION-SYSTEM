package com.authentication.jwt.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(unique = true)
    private String userID;
    private String Name;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    private String Verifyotp;
    private Boolean isAccountVerified;
    private Long VerifyOTPExpireAt;
    private String resetOTP;
    private Long resetOTPExpireAt;
    @Column(nullable = false)
    private String role = "ROLE_USER";
    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;
    @UpdateTimestamp
    private Timestamp updatedAt;

}
