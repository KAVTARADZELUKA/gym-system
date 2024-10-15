package com.gym.gymsystem.service;

import com.gym.gymsystem.entity.LoginAttempt;
import com.gym.gymsystem.repository.LoginAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 6;
    private static final long LOCK_TIME_DURATION = 5; // 5 minutes

    private final LoginAttemptRepository loginAttemptRepository;

    @Autowired
    public LoginAttemptService(LoginAttemptRepository loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }

    public void loginFailed(String username) {
        LoginAttempt loginAttempt = loginAttemptRepository.findByUsername(username)
                .orElse(new LoginAttempt());

        loginAttempt.setUsername(username);
        loginAttempt.setAttempts(loginAttempt.getAttempts() + 1);
        loginAttempt.setLastAttempt(LocalDateTime.now());

        if (loginAttempt.getAttempts() >= MAX_ATTEMPTS) {
            loginAttempt.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_TIME_DURATION));
        }

        loginAttemptRepository.save(loginAttempt);
    }

    public void loginSucceeded(String username) {
        LoginAttempt loginAttempt = loginAttemptRepository.findByUsername(username).orElse(null);
        if (loginAttempt != null) {
            loginAttempt.setAttempts(0);
            loginAttempt.setLockedUntil(null);
            loginAttemptRepository.save(loginAttempt);
        }
    }

    public boolean isBlocked(String username) {
        Optional<LoginAttempt> loginAttempt = loginAttemptRepository.findByUsername(username);

        if (loginAttempt.isPresent()) {
            if (loginAttempt.get().getLockedUntil() != null &&
                    loginAttempt.get().getLockedUntil().isAfter(LocalDateTime.now())) {
                return true;
            }
        }

        return false;
    }
}
