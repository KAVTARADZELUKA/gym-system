package com.gym.gymsystem.dto.trainee;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TraineeRegistrationRequest {
    @NotBlank(message = "firstName is required")
    String firstName;
    @NotBlank(message = "lastName is required")
    String lastName;
    String address;
    String dateOfBirth;
}
