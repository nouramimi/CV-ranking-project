package com.example.cvfilter.controller;

import com.example.cvfilter.model.*;
import com.example.cvfilter.repository.UserRepository;
import com.example.cvfilter.security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.cvfilter.dto.JwtResponse;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final JwtUtils jwtUtils;


    public AuthController(UserRepository userRepo, JwtUtils jwtUtils) {
        this.userRepo = userRepo;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        return userRepo.save(user);
    }

    /*@PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        Optional<User> userOpt = userRepo.findByUsername(loginData.get("username"));

        if (userOpt.isPresent() &&
                new BCryptPasswordEncoder().matches(loginData.get("password"), userOpt.get().getPassword())) {
            String token = jwtUtils.generateToken(userOpt.get().getUsername(), userOpt.get().getRole().name());
            return ResponseEntity.ok(new JwtResponse(token));
        } else {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "Invalid credentials"));
        }
    }*/

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "Username and password are required"));
        }

        Optional<User> userOpt = userRepo.findByUsername(username);

        if (userOpt.isPresent() &&
                new BCryptPasswordEncoder().matches(password, userOpt.get().getPassword())) {
            String token = jwtUtils.generateToken(userOpt.get().getUsername(), userOpt.get().getRole().name());
            return ResponseEntity.ok(new JwtResponse(token));
        } else {
            return ResponseEntity.status(401)
                    .body(Collections.singletonMap("error", "Invalid credentials"));
        }
    }
}
