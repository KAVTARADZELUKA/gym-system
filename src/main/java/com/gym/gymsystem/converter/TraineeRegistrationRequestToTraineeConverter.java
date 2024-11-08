package com.gym.gymsystem.converter;

import com.gym.gymsystem.dto.trainee.TraineeRegistrationRequest;
import com.gym.gymsystem.entity.Trainee;
import com.gym.gymsystem.entity.User;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TraineeRegistrationRequestToTraineeConverter implements Converter<TraineeRegistrationRequest, Trainee> {
    @Override
    public Trainee convert(TraineeRegistrationRequest request) {
        Trainee trainee = new Trainee();

        if (request.getDateOfBirth() != null && !request.getDateOfBirth().isEmpty()) {
            trainee.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()).atStartOfDay());
        }
        trainee.setAddress(request.getAddress());

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        trainee.setUser(user);

        return trainee;
    }
}
