package com.gym.gymsystem.controller;

import com.gym.gymsystem.dto.user.ChangePasswordRequest;
import com.gym.gymsystem.dto.user.LoginRequest;
import com.gym.gymsystem.dto.user.Message;
import com.gym.gymsystem.service.TokenBlacklistService;
import com.gym.gymsystem.service.UserService;
import com.gym.gymsystem.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/auth")
public class UserController {
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    @Autowired
    public UserController(UserService userService, JwtTokenUtil jwtTokenUtil,
                          AuthenticationManager authenticationManager,
                          TokenBlacklistService tokenBlacklistService) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticationManager = authenticationManager;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        return ResponseEntity.ok(userService.logout(request,jwtTokenUtil,tokenBlacklistService));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(HttpServletRequest request) {
        return ResponseEntity.ok(userService.generateToken(request,authenticationManager,jwtTokenUtil));
    }


    @PutMapping("/change-password")
    public ResponseEntity<Message> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(new Message("Password updated successfully"));
    }
}