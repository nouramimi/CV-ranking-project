package com.example.cvfilter.controller;

import com.example.cvfilter.dao.entity.User;
import com.example.cvfilter.dto.JwtResponse;
import com.example.cvfilter.dto.LoginRequest;
import com.example.cvfilter.dto.RegisterRequest;
import com.example.cvfilter.exception.InvalidCredentialsException;
import com.example.cvfilter.service.impl.AuthServiceInterface;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServiceInterface authService;

    public AuthController(AuthServiceInterface authService) {
        this.authService = authService;
    }
    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@RequestBody RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setRole(registerRequest.getRole());

        JwtResponse jwtResponse = authService.register(user);
        return ResponseEntity.ok(jwtResponse);
    }

    /*@PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setRole(registerRequest.getRole());

        User registeredUser = authService.register(user);
        return ResponseEntity.ok(registeredUser);
    }*/

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(jwtResponse);
    }
}
