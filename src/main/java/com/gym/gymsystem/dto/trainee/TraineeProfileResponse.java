package com.gym.gymsystem.dto.trainee;

import com.gym.gymsystem.dto.trainer.TrainerInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TraineeProfileResponse {
    private String username;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String address;
    private boolean isActive;
    private List<TrainerInfo> trainers;
}
