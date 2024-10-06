package com.gym.gymsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.gymsystem.dto.trainee.TraineeProfileResponse;
import com.gym.gymsystem.dto.trainee.TraineeRegistrationRequest;
import com.gym.gymsystem.dto.trainee.UpdateTraineeProfileRequest;
import com.gym.gymsystem.dto.user.UpdateStatusRequest;
import com.gym.gymsystem.entity.Trainee;
import com.gym.gymsystem.entity.User;
import com.gym.gymsystem.service.TraineeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TraineeControllerTest {
    @Mock
    private TraineeService traineeService;

    @InjectMocks
    private TraineeController traineeController;

    @Mock
    private Converter<TraineeRegistrationRequest, Trainee> traineeConverter;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(traineeController).build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    void testRegisterTrainee() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("testUser");
        request.setLastName("testPassword");

        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("testPassword");
        trainee.setUser(user);

        when(traineeConverter.convert(any(TraineeRegistrationRequest.class))).thenReturn(trainee);
        when(traineeService.createTraineeProfile(any(Trainee.class))).thenReturn(trainee);

        mockMvc.perform(post("/api/trainee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.password").value("testPassword"));

        verify(traineeService, times(1)).createTraineeProfile(any(Trainee.class));
    }

    @Test
    void testGetTraineeProfile() throws Exception {
        TraineeProfileResponse profileResponse = new TraineeProfileResponse();
        when(traineeService.getTraineeProfileAndTrainersByUsername( anyString()))
                .thenReturn(profileResponse);

        mockMvc.perform(get("/api/trainee/{findUsername}", "findUser")
                        .header("username", "testUser")
                        .header("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testUpdateTraineeProfile() throws Exception {
        UpdateTraineeProfileRequest updateRequest = new UpdateTraineeProfileRequest();
        updateRequest.setDateOfBirth("1990-01-01");
        updateRequest.setAddress("123 Test St");
        updateRequest.setFirstName("TestFirstName");
        updateRequest.setLastName("TestLastName");
        updateRequest.setIsActive(true);

        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("password123");
        user.setFirstName("TestFirstName");
        trainee.setUser(user);
        trainee.setAddress("123 Test St");

        TraineeProfileResponse expectedResponse = new TraineeProfileResponse();
        expectedResponse.setUsername("testUser");
        expectedResponse.setFirstName("TestFirstName");
        expectedResponse.setLastName("TestLastName");
        expectedResponse.setAddress("123 Test St");
        expectedResponse.setDateOfBirth("1990-01-01");
        expectedResponse.setActive(true);

        when(traineeService.getTraineeProfileByUsername( anyString())).thenReturn(trainee);
        when(traineeService.getTraineeProfileAndTrainersByUsername(anyString()))
                .thenReturn(expectedResponse);

        mockMvc.perform(put("/api/trainee/{traineeUsername}", "testUser")
                        .header("username", "testUser")
                        .header("password", "password123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    void testDeleteTraineeProfile() throws Exception {
        when(traineeService.deleteTraineeByUsername( anyString()))
                .thenReturn("Profile deleted");

        mockMvc.perform(delete("/api/trainee/{traineeUsername}", "deleteUser")
                        .header("username", "testUser")
                        .header("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"Profile deleted\"}"));
    }

    @Test
    void testUpdateTraineeStatus() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setIsActive(true);

        mockMvc.perform(patch("/api/trainee/status/{findUsername}", "findUser")
                        .header("username", "testUser")
                        .header("password", "password123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"Trainee findUser has been activated successfully\"}"));
    }
}
