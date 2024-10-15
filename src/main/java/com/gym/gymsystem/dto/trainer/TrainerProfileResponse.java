package com.gym.gymsystem.dto.trainer;

import com.gym.gymsystem.dto.trainee.TraineeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerProfileResponse {
    private String firstName;
    private String lastName;
    private String specialization;
    private boolean isActive;
    private List<TraineeInfo> trainees;
}
