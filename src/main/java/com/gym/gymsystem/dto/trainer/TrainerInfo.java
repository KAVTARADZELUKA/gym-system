package com.gym.gymsystem.dto.trainer;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrainerInfo {
    private String username;
    private String firstName;
    private String lastName;
    private String specialization;
}
