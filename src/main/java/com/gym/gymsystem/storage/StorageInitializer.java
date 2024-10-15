package com.gym.gymsystem.storage;

import com.gym.gymsystem.entity.*;
import com.gym.gymsystem.repository.TrainingRepository;
import com.gym.gymsystem.service.TraineeService;
import com.gym.gymsystem.service.TrainerService;
import com.gym.gymsystem.service.TrainingTypeService;
import com.gym.gymsystem.service.UserService;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class StorageInitializer implements SmartInitializingSingleton {
    private static final String TRAINEE_DATA_FILE = "traineeDatafile.csv";
    private static final String TRAINER_DATA_FILE = "trainerDatafile.csv";
    private static final String TRAINING_DATA_FILE = "trainingDatafile.csv";

    private final UserService userService;
    private final TrainerService trainerService;
    private final TraineeService traineeService;
    private final TrainingTypeService trainingTypeService;
    private final TrainingRepository trainingRepository;

    public StorageInitializer(UserService userService, TrainerService trainerService, TraineeService traineeService, TrainingTypeService trainingTypeService, TrainingRepository trainingRepository) {
        System.out.println("Initializing StorageInitializer");
        this.userService = userService;
        this.trainerService = trainerService;
        this.traineeService = traineeService;
        this.trainingTypeService = trainingTypeService;
        this.trainingRepository = trainingRepository;
    }


    public void loadData(TrainingRepository trainingRepository) {
        loadTraineeData();
        loadTrainerData();
        loadTrainingData(trainingRepository);
    }

    private void loadTraineeData() {
        Resource resource = new ClassPathResource(TRAINEE_DATA_FILE);
        String line = null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                Trainee trainee = new Trainee();
                User user = new User();
                user.setFirstName(parts[1]);
                user.setLastName(parts[2]);

                trainee.setUser(user);
                trainee.setAddress(parts[3]);
                trainee.setDateOfBirth(LocalDate.parse(parts[4]).atStartOfDay());
                traineeService.createTraineeProfile(trainee);
            }
        } catch (IOException e) {
            System.err.println("Error reading trainee data file: " + TRAINEE_DATA_FILE);
            e.printStackTrace();
        }
    }

    private void loadTrainerData() {
        Resource resource = new ClassPathResource(TRAINER_DATA_FILE);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                Trainer trainer = new Trainer();
                User user = new User();
                TrainingType trainingType;
                List<TrainingType> trainingTypes = new ArrayList<>();

                user.setFirstName(parts[1]);
                user.setLastName(parts[2]);

                trainingType = trainingTypeService.getTrainingTypeByNameForInitializer(parts[3]);

                trainer.setUser(user);
                trainingTypes.add(trainingType);
                trainer.setSpecializations(trainingTypes);

                trainerService.createTrainerProfile(trainer);
            }
        } catch (IOException e) {
            System.err.println("Error reading trainer data file: " + TRAINER_DATA_FILE);
            e.printStackTrace();
        }
    }


    private void loadTrainingData(TrainingRepository trainingRepository) {
        Resource resource = new ClassPathResource(TRAINING_DATA_FILE);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                Training training = new Training();
                Trainer trainer;
                Trainee trainee;
                TrainingType trainingType;

                List<Trainee> traineeList = new ArrayList<>();
                List<Trainer> trainerList = new ArrayList<>();

                trainee = traineeService.findById(Long.parseLong(parts[0]));
                traineeList.add(trainee);

                trainer = trainerService.findById(Long.parseLong(parts[1])).get();
                trainerList.add(trainer);

                trainingType = trainingTypeService.getTrainingTypeByNameForInitializer(parts[3]);

                training.setTrainees(traineeList);
                training.setTrainers(trainerList);
                training.setName(parts[2]);
                training.setTrainingType(trainingType);
                training.setTrainingDate(LocalDate.parse(parts[4]).atStartOfDay());
                training.setTrainingDuration(Long.parseLong(parts[5]));
                trainingRepository.save(training);
            }
        } catch (IOException e) {
            System.err.println("Error reading training data file: " + TRAINING_DATA_FILE);
            e.printStackTrace();
        }
    }

    @Override
    public void afterSingletonsInstantiated() {
        System.out.println("StorageInitializer afterSingletonsInstantiated called");
        loadData(trainingRepository);
        System.out.println("StorageInitializer loadData method completed");
    }
}
