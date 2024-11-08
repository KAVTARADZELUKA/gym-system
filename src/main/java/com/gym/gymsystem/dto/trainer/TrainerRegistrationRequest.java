package com.gym.gymsystem.dto.trainer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TrainerRegistrationRequest {
    String firstName;
    String lastName;
    @NotBlank(message = "specialization is required")
    String specialization;
}
