package com.gym.gymsystem.service;

import com.gym.gymsystem.dto.trainee.TraineeInfo;
import com.gym.gymsystem.dto.trainer.TrainerProfileResponse;
import com.gym.gymsystem.dto.trainer.UpdateTrainerProfileRequest;
import com.gym.gymsystem.entity.Trainer;
import com.gym.gymsystem.entity.Training;
import com.gym.gymsystem.entity.TrainingType;
import com.gym.gymsystem.exception.TrainerNotFoundException;
import com.gym.gymsystem.repository.TrainerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TrainerService {
    private static final Logger logger = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerRepository trainerRepository;
    private final UserService userService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public TrainerService(TrainerRepository trainerRepository, UserService userService, TrainingService trainingService, TrainingTypeService trainingTypeService, PasswordEncoder passwordEncoder) {
        this.trainerRepository = trainerRepository;
        this.userService = userService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public List<Trainer> getAll() {
        return trainerRepository.findAll();
    }

    public Trainer updateTrainerProfile(UpdateTrainerProfileRequest request, Trainer trainer) {
        if (request.getFirstName() != null) {
            trainer.getUser().setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            trainer.getUser().setLastName(request.getLastName());
        }
        trainer.getUser().setIsActive(request.isActive());
        return trainer;
    }

    public Map<String, String> createTrainerProfile(Trainer trainer) {
        logger.info("Creating trainer profile: {}", trainer);
        userService.generateUserData(trainer.getUser());
        String password = userService.generateRandomPassword();
        trainer.getUser().setPassword(passwordEncoder.encode(password));

        List<TrainingType> trainingTypes = new ArrayList<>();
        TrainingType trainingType = null;
        for (TrainingType type : trainer.getSpecializations()) {
            trainingType = trainingTypeService.getTrainingTypeByName(type.getTrainingTypeName());
        }
        trainingTypes.add(trainingType);
        trainer.setSpecializations(trainingTypes);
        trainerRepository.save(trainer);

        Map<String, String> response = new HashMap<>();
        response.put("username",trainer.getUser().getUsername());
        response.put("password", password);

        return response;
    }


    public TrainerProfileResponse updateProfile(String trainerUsername, UpdateTrainerProfileRequest request) {
        Trainer trainer = getTrainerProfileByUsername(trainerUsername);
        TrainingType trainingType = trainingTypeService.getTrainingTypeByName(request.getSpecialization());
        trainer.setSpecializations(List.of(trainingType));
        updateTrainerProfile(updateTrainerProfile(request, trainer));
        TrainerProfileResponse response = getTrainerProfileAndTraineesByUsername(trainerUsername);
        return response;
    }

    public Trainer getTrainerProfileByUsername(String findUsername) {

        return trainerRepository.findByUser_Username(findUsername).orElseThrow(() -> new TrainerNotFoundException("Trainer not found"));
    }

    public TrainerProfileResponse getTrainerProfileAndTraineesByUsername(String findUsername) {
        Trainer trainer = getTrainerProfileByUsername(findUsername);
        if (trainer == null) {
            throw new TrainerNotFoundException("Trainer not found");
        }
        List<Training> trainings = trainingService.getTrainingByTrainersContaining(trainer);

        List<TraineeInfo> traineeInfoList = trainings.stream()
                .flatMap(training -> training.getTrainees().stream())
                .map(trainee -> new TraineeInfo(
                        trainee.getUser().getUsername(),
                        trainee.getUser().getFirstName(),
                        trainee.getUser().getLastName()
                ))
                .distinct()
                .toList();

        return new TrainerProfileResponse(
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecializations().get(0).getTrainingTypeName(),
                trainer.getUser().getIsActive(),
                traineeInfoList
        );
    }

    @Transactional
    public Trainer updateTrainerProfile(Trainer trainer) {
        logger.info("Updating trainer profile: {}", trainer);

        if (trainer.getId() == null) {
            throw new IllegalArgumentException("Trainer or Trainer ID cannot be null");
        }
        userService.updateUser(trainer.getUser());

        if (trainer.getSpecializations() != null) {
            List<TrainingType> managedSpecializations = trainer.getSpecializations().stream()
                    .map(specialization -> trainingTypeService.getTrainingType(specialization.getId())
                            .orElseThrow(() -> new RuntimeException("TrainingType not found with ID: " + specialization.getId())))
                    .toList();
            trainer.setSpecializations(managedSpecializations);
        }

        return trainerRepository.save(trainer);
    }

    public List<Trainer> findAllById(List<Long> trainerIds) {
        logger.info("Finding trainers by IDs: {}", trainerIds);
        return trainerRepository.findAllById(trainerIds);
    }

    public Optional<Trainer> findById(long l) {
        return trainerRepository.findById(l);
    }

    public Trainer findByUsername(String username) {
        return trainerRepository.findByUser_Username(username).orElseThrow(() -> new TrainerNotFoundException("Trainer not found"));
    }

    public List<Trainer> findAllByUser_UsernameIn(List<String> trainerUsernames) {
        return trainerRepository.findAllByUser_UsernameIn(trainerUsernames);
    }

    public void updateTraineeStatus(String findUsername, boolean isActive) {
        Trainer trainer = getTrainerProfileByUsername(findUsername);
        if (trainer == null) {
            throw new TrainerNotFoundException("trainer not found");
        }
        trainer.getUser().setIsActive(isActive);
        trainerRepository.save(trainer);
    }
}
