package com.gym.gymsystem.dto.trainer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerInfo {
    private String username;
    private String firstName;
    private String lastName;
    private String specialization;
}
