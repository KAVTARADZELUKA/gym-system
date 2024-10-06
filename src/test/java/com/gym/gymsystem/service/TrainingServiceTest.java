package com.gym.gymsystem.service;

import com.gym.gymsystem.entity.*;
import com.gym.gymsystem.repository.TrainingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrainingServiceTest {
    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private UserService userService;
    @Mock
    private TrainingTypeService trainingTypeService;

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private TrainingService trainingService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateTraining() {
        String username = "username";
        String password = "password";
        Training training = new Training();
        training.setName("Box");
        training.setTrainingType(new TrainingType());
        when(trainingRepository.saveAndFlush(any(Training.class))).thenReturn(training);
        when(trainingTypeService.getTrainingTypeByName(training.getTrainingType().getTrainingTypeName())).thenReturn(null);
        when(userService.usernamePasswordMatches(username,password)).thenReturn(true);

        Training result = trainingService.createTraining(training);

        assertNotNull(result);
        verify(trainingRepository).saveAndFlush(training);
    }

    @Test
    public void testGetAllTrainings() {
        String username = "username";
        String password = "password";
        List<Training> trainings = Arrays.asList(new Training(), new Training());
        when(trainingRepository.findAll()).thenReturn(trainings);
        when(userService.usernamePasswordMatches(username,password)).thenReturn(true);

        Collection<Training> result = trainingService.getAllTrainings(username,password);

        assertEquals(2, result.size());
        verify(trainingRepository).findAll();
    }

    @Test
    public void testFindTrainingsByTraineeAndCriteria() {
        String username = "testUser";
        LocalDateTime fromDate = LocalDate.now().atStartOfDay();
        LocalDateTime toDate = LocalDate.now().atStartOfDay();
        String trainerName = "testTrainer";
        String trainingType = "personal";

        List<Training> trainings = Arrays.asList(new Training(), new Training());
        when(trainingRepository.findTrainingsByTraineeAndCriteria(username, fromDate, toDate, trainerName, trainingType))
                .thenReturn(trainings);
List<Training> result = trainingService.findTrainingsByTraineeAndCriteria(username, fromDate, toDate, trainerName, trainingType);

        assertEquals(2, result.size());
        verify(trainingRepository).findTrainingsByTraineeAndCriteria(username, fromDate, toDate, trainerName, trainingType);
    }

    @Test
    public void testFindTrainingsByTrainerAndCriteria() {
        String username = "testUser";
        LocalDateTime fromDate = LocalDate.now().atStartOfDay();
        LocalDateTime toDate = LocalDate.now().atStartOfDay();
        String trainerName = "testTrainer";
        String trainingType ="personal";

        List<Training> trainings = Arrays.asList(new Training(), new Training());
        when(trainingRepository.findTrainingsByTrainerAndCriteria(username, fromDate, toDate, trainerName, trainingType))
                .thenReturn(trainings);

        List<Training> result = trainingService.findTrainingsByTrainerAndCriteria(username, fromDate, toDate, trainerName, trainingType);

        assertEquals(2, result.size());
        verify(trainingRepository).findTrainingsByTrainerAndCriteria(username, fromDate, toDate, trainerName, trainingType);
    }

    @Test
    public void testGetTrainersNotAssignedToTrainee() {
        String username = "testUser";
        List<Trainer> trainers = Arrays.asList(new Trainer(), new Trainer());
        when(trainingRepository.findTrainersNotAssignedToTrainee(username)).thenReturn(trainers);

        List<Trainer> result = trainingService.getTrainersNotAssignedToTrainee(username);

        assertEquals(2, result.size());
        verify(trainingRepository).findTrainersNotAssignedToTrainee(username);
    }

    @Test
    public void testUpdateTraineeTrainers_UserNotFound() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User());
        trainee.getUser().setUsername("testUser");
        trainee.getUser().setPassword("password");

        List<Long> trainerIds = Arrays.asList(1L, 2L);

        when(userService.usernamePasswordMatches(trainee.getUser().getUsername(), trainee.getUser().getPassword())).thenReturn(false);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            trainingService.updateTraineeTrainers(trainee.getUser().getUsername(), trainee.getUser().getPassword(),trainee, trainerIds);
        });
        assertEquals("Invalid credentials", thrown.getMessage());
    }

    @Test
    public void testUpdateTraineeTrainers_TrainerNotFound() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User());
        trainee.getUser().setUsername("testUser");
        trainee.getUser().setPassword("password");

        List<Long> trainerIds = Arrays.asList(1L, 2L);

        when(userService.usernamePasswordMatches(trainee.getUser().getUsername(), trainee.getUser().getPassword())).thenReturn(true);
        when(trainerService.findAllById(trainerIds)).thenReturn(new ArrayList<>());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            trainingService.updateTraineeTrainers(trainee.getUser().getUsername(), trainee.getUser().getPassword(),trainee, trainerIds);
        });
        assertEquals("One or more trainers not found", thrown.getMessage());
    }
}
