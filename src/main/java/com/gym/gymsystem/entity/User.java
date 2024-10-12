package com.gym.gymsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String username;
    private String password;
    private String role;
    private Boolean isActive;
}
