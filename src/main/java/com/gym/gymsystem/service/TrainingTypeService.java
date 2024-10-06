package com.gym.gymsystem.service;

import com.gym.gymsystem.entity.TrainingType;
import com.gym.gymsystem.exception.TrainingTypeNotFoundException;
import com.gym.gymsystem.repository.TrainingTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingTypeService {
    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeService(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    public Optional<TrainingType> getTrainingType(Long id) {
        return trainingTypeRepository.findById(id);
    }

    public TrainingType getTrainingTypeByName(String name) {
        return trainingTypeRepository.findByTrainingTypeName(name).orElseThrow(()->new TrainingTypeNotFoundException("Training Type Not Found"));
    }

    public TrainingType getTrainingTypeByNameForInitializer(String name) {
        return trainingTypeRepository.findByTrainingTypeName(name)
                .orElseGet(() -> {
                    TrainingType newTrainingType = new TrainingType();
                    newTrainingType.setTrainingTypeName(name);
                    return trainingTypeRepository.save(newTrainingType);
                });
    }

    public TrainingType save(TrainingType trainingType) {
        return trainingTypeRepository.save(trainingType);
    }

    public List<TrainingType> getAllTrainingTypes() {
        return trainingTypeRepository.findAll();
    }
}
