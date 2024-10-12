package com.gym.gymsystem.service;

import com.gym.gymsystem.dto.trainee.TraineeProfileResponse;
import com.gym.gymsystem.dto.trainee.UpdateTraineeProfileRequest;
import com.gym.gymsystem.dto.trainer.TrainerInfo;
import com.gym.gymsystem.entity.Trainee;
import com.gym.gymsystem.entity.Training;
import com.gym.gymsystem.exception.TraineeNotFoundException;
import com.gym.gymsystem.repository.TraineeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TraineeService {
    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);

    private final TraineeRepository traineeRepository;
    private final TrainingService trainingService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;


    public TraineeService(TraineeRepository traineeRepository, TrainingService trainingService, UserService userService, PasswordEncoder passwordEncoder) {
        this.traineeRepository = traineeRepository;
        this.trainingService = trainingService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public Trainee fromUpdateRequest(UpdateTraineeProfileRequest updateRequest, Trainee trainee) {
        if (updateRequest.getDateOfBirth() != null) {
            trainee.setDateOfBirth(LocalDate.parse(updateRequest.getDateOfBirth()).atStartOfDay());
        }
        if (updateRequest.getAddress() != null) {
            trainee.setAddress(updateRequest.getAddress());
        }
        if (updateRequest.getFirstName() != null) {
            trainee.getUser().setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            trainee.getUser().setLastName(updateRequest.getLastName());
        }
        trainee.getUser().setIsActive(updateRequest.getIsActive());

        return trainee;
    }

    public Map<String, String> createTraineeProfile(Trainee trainee) {
        logger.info("Creating trainee profile: {}", trainee);
        userService.generateUserData(trainee.getUser());
        String password = userService.generateRandomPassword();
        trainee.getUser().setPassword(passwordEncoder.encode(password));
        traineeRepository.save(trainee);

        Map<String, String> response = new HashMap<>();
        response.put("username",trainee.getUser().getUsername());
        response.put("password", password);

        return response;
    }

    public List<Trainee> getAllTrainees() {
        logger.info("Fetching all trainees");
        return traineeRepository.findAll();
    }

    public Trainee getTraineeProfileByUsername( String findUsername) {
        return traineeRepository.findByUser_Username(findUsername).orElseThrow(() -> new TraineeNotFoundException("Trainee not found"));
    }

    public TraineeProfileResponse getTraineeProfileAndTrainersByUsername(String findUsername) {
        Trainee trainee = getTraineeProfileByUsername(findUsername);

        List<Training> trainings = trainingService.getTrainingByTraineesContaining(trainee);

        List<TrainerInfo> trainerInfoList = trainings.stream()
                .flatMap(training -> training.getTrainers().stream()
                        .map(trainer -> new TrainerInfo(trainer.getUser().getUsername(),
                                trainer.getUser().getFirstName(),
                                trainer.getUser().getLastName(),
                                trainer.getSpecializations().getFirst().getTrainingTypeName()))
                )
                .toList();

        return new TraineeProfileResponse(
                trainee.getUser().getUsername(),
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                trainee.getDateOfBirth() != null ? trainee.getDateOfBirth().toString() : "N/A",
                trainee.getAddress(),
                trainee.getUser().getIsActive(),
                trainerInfoList
        );
    }

    public void updateTraineeStatus( String findUsername, boolean isActive) {
        Trainee trainee = getTraineeProfileByUsername(findUsername);
        if (trainee == null) {
            throw new TraineeNotFoundException("Trainee not found");
        }
        trainee.getUser().setIsActive(isActive);
        traineeRepository.save(trainee);
    }

    public Trainee updateTraineeProfile( Trainee trainee) {
        logger.info("Updating trainee profile: {}", trainee);
        if (trainee.getId() == null) {
            logger.error("Trainee or Trainee ID cannot be null");
            throw new IllegalArgumentException("Trainee or Trainee ID cannot be null");
        }
        userService.updateUser(trainee.getUser());

        return traineeRepository.save(trainee);
    }

    @Transactional
    public String deleteTraineeByUsername( String findUsername) {
        Trainee trainee = traineeRepository.findByUser_Username(findUsername).orElseThrow(() -> new TraineeNotFoundException("Trainee not found"));

        List<Training> trainings = trainingService.getTrainingByTraineesContaining(trainee);
        for (Training training : trainings) {
            training.getTrainees().remove(trainee);
            trainingService.save(training);
        }

        traineeRepository.delete(trainee);
        return "The Trainee has been deleted";
    }

    public Optional<Trainee> findById(Long traineeId) {
        return traineeRepository.findById(traineeId);
    }

    public Trainee findByUsername(String traineeUsername) {
        return traineeRepository.findByUser_Username(traineeUsername).orElseThrow(() -> new TraineeNotFoundException("Trainee not found"));
    }
}
