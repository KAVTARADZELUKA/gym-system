package com.gym.gymsystem.controller;

import com.gym.gymsystem.dto.user.ChangePasswordRequest;
import com.gym.gymsystem.dto.user.LoginRequest;
import com.gym.gymsystem.dto.user.Message;
import com.gym.gymsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/auth")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<Message> login(@RequestBody @Validated LoginRequest request) {
        if (userService.usernamePasswordMatches(request.getUsername(), request.getPassword())) {
            return ResponseEntity.ok(new Message("login successfully"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message("Invalid credentials"));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Message> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(new Message("Password updated successfully"));
    }
}