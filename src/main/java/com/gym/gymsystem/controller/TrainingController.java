package com.gym.gymsystem.controller;

import com.gym.gymsystem.dto.trainer.TrainerInfo;
import com.gym.gymsystem.dto.training.AddTrainingRequest;
import com.gym.gymsystem.dto.training.TrainingInfo;
import com.gym.gymsystem.dto.training.TrainingInfoForTrainer;
import com.gym.gymsystem.dto.training.UpdateTraineeTrainersRequest;
import com.gym.gymsystem.dto.user.Message;
import com.gym.gymsystem.entity.Trainer;
import com.gym.gymsystem.entity.Training;
import com.gym.gymsystem.entity.TrainingType;
import com.gym.gymsystem.exception.CustomAccessDeniedException;
import com.gym.gymsystem.service.AuthorizationService;
import com.gym.gymsystem.service.TraineeService;
import com.gym.gymsystem.service.TrainerService;
import com.gym.gymsystem.service.TrainingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/training")
public class TrainingController {
    private final TrainingService trainingService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final AuthorizationService authorizationService;

    public TrainingController(TrainingService trainingService, TraineeService traineeService, TrainerService trainerService, AuthorizationService authorizationService) {
        this.trainingService = trainingService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/trainers/not-assigned")
    public ResponseEntity<List<TrainerInfo>> getNotAssignedActiveTrainers(
            @RequestParam("findUsername") String findUsername) {
        if (!authorizationService.isAdmin() && !authorizationService.isTrainer() && !authorizationService.isAuthenticatedUser(findUsername)) {
            throw new CustomAccessDeniedException("You do not have permission to get this trainee's not assigned trainers.");
        }
        List<TrainerInfo> trainerInfos = trainingService.getTrainersNotAssignedToTrainee(findUsername);
        return ResponseEntity.ok(trainerInfos);
    }

    @PutMapping("/trainee/{traineeUsername}")
    public ResponseEntity<List<TrainerInfo>> updateTraineeTrainers(
            @PathVariable("traineeUsername") String findUsername,
            @RequestBody UpdateTraineeTrainersRequest request) {

        if (!authorizationService.isAdmin()  && !authorizationService.isAuthenticatedUser(findUsername)) {
            throw new CustomAccessDeniedException("You do not have permission to update this trainee's trainers.");
        }
        List<Training> updatedTrainers = trainingService.updateTraineeTrainersByUsername(findUsername, request.getTrainersUsernames());
        List<TrainerInfo> trainerInfos = updatedTrainers.stream()
                .flatMap(training -> training.getTrainers().stream())
                .map(trainer -> new TrainerInfo(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecializations().stream()
                                .findFirst()
                                .map(TrainingType::getTrainingTypeName).get()
                ))
                .distinct()
                .toList();

        return ResponseEntity.ok(trainerInfos);
    }

    @GetMapping("/trainee/{traineeUsername}")
    public ResponseEntity<List<TrainingInfo>> getTraineeTrainings(
            @PathVariable("traineeUsername") String traineeUsername,
            @RequestParam("periodFrom") String periodFrom,
            @RequestParam("periodTo") String periodTo,
            @RequestParam(value = "trainerName", required = false) String trainerName,
            @RequestParam(value = "trainingType", required = false) String trainingType) {

        if (!authorizationService.isAdmin() && !authorizationService.isTrainer() && !authorizationService.isAuthenticatedUser(traineeUsername)) {
            throw new CustomAccessDeniedException("You do not have permission to get this trainee's trainings.");
        }
        List<Training> trainings = trainingService.findTrainingsByTraineeAndCriteria(
                traineeUsername,
                LocalDate.parse(periodFrom).atStartOfDay(), LocalDate.parse(periodTo).atStartOfDay(),
                trainerName, trainingType);

        List<TrainingInfo> trainingInfos = trainings.stream()
                .map(training -> new TrainingInfo(
                        training.getName(),
                        training.getTrainingDate() != null ? training.getTrainingDate().toString() : "N/A",
                        training.getTrainingType().getTrainingTypeName(),
                        training.getTrainingDuration(),
                        (training.getTrainers() != null && !training.getTrainers().isEmpty())
                                ? training.getTrainers().getFirst().getUser().getUsername()
                                : "N/A"
                )).toList();
        return ResponseEntity.ok(trainingInfos);
    }

    @GetMapping("/trainer/{trainerUsername}")
    public ResponseEntity<List<TrainingInfoForTrainer>> getTrainerTrainings(
            @PathVariable("trainerUsername") String trainerUsername,
            @RequestParam("periodFrom") String periodFrom,
            @RequestParam("periodTo") String periodTo,
            @RequestParam(value = "traineeName", required = false) String traineeName,
            @RequestParam(value = "trainingType", required = false) String trainingType) {

        if (!authorizationService.isAdmin() && !authorizationService.isAuthenticatedUser(trainerUsername)) {
            throw new CustomAccessDeniedException("You do not have permission to get this trainer's trainings.");
        }

        List<Training> trainings = trainingService.findTrainingsByTrainerAndCriteria(
                trainerUsername,
                LocalDate.parse(periodFrom).atStartOfDay(), LocalDate.parse(periodTo).atStartOfDay(),
                traineeName, trainingType);

        List<TrainingInfoForTrainer> trainingInfos = trainings.stream()
                .map(training -> new TrainingInfoForTrainer(
                        training.getName(),
                        training.getTrainingDate() != null ? training.getTrainingDate().toString() : "N/A",
                        training.getTrainingType().getTrainingTypeName(),
                        training.getTrainingDuration(),
                        training.getTrainees().getFirst().getUser().getUsername()
                )).toList();
        return ResponseEntity.ok(trainingInfos);
    }

    @PostMapping
    public ResponseEntity<Message> addTraining(@RequestBody @Valid AddTrainingRequest request) {
        if (!authorizationService.isAdmin() && !authorizationService.isAuthenticatedUser(request.getTrainerUsername())) {
            throw new CustomAccessDeniedException("You do not have permission to add trainings.");
        }
        TrainingType type = new TrainingType();
        type.setTrainingTypeName(request.getTrainingType());

        Training training = Training.builder()
                .trainees(List.of(traineeService.getTraineeProfileByUsername(request.getTraineeUsername())))
                .trainers(List.of(trainerService.findByUsername(request.getTrainerUsername())))
                .name(request.getTrainingName())
                .trainingType(type)
                .trainingDate(LocalDate.parse(request.getTrainingDate()).atStartOfDay())
                .trainingDuration(request.getTrainingDuration())
                .build();

        trainingService.createTraining(training);
        return ResponseEntity.ok(new Message("Training added successfully"));
    }
}
