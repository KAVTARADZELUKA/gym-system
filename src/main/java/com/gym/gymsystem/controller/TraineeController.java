package com.gym.gymsystem.controller;


import com.gym.gymsystem.dto.trainee.TraineeProfileResponse;
import com.gym.gymsystem.dto.trainee.TraineeRegistrationRequest;
import com.gym.gymsystem.dto.trainee.UpdateTraineeProfileRequest;
import com.gym.gymsystem.dto.user.Message;
import com.gym.gymsystem.dto.user.UpdateStatusRequest;
import com.gym.gymsystem.entity.Trainee;
import com.gym.gymsystem.service.TraineeService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.Valid;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/trainee")
public class TraineeController {
    private final TraineeService traineeService;
    private final Converter<TraineeRegistrationRequest, Trainee> traineeConverter;
    private final MeterRegistry meterRegistry;
    private final Counter traineeDeletionCounter;

    public TraineeController(TraineeService traineeService, Converter<TraineeRegistrationRequest, Trainee> traineeConverter, MeterRegistry meterRegistry) {
        this.traineeService = traineeService;
        this.traineeConverter = traineeConverter;
        this.meterRegistry = meterRegistry;
        this.traineeDeletionCounter = meterRegistry.counter("trainee.deletion.count");
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> registerTrainee(@RequestBody @Valid TraineeRegistrationRequest request) {
        Timer timer = meterRegistry.timer("trainee.registration.timer");

        return timer.record(() -> {
            Trainee trainee = traineeService.createTraineeProfile(traineeConverter.convert(request));

            Map<String, String> response = new HashMap<>();
            response.put("username", trainee.getUser().getUsername());
            response.put("password", trainee.getUser().getPassword());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        });
    }

    @GetMapping("/{findUsername}")
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(
            @PathVariable("findUsername") String findUsername) {
        TraineeProfileResponse response = traineeService.getTraineeProfileAndTrainersByUsername(findUsername);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{traineeUsername}")
    public ResponseEntity<TraineeProfileResponse> updateTraineeProfile(
            @PathVariable("traineeUsername") String traineeUsername,
            @RequestBody @Valid UpdateTraineeProfileRequest request) {
        Trainee trainee = traineeService.getTraineeProfileByUsername(traineeUsername);
        traineeService.updateTraineeProfile(traineeService.fromUpdateRequest(request, trainee));
        TraineeProfileResponse response = traineeService.getTraineeProfileAndTrainersByUsername(traineeUsername);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{traineeUsername}")
    public ResponseEntity<Message> deleteTraineeProfile(
            @PathVariable("traineeUsername") String traineeUsername) {
        String response = traineeService.deleteTraineeByUsername(traineeUsername);

        traineeDeletionCounter.increment();
        return ResponseEntity.ok(new Message(response));
    }

    @PatchMapping("/status/{findUsername}")
    public ResponseEntity<Message> updateTraineeStatus(
            @PathVariable("findUsername") String findUsername,
            @RequestBody UpdateStatusRequest request) {
        traineeService.updateTraineeStatus(findUsername, request.getIsActive());
        String status = request.getIsActive() ? "activated" : "deactivated";
        return ResponseEntity.ok(new Message("Trainee " + findUsername + " has been " + status + " successfully"));
    }
}
