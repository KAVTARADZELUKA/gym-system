package com.gym.gymsystem.service;

import com.gym.gymsystem.entity.Trainee;
import com.gym.gymsystem.entity.User;
import com.gym.gymsystem.repository.TraineeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeServiceTest {
    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private UserService userService;
    @Mock
    private TrainingService trainingService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TraineeService traineeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateTraineeProfile() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User());
        String encodedPassword = "encodedPassword";

        when(userService.generateUserData(any(User.class))).thenReturn(null);
        when(userService.generateRandomPassword()).thenReturn("randomPassword");
        when(passwordEncoder.encode("randomPassword")).thenReturn(encodedPassword);
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        Map<String, String> result = traineeService.createTraineeProfile(trainee);

        assertNotNull(result);
        assertEquals("encodedPassword", trainee.getUser().getPassword());

        verify(userService).generateUserData(trainee.getUser());
        verify(userService).generateRandomPassword();
        verify(passwordEncoder).encode("randomPassword");
        verify(traineeRepository).save(trainee);
    }

    @Test
    public void testGetAllTrainees() {
        List<Trainee> trainees = Arrays.asList(new Trainee(), new Trainee());
        when(traineeRepository.findAll()).thenReturn(trainees);

        List<Trainee> result = traineeService.getAllTrainees();

        assertEquals(2, result.size());
        verify(traineeRepository).findAll();
    }

    @Test
    public void testGetTraineeProfileByUsername() {
        String username = "testUser";
        Trainee trainee = new Trainee();
        when(traineeRepository.findByUser_Username(username)).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.getTraineeProfileByUsername(username);

        assertNotNull(result);
        verify(traineeRepository).findByUser_Username(username);
    }


    @Test
    public void testUpdateTraineeProfile_NullId() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            traineeService.updateTraineeProfile(trainee);
        });
        assertEquals("Trainee or Trainee ID cannot be null", thrown.getMessage());
    }

    @Test
    public void testDeleteTraineeByUsername() {
        String username = "testUser";
        Trainee trainee = new Trainee();
        when(traineeRepository.findByUser_Username(username)).thenReturn(Optional.of(trainee));
        when(trainingService.getTrainingByTraineesContaining(trainee)).thenReturn(new ArrayList<>());
        doNothing().when(traineeRepository).delete(trainee);

        String result = traineeService.deleteTraineeByUsername(username);

        assertEquals("The Trainee has been deleted", result);
        verify(traineeRepository).delete(trainee);
    }

    @Test
    public void testFindById() {
        Long traineeId = 1L;
        Trainee trainee = new Trainee();
        Optional<Trainee> optionalTrainee = Optional.of(trainee);
        when(traineeRepository.findById(traineeId)).thenReturn(optionalTrainee);

        traineeService.findById(traineeId);

        verify(traineeRepository).findById(traineeId);
    }
}
