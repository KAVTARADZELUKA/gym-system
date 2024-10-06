package com.gym.gymsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "TRAINING")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "TRAINING_TRAINEE",
            joinColumns = @JoinColumn(name = "TRAINING_ID"),
            inverseJoinColumns = @JoinColumn(name = "TRAINEE_ID"))
    private List<Trainee> trainees;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "TRAINING_TRAINER",
            joinColumns = @JoinColumn(name = "TRAINING_ID"),
            inverseJoinColumns = @JoinColumn(name = "TRAINER_ID"))
    private List<Trainer> trainers;
    private String name;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TRAINING_TYPE_ID")
    private TrainingType trainingType;
    private LocalDateTime trainingDate;
    private Long trainingDuration;
}
