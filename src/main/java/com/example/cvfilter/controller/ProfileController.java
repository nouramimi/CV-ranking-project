package com.example.cvfilter.controller;

import com.example.cvfilter.dao.entity.User;
import com.example.cvfilter.dao.repository.UserRepository;
import com.example.cvfilter.dto.*;
import com.example.cvfilter.service.ProfileService;
import com.example.cvfilter.exception.ProfileException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ProfileDto> getProfile() {
        try {
            String userEmail = getCurrentUserEmail();
            ProfileDto profile = profileService.getUserProfileByEmail(userEmail);
            return ResponseEntity.ok(profile);
        } catch (ProfileException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileDto updateProfileDto) {
        try {
            Long userId = getCurrentUserId();
            ProfileDto updatedProfile = profileService.updateProfile(userId, updateProfileDto);
            return ResponseEntity.ok(updatedProfile);
        } catch (ProfileException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto) {
        try {
            Long userId = getCurrentUserId();
            profileService.changePassword(userId, changePasswordDto);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);
        } catch (ProfileException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ProfileException("User not authenticated");
        }
        return authentication.getName();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ProfileException("User not authenticated");
        }

        // Le principal contient l'email de l'utilisateur
        String userEmail = authentication.getName();

        // Récupérer l'utilisateur par son email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ProfileException("User not found"));

        return user.getId();
    }
}