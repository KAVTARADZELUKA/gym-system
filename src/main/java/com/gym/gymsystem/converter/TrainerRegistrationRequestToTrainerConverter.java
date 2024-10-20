package com.gym.gymsystem.converter;

import com.gym.gymsystem.dto.trainer.TrainerRegistrationRequest;
import com.gym.gymsystem.entity.Trainer;
import com.gym.gymsystem.entity.TrainingType;
import com.gym.gymsystem.entity.User;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TrainerRegistrationRequestToTrainerConverter implements Converter<TrainerRegistrationRequest, Trainer> {
    @Override
    public Trainer convert(TrainerRegistrationRequest request) {
        Trainer trainer = new Trainer();

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        trainer.setUser(user);

        List<TrainingType> specializations = new ArrayList<>();
        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName(request.getSpecialization());
        specializations.add(trainingType);

        trainer.setSpecializations(specializations);

        return trainer;
    }
}
