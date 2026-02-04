package com.planotech.plano.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    @Column(unique = true, nullable = false)
    private String otpHash;

    @OneToOne
    private User user;

    @Column(nullable = false)
    private int attemptCount= 0;

    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean used=false;
}
