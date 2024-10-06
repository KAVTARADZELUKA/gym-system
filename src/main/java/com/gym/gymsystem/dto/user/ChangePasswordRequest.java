package com.gym.gymsystem.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "username is required")
    String username;
    @NotBlank(message = "oldPassword is required")
    String oldPassword;
    @NotBlank(message = "newPassword is required")
    String newPassword;
}
