package com.gym.gymsystem.service;

import com.gym.gymsystem.entity.Trainer;
import com.gym.gymsystem.entity.TrainingType;
import com.gym.gymsystem.entity.User;
import com.gym.gymsystem.repository.TrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrainerServiceTest {
    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserService userService;

    @Mock
    private TrainingTypeService trainingTypeService;

    @InjectMocks
    private TrainerService trainerService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAll() {
        List<Trainer> trainers = Arrays.asList(new Trainer(), new Trainer());
        when(trainerRepository.findAll()).thenReturn(trainers);

        List<Trainer> result = trainerService.getAll();

        assertEquals(2, result.size());
        verify(trainerRepository).findAll();
    }

    @Test
    public void testCreateTrainerProfile() {
        Trainer trainer = new Trainer();
        trainer.setUser(new User());
        trainer.setSpecializations(List.of(new TrainingType()));
        when(userService.generateUserData(any(User.class))).thenReturn(null);
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);


        Trainer result = trainerService.createTrainerProfile(trainer);

        assertNotNull(result);
        verify(userService).generateUserData(trainer.getUser());
        verify(trainerRepository).save(trainer);
    }

    @Test
    public void testGetTrainerProfileByUsername() {
        String username = "testUser";
        Trainer trainer = new Trainer();
        when(trainerRepository.findByUser_Username(username)).thenReturn(Optional.of(trainer));

        Trainer result = trainerService.getTrainerProfileByUsername(username);

        assertNotNull(result);
        verify(trainerRepository).findByUser_Username(username);
    }

    @Test
    public void testUpdateTrainerProfile_NullId() {
        Trainer trainer = new Trainer();
        trainer.setUser(new User());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            trainerService.updateTrainerProfile(trainer);
        });
        assertEquals("Trainer or Trainer ID cannot be null", thrown.getMessage());
    }

    @Test
    public void testFindAllById() {
        List<Long> trainerIds = Arrays.asList(1L, 2L);
        List<Trainer> trainers = Arrays.asList(new Trainer(), new Trainer());
        when(trainerRepository.findAllById(trainerIds)).thenReturn(trainers);

        List<Trainer> result = trainerService.findAllById(trainerIds);

        assertEquals(2, result.size());
        verify(trainerRepository).findAllById(trainerIds);
    }
}
