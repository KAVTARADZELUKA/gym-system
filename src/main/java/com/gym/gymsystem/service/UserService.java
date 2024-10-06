package com.gym.gymsystem.service;

import com.gym.gymsystem.entity.User;
import com.gym.gymsystem.exception.BlankRegistrationRequestException;
import com.gym.gymsystem.exception.InvalidOldPasswordException;
import com.gym.gymsystem.exception.UserNotFoundException;
import com.gym.gymsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    protected UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateRandomPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    public User generateUserData(User user) {
        if (user.getFirstName() == null || user.getFirstName().isEmpty()
                || user.getLastName() == null || user.getLastName().isEmpty()) {
            throw new BlankRegistrationRequestException("firstName and LastName cannot be blank");
        }
        String password = generateRandomPassword();
        user.setPassword(password);
        user.setUsername(generateUsername(user.getFirstName(), user.getLastName()));
        user.setIsActive(true);
        return user;
    }

    public User saveUser(User user) {
        generateUserData(user);
        userRepository.save(user);

        return user;
    }

    public Optional<User> getByUsername(String username) {
        return userRepository.getUserByUsername(username);
    }

    public String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName.toLowerCase() + "." + lastName.toLowerCase();
        List<String> existingUsernames = userRepository.findUsernamesByUsernameStartingWith(baseUsername);

        if (existingUsernames.isEmpty()) {
            return baseUsername;
        }

        Set<Integer> suffixes = new HashSet<>();
        for (String username : existingUsernames) {
            if (username.equals(baseUsername)) {
                suffixes.add(0);
            } else {
                String[] parts = username.split("\\.");
                if (parts.length > 2) {
                    try {
                        int suffix = Integer.parseInt(parts[2]);
                        suffixes.add(suffix);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        int nextSuffix = 1;
        while (suffixes.contains(nextSuffix)) {
            nextSuffix++;
        }
        return baseUsername + "." + nextSuffix;
    }

    @Transactional
    public boolean usernamePasswordMatches(String username, String password) {
        User user = userRepository.getUserByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        if (user == null) {
            return false;
        }
        return password.equals(user.getPassword());
    }

    @Transactional
    public Boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!oldPassword.equals(user.getPassword())) {
            throw new InvalidOldPasswordException("Old password is incorrect");
        }
        user.setPassword(newPassword);
        userRepository.save(user);

        return true;
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
