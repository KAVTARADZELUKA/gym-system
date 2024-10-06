package com.gym.gymsystem.controller;

import com.gym.gymsystem.dto.trainer.TrainerProfileResponse;
import com.gym.gymsystem.dto.trainer.TrainerRegistrationRequest;
import com.gym.gymsystem.dto.trainer.UpdateTrainerProfileRequest;
import com.gym.gymsystem.dto.user.Message;
import com.gym.gymsystem.dto.user.UpdateStatusRequest;
import com.gym.gymsystem.entity.Trainee;
import com.gym.gymsystem.entity.Trainer;
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
@RequestMapping(value = "/api/trainer")
public class TrainerController {
    private final Converter<TrainerRegistrationRequest, Trainer> trainerConverter;
    private final TrainerService trainerService;
    private final MeterRegistry meterRegistry;

    public TrainerController(Converter<TrainerRegistrationRequest, Trainer> trainerConverter, TrainerService trainerService, MeterRegistry meterRegistry) {
        this.trainerConverter = trainerConverter;
        this.trainerService = trainerService;
        this.meterRegistry = meterRegistry;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> registerTrainer(@RequestBody @Valid TrainerRegistrationRequest request) {
        Timer timer = meterRegistry.timer("trainer.registration.timer");

        return timer.record(() -> {
            Trainer trainer = trainerService.createTrainerProfile(trainerConverter.convert(request));

            Map<String, String> response = new HashMap<>();
            response.put("username", trainer.getUser().getUsername());
            response.put("password", trainer.getUser().getPassword());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        });
    }

    @GetMapping("/{findUsername}")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(
            @PathVariable("findUsername") String findUsername) {
        TrainerProfileResponse response = trainerService.getTrainerProfileAndTraineesByUsername( findUsername);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{trainerUsername}")
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfile(
            @PathVariable("trainerUsername") String trainerUsername,
            @RequestBody @Valid UpdateTrainerProfileRequest request) {
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
