package com.gym.gymsystem.controller;

import com.gym.gymsystem.dto.trainer.TrainerProfileResponse;
import com.gym.gymsystem.dto.trainer.TrainerRegistrationRequest;
import com.gym.gymsystem.dto.trainer.UpdateTrainerProfileRequest;
import com.gym.gymsystem.dto.user.Message;
import com.gym.gymsystem.dto.user.UpdateStatusRequest;
import com.gym.gymsystem.entity.Trainee;
import com.gym.gymsystem.entity.Trainer;
import com.gym.gymsystem.exception.CustomAccessDeniedException;
import com.gym.gymsystem.service.AuthorizationService;
import com.gym.gymsystem.service.TrainerService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.Valid;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/trainer")
public class TrainerController {
    private final Converter<TrainerRegistrationRequest, Trainer> trainerConverter;
    private final TrainerService trainerService;
    private final MeterRegistry meterRegistry;
    private final AuthorizationService authorizationService;

    public TrainerController(Converter<TrainerRegistrationRequest, Trainer> trainerConverter, TrainerService trainerService, MeterRegistry meterRegistry, AuthorizationService authorizationService) {
        this.trainerConverter = trainerConverter;
        this.trainerService = trainerService;
        this.meterRegistry = meterRegistry;
        this.authorizationService = authorizationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> registerTrainer(@RequestBody @Valid TrainerRegistrationRequest request) {
        Timer timer = meterRegistry.timer("trainer.registration.timer");

        return timer.record(() -> {
            Map<String, String> response = trainerService.createTrainerProfile(trainerConverter.convert(request));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        });
    }

    @GetMapping("/{findUsername}")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(
            @PathVariable("findUsername") String findUsername) {
        if (!authorizationService.isAuthenticatedUser(findUsername)) {
            throw new CustomAccessDeniedException("You do not have permission to get this trainer's profile.");
        }
        TrainerProfileResponse response = trainerService.getTrainerProfileAndTraineesByUsername( findUsername);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{trainerUsername}")
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfile(
            @PathVariable("trainerUsername") String trainerUsername,
            @RequestBody @Valid UpdateTrainerProfileRequest request) {
        if (!authorizationService.isAuthenticatedUser(trainerUsername)) {
            throw new CustomAccessDeniedException("You do not have permission to update this trainer's profile.");
        }
        TrainerProfileResponse response = trainerService.updateProfile( trainerUsername,request);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @PatchMapping("/status/{findUsername}")
    public ResponseEntity<Message> updateTrainerStatus(
            @PathVariable("findUsername") String findUsername,
            @RequestBody UpdateStatusRequest request) {
        trainerService.updateTraineeStatus( findUsername, request.getIsActive());
        String status = request.getIsActive() ? "activated" : "deactivated";
        return ResponseEntity.ok(new Message("Trainer " + findUsername + " has been " + status + " successfully"));
    }
}
