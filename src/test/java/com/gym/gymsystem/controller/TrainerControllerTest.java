package com.gym.gymsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.gymsystem.dto.trainer.TrainerProfileResponse;
import com.gym.gymsystem.dto.trainer.TrainerRegistrationRequest;
import com.gym.gymsystem.dto.trainer.UpdateTrainerProfileRequest;
import com.gym.gymsystem.dto.user.UpdateStatusRequest;
import com.gym.gymsystem.entity.Trainer;
import com.gym.gymsystem.entity.TrainingType;
import com.gym.gymsystem.entity.User;
import com.gym.gymsystem.service.AuthorizationService;
import com.gym.gymsystem.service.TrainerService;
import com.gym.gymsystem.service.TrainingTypeService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TrainerControllerTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private TrainingTypeService trainingTypeService;
    @Mock
    private AuthorizationService authorizationService;


    @InjectMocks
    private TrainerController trainerController;

    @Mock
    private Converter<TrainerRegistrationRequest, Trainer> trainerConverter;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(trainerController).build();
        this.objectMapper = new ObjectMapper();
        when(meterRegistry.timer("trainer.registration.timer")).thenReturn(mock(Timer.class));

        Timer timer = meterRegistry.timer("trainer.registration.timer");
        doAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        }).when(timer).record(any(Supplier.class));
    }

    @Test
    void testRegisterTrainer() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("trainerUser");
        request.setLastName("trainerUser");
        request.setSpecialization("trainerSpecialization");

        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("trainerUser");
        user.setPassword("password123");
        trainer.setUser(user);

        Map<String, String> response = new HashMap<>();
        response.put("username","trainerUser");
        response.put("password", "password123");

        when(trainerConverter.convert(any(TrainerRegistrationRequest.class))).thenReturn(trainer);
        when(trainerService.createTrainerProfile(any(Trainer.class))).thenReturn(response);


        mockMvc.perform(post("/api/trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("trainerUser"))
                .andExpect(jsonPath("$.password").value("password123"));
    }

    @Test
    void testGetTrainerProfile() throws Exception {
        TrainerProfileResponse response = new TrainerProfileResponse();
        response.setFirstName("trainerUser");

        when(authorizationService.isAdmin()).thenReturn(true);
        when(trainerService.getTrainerProfileAndTraineesByUsername(anyString()))
                .thenReturn(response);

        mockMvc.perform(get("/api/trainer/{findUsername}", "trainerUser")
                        .header("username", "trainerUser")
                        .header("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("trainerUser"));
    }

    @Test
    void testUpdateTrainerProfile() throws Exception {
        UpdateTrainerProfileRequest updateRequest = new UpdateTrainerProfileRequest();
        updateRequest.setFirstName("John");
        updateRequest.setLastName("Doe");
        updateRequest.setSpecialization("Yoga");

        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("trainerUser");
        trainer.setUser(user);

        when(authorizationService.isAdmin()).thenReturn(true);
        when(trainerService.getTrainerProfileByUsername(anyString())).thenReturn(trainer);
        when(trainingTypeService.getTrainingTypeByName(anyString())).thenReturn(new TrainingType());
        when(trainerService.getTrainerProfileAndTraineesByUsername(anyString()))
                .thenReturn(new TrainerProfileResponse());

        mockMvc.perform(put("/api/trainer/{trainerUsername}", "trainerUser")
                        .header("username", "adminUser")
                        .header("password", "adminPass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testUpdateTrainerStatus() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setIsActive(true);
        String findUsername = "trainerUser";

        mockMvc.perform(patch("/api/trainer/status/{findUsername}", findUsername)
                        .header("username", "adminUser")
                        .header("password", "adminPass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"Trainer " + findUsername + " has been activated successfully\"}"));
    }
}