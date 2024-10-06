package com.gym.gymsystem.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "username is required")
    String username;
    @NotBlank(message = "password is required")
    String password;
}
