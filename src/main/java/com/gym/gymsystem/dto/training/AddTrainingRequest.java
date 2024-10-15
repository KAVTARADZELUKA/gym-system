package com.gym.gymsystem.dto.training;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddTrainingRequest {
    @NotBlank(message = "Trainee username is required")
    private String traineeUsername;

    @NotBlank(message = "Trainer username is required")
    private String trainerUsername;

    @NotBlank(message = "Training name is required")
    private String trainingName;

    @NotBlank(message = "Training type is required")
    private String trainingType;

    @NotNull(message = "Training date is required")
    private String trainingDate;

    @NotNull(message = "Training duration is required")
    private Long trainingDuration;
}
