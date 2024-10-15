package com.gym.gymsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts")
@Data
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "last_attempt")
    private LocalDateTime lastAttempt;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

}