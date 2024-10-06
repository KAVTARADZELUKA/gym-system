package com.gym.gymsystem.dto.trainee;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateTraineeProfileRequest {
    @NotBlank(message = "firstName is required")
    private String firstName;
    @NotBlank(message = "lastName is required")
    private String lastName;
    private String dateOfBirth;
    private String address;
    private Boolean isActive;
}
