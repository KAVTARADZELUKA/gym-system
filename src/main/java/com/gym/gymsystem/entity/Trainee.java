package com.gym.gymsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "TRAINEES")
public class Trainee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateOfBirth;
    private String address;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "USER_ID")
    private User user;
}
