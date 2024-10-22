package com.gym.gymsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.gymsystem.dto.trainee.TraineeProfileResponse;
import com.gym.gymsystem.dto.trainee.TraineeRegistrationRequest;
import com.gym.gymsystem.dto.trainee.UpdateTraineeProfileRequest;
import com.gym.gymsystem.dto.user.UpdateStatusRequest;
import com.gym.gymsystem.entity.Trainee;
import com.gym.gymsystem.entity.User;
import com.gym.gymsystem.service.AuthorizationService;
import com.gym.gymsystem.service.TraineeService;
import org.springframework.security.test.context.support.WithMockUser;
import io.micrometer.core.instrument.Counter;
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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TraineeControllerTest {
    @Mock
    private TraineeService traineeService;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private Timer timer;

    @Mock
    private Counter traineeDeletionCounter;

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
        when(meterRegistry.timer("trainee.registration.timer")).thenReturn(mock(Timer.class));
        when(meterRegistry.counter("trainee.deletion.count")).thenReturn(traineeDeletionCounter);

        Timer timer = meterRegistry.timer("trainee.registration.timer");
        doAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        }).when(timer).record(any(Supplier.class));
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

        Map<String, String> response = new HashMap<>();
        response.put("username", trainee.getUser().getUsername());
        response.put("password", "testPassword");

        when(traineeConverter.convert(any(TraineeRegistrationRequest.class))).thenReturn(trainee);
        when(traineeService.createTraineeProfile(any(Trainee.class))).thenReturn(response);

        mockMvc.perform(post("/trainee")
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
        profileResponse.setUsername("findUser");

        when(authorizationService.isAdmin()).thenReturn(false);
        when(authorizationService.isTrainer()).thenReturn(false);
        when(authorizationService.isAuthenticatedUser("findUser")).thenReturn(true);

        when(traineeService.getTraineeProfileAndTrainersByUsername(anyString()))
                .thenReturn(profileResponse);

        mockMvc.perform(get("/trainee/{findUsername}", "findUser")
                        .header("username", "testUser")
                        .header("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.username").value("findUser"));
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

        when(authorizationService.isAuthenticatedUser("testUser")).thenReturn(true);
        when(traineeService.getTraineeProfileByUsername( anyString())).thenReturn(trainee);
        when(traineeService.getTraineeProfileAndTrainersByUsername(anyString()))
                .thenReturn(expectedResponse);

        mockMvc.perform(put("/trainee/{traineeUsername}", "testUser")
                        .header("username", "testUser")
                        .header("password", "password123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    void testUpdateTraineeStatus() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setIsActive(true);

        mockMvc.perform(patch("/trainee/status/{findUsername}", "findUser")
                        .header("username", "testUser")
                        .header("password", "password123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"Trainee findUser has been activated successfully\"}"));
    }
}
