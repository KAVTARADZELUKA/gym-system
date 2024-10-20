package com.gym.gymsystem.dto.training;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateTraineeTrainersRequest {
    @NotNull
    private List<String> trainersUsernames;
}
