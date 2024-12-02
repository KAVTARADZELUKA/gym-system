package com.gym.gymsystem.service;

import com.gym.gymsystem.dto.trainer.TrainerInfo;
import com.gym.gymsystem.dto.workload.WorkloadRequest;
import com.gym.gymsystem.entity.Trainee;
import com.gym.gymsystem.entity.Trainer;
import com.gym.gymsystem.entity.Training;
import com.gym.gymsystem.entity.TrainingType;
import com.gym.gymsystem.exception.InvalidCredentialsException;
import com.gym.gymsystem.exception.TraineeNotFoundException;
import com.gym.gymsystem.exception.TrainerNotFoundException;
import com.gym.gymsystem.feign.WorkloadInterface;
import com.gym.gymsystem.repository.TrainingRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TrainingService {
    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);
    private final TrainingRepository trainingRepository;
    private final TrainerService trainerService;
    private final TrainingTypeService trainingTypeService;
    private final TraineeService traineeService;
    private final UserService userService;
    private final WorkloadInterface workloadInterface;
    private static final String CIRCUIT_BREAKER_NAME = "workloadService";

    public TrainingService(TrainingRepository trainingRepository, @Lazy TrainerService trainerService, TrainingTypeService trainingTypeService, @Lazy TraineeService traineeService, UserService userService, WorkloadInterface workloadInterface) {
        this.trainingRepository = trainingRepository;
        this.trainerService = trainerService;
        this.trainingTypeService = trainingTypeService;
        this.traineeService = traineeService;
        this.userService = userService;
        this.workloadInterface = workloadInterface;
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "workloadFallback")
    public void sendWorkloadUpdate(WorkloadRequest request,String transactionId) {
        workloadInterface.updateWorkload(request,transactionId);
    }

    public HttpStatus workloadFallback(WorkloadRequest request, Throwable throwable) {
        logger.error("Fallback triggered for workload update: {}. Error: {}", request.getUsername(), throwable.getMessage());
        return HttpStatus.SERVICE_UNAVAILABLE;
    }

    @Transactional
    public Training createTraining(Training training,String transactionId) {
        logger.info("Creating training: {}, transactionId={}", training, transactionId);

        if (!training.getTrainingDate().isAfter(LocalDate.now().atStartOfDay())) {
            throw new IllegalArgumentException("Training date must be in the future.");
        }else if ((training.getTrainers().getFirst().getUser().getIsActive() == null || !training.getTrainers().getFirst().getUser().getIsActive())) {
            throw new IllegalArgumentException("Cannot add workload for an inactive user.");
        }

        TrainingType trainingType = trainingTypeService.getTrainingTypeByName(training.getTrainingType().getTrainingTypeName());
        if (trainingType == null) {
            trainingType = new TrainingType();
            trainingType.setTrainingTypeName(training.getTrainingType().getTrainingTypeName());
            trainingTypeService.save(trainingType);
        }
        training.setTrainingType(trainingType);
        Training newTraining = trainingRepository.saveAndFlush(training);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Trainer trainer : training.getTrainers()) {
            sendWorkloadUpdate(new WorkloadRequest(trainer.getUser().getUsername(),trainer.getUser().getFirstName(),
                    trainer.getUser().getLastName(), trainer.getUser().getIsActive(), training.getTrainingDate().toLocalDate().format(dateFormatter),
                    training.getTrainingDuration(), "ADD"),transactionId);
        }
        return newTraining;
    }

    public List<Training> findTrainingsByTraineeAndCriteria(String findUsername, LocalDateTime fromDate, LocalDateTime toDate, String trainerName, String trainingType) {

        logger.info("Finding trainings by trainee: username={}, fromDate={}, toDate={}, trainerName={}, trainingTypeId={}",
                findUsername, fromDate, toDate, trainerName, trainingType);
        return trainingRepository.findTrainingsByTraineeAndCriteria(findUsername, fromDate, toDate, trainerName, trainingType);
    }

    public List<Training> findTrainingsByTrainerAndCriteria(String findUsername, LocalDateTime fromDate, LocalDateTime toDate, String trainerName, String trainingType) {
        return trainingRepository.findTrainingsByTrainerAndCriteria(findUsername, fromDate, toDate, trainerName, trainingType);
    }

    public List<TrainerInfo> getTrainersNotAssignedToTrainee(String findUsername) {
        List<Trainer> availableTrainers = trainingRepository.findTrainersNotAssignedToTrainee(findUsername);

        return availableTrainers.stream()
                .map(trainer -> new TrainerInfo(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                       trainer.getSpecializations().stream()
                                .findFirst()
                                .map(TrainingType::getTrainingTypeName).get()
                ))
                .toList();
    }

    public List<Training> getTrainingByTraineesContaining(Trainee trainee) {
        return trainingRepository.findByTraineesContaining(trainee);
    }

    public List<Training> getTrainingByTrainersContaining(Trainer trainer) {
        return trainingRepository.findByTrainersContaining(trainer);
    }

    @Transactional
    public List<Training> updateTraineeTrainersByUsername(String traineeUsername, List<String> trainerUsernames, String transactionId) {
        Trainee trainee = traineeService.findByUsername(traineeUsername);
        if (trainee == null) {
            throw new TraineeNotFoundException("Trainee not found");
        }
        logger.info("Updating trainee's trainers: trainee={}, trainerIds={}", trainee, trainerUsernames);

        List<Trainer> newTrainers = trainerService.findAllByUser_UsernameIn(trainerUsernames);

        System.out.println("newTrainersn "+newTrainers);
        if (newTrainers.size() != trainerUsernames.size()) {
            throw new TrainerNotFoundException("One or more trainers not found");
        }

        List<Training> trainings = trainingRepository.findByTraineesContaining(trainee);

        System.out.println("trainings "+trainings);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Training training : trainings) {
            if (!training.getTrainingDate().isAfter(LocalDate.now().atStartOfDay())) {
                throw new IllegalArgumentException("Training date must be in the future.");
            }
            for (Trainer trainer : training.getTrainers()) {
                sendWorkloadUpdate(new WorkloadRequest(trainer.getUser().getUsername(),trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(), trainer.getUser().getIsActive(), training.getTrainingDate().toLocalDate().format(dateFormatter),
                        training.getTrainingDuration(), "DELETE"),transactionId);
            }
            training.getTrainers().clear();
            for (Trainer trainer : newTrainers) {
                sendWorkloadUpdate(new WorkloadRequest(trainer.getUser().getUsername(),trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(), trainer.getUser().getIsActive(), training.getTrainingDate().toLocalDate().format(dateFormatter),
                        training.getTrainingDuration(), "ADD"),transactionId);
            }
            training.getTrainers().addAll(newTrainers);
        }

        return trainingRepository.saveAll(trainings);
    }

    public void save(Training training) {
        trainingRepository.save(training);
    }

    public List<Training> getAllTrainings(String username, String password) {
        if (!userService.usernamePasswordMatches(username, password)) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        logger.info("Fetching all trainings");
        return trainingRepository.findAll();
    }

    @Transactional
    public List<Training> updateTraineeTrainers(String username, String password, Trainee trainee, List<Long> trainerIds) {
        if (!userService.usernamePasswordMatches(username, password)) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        logger.info("Updating trainee's trainers: trainee={}, trainerIds={}", trainee, trainerIds);
        List<Trainer> newTrainers = trainerService.findAllById(trainerIds);

        if (newTrainers.size() != trainerIds.size()) {
            throw new RuntimeException("One or more trainers not found");
        }

        List<Training> trainings = trainingRepository.findByTraineesContaining(trainee);
        for (Training training : trainings) {
            training.getTrainers().clear();
            training.getTrainers().addAll(newTrainers);
        }

        return trainingRepository.saveAll(trainings);
    }
}
