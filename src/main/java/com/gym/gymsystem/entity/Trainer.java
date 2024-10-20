package com.gym.gymsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "TRAINERS")
public class Trainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "TRAINER_SPECIALIZATION",
            joinColumns = @JoinColumn(name = "TRAINER_ID"),
            inverseJoinColumns = @JoinColumn(name = "TRAINING_TYPE_ID"))
    private List<TrainingType> specializations;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "USER_ID")
    private User user;
}
