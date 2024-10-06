package com.gym.gymsystem.dto.trainer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateTrainerProfileRequest {
    @NotBlank(message = "firstName is required")
    private String firstName;
    @NotBlank(message = "lastName is required")
    private String lastName;
    @NotBlank(message = "specialization is required")
    private String specialization;
    private boolean isActive;
}
