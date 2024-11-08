package com.gym.gymsystem.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrainingInfoForTrainer {
    private String trainingName;
    private String trainingDate;
    private String trainingType;
    private Long trainingDuration;
    private String traineeName;
}
