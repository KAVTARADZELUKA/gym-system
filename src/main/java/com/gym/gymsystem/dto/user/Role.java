package com.gym.gymsystem.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    ADMIN("ADMIN"),
    TRAINEE("TRAINEE"),
    TRAINER("TRAINER");

    private final String role;
}
