package com.gym.gymsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.gymsystem.dto.training.AddTrainingRequest;
import com.gym.gymsystem.dto.training.UpdateTraineeTrainersRequest;
import com.gym.gymsystem.entity.*;
import com.gym.gymsystem.service.TraineeService;
import com.gym.gymsystem.service.TrainerService;
import com.gym.gymsystem.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TrainingControllerTest {

    @Mock
    private TrainingService trainingService;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private TrainingController trainingController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(trainingController).build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    void testGetNotAssignedActiveTrainers() throws Exception {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("activeTrainer");
        user.setFirstName("Active");
        user.setLastName("Trainer");
        trainer.setUser(user);
        TrainingType type = new TrainingType();
        type.setTrainingTypeName("Personal");
        trainer.setSpecializations(List.of(type));


        when(trainingService.getTrainersNotAssignedToTrainee( anyString()))
                .thenReturn(List.of(trainer));

        mockMvc.perform(get("/api/training/not-assigned-active-trainers")
                        .header("username", "username")
                        .header("password", "password")
                        .param("findUsername", "traineeUser"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].username").value("activeTrainer"));
    }

    @Test
    void testUpdateTraineeTrainers() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
        request.setTrainersUsernames(Arrays.asList("trainerUser1", "trainerUser2"));

        User user = new User();
        user.setUsername("activeTrainer");
        user.setFirstName("Active");
        user.setLastName("Trainer");

        User user2 = new User();
        user2.setUsername("trainerUser2");
        user2.setFirstName("Trainer Two");
        user2.setLastName("LastName2");

        TrainingType type = new TrainingType();
        type.setTrainingTypeName("Personal");

        Trainer trainer1 = new Trainer();
        trainer1.setUser(user);
        trainer1.setSpecializations(List.of(type));

        Trainer trainer2 = new Trainer();
        trainer2.setUser(user2);
        trainer2.setSpecializations(List.of(type));

        Training training = new Training();
        training.setTrainers(Arrays.asList(trainer1, trainer2));

        when(trainingService.updateTraineeTrainersByUsername(anyString(), anyList()))
                .thenReturn(List.of(training));

        mockMvc.perform(put("/api/training/trainers")
                        .header("username", "username")
                        .header("password", "password")
                        .param("findUsername", "traineeUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].username").value("activeTrainer"))
                .andExpect(jsonPath("$[1].username").value("trainerUser2"));
    }

    @Test
    void testGetTraineeTrainings() throws Exception {
        User user = new User();
        user.setUsername("activeTrainer");
        user.setFirstName("Active");
        user.setLastName("Trainer");
        Trainer trainer1 = new Trainer();
        trainer1.setUser(user);

        Training training = new Training();
        training.setName("Training Session");
        training.setTrainingDate(LocalDate.now().atStartOfDay());
        training.setTrainingDuration(60L);
        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName("Yoga");
        training.setTrainingType(trainingType);
        training.setTrainers(List.of(trainer1));

        when(trainingService.findTrainingsByTraineeAndCriteria(anyString(),
                any(), any(), anyString(), anyString()))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/api/training/trainee")
                        .header("username", "username")
                        .header("password", "password")
                        .param("traineeUsername", "traineeUser")
                        .param("periodFrom", LocalDate.now().minusDays(30).toString())
                        .param("periodTo", LocalDate.now().toString())
                        .param("trainerName", "Active Trainer")
                        .param("trainingType", "Yoga")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].trainingName").value("Training Session"))
                .andExpect(jsonPath("$[0].trainingType").value("Yoga"));
    }

    @Test
    void testGetTrainerTrainings() throws Exception {
        Training training = new Training();
        training.setName("Training Session");
        training.setTrainingDate(LocalDate.now().atStartOfDay());
        training.setTrainingDuration(60L);
        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName("Yoga");
        training.setTrainingType(trainingType);

        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("traineeUser");
        user.setFirstName("Active");
        user.setLastName("Trainer");
        trainee.setUser(user);
        training.setTrainees(List.of(trainee));


        when(trainingService.findTrainingsByTrainerAndCriteria(anyString(),
                any(), any(), anyString(), anyString()))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/api/training/trainer")
                        .header("username", "username")
                        .header("password", "password")
                        .param("trainerUsername", "trainerUser")
                        .param("periodFrom", LocalDate.now().minusDays(30).toString())
                        .param("periodTo", LocalDate.now().toString())
                        .param("traineeName", "Active Trainee")
                        .param("trainingType", "Yoga")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].trainingName").value("Training Session"))
                .andExpect(jsonPath("$[0].trainingType").value("Yoga"))
                .andExpect(jsonPath("$[0].traineeName").value("traineeUser"));
    }

    @Test
    void testAddTraining() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("traineeUser");
        request.setTrainerUsername("trainerUser");
        request.setTrainingName("New Training Session");
        request.setTrainingType("Yoga");
        request.setTrainingDate(LocalDate.now().toString());
        request.setTrainingDuration(60L);

        User user = new User();
        user.setUsername("trainerUser");
        user.setFirstName("Active");
        user.setLastName("Trainer");
        Trainer trainer1 = new Trainer();
        trainer1.setUser(user);
        Trainee trainee = new Trainee();
        User user1 = new User();
        user1.setUsername("traineeUser");
        user1.setPassword("testPassword");
        trainee.setUser(user1);

        when(traineeService.getTraineeProfileByUsername( anyString()))
                .thenReturn(trainee);
        when(trainerService.findByUsername(anyString())).thenReturn(trainer1);
        when(trainingService.createTraining( any(Training.class)))
                .thenReturn(null);

        mockMvc.perform(post("/api/training")
                        .param("username", "username")
                        .param("password", "password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"Training added successfully\"}"));
    }
}