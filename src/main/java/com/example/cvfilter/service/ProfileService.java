package com.example.cvfilter.service;

import com.example.cvfilter.dao.entity.*;
import com.example.cvfilter.dao.repository.UserRepository;
import com.example.cvfilter.dto.*;
import com.example.cvfilter.exception.ProfileException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileException("User not found"));

        return buildProfileDto(user);
    }

    public ProfileDto getUserProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ProfileException("User not found"));

        return buildProfileDto(user);
    }

    private ProfileDto buildProfileDto(User user) {
        String companyName = null;
        if (user instanceof Admin) {
            Admin admin = (Admin) user;
            companyName = admin.getCompany() != null ? admin.getCompany().getName() : null;
        } else if (user instanceof HRManager) {
            HRManager hrManager = (HRManager) user;
            companyName = hrManager.getCompany() != null ? hrManager.getCompany().getName() : null;
        }

        return new ProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                companyName
        );
    }

    public ProfileDto updateProfile(Long userId, UpdateProfileDto updateProfileDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileException("User not found"));

        // Vérifier si l'email existe déjà pour un autre utilisateur
        if (!user.getEmail().equals(updateProfileDto.getEmail()) &&
                userRepository.existsByEmail(updateProfileDto.getEmail())) {
            throw new ProfileException("Email already exists");
        }

        // Vérifier si le username existe déjà pour un autre utilisateur
        if (!user.getUsername().equals(updateProfileDto.getUsername()) &&
                userRepository.existsByUsername(updateProfileDto.getUsername())) {
            throw new ProfileException("Username already exists");
        }

        user.setUsername(updateProfileDto.getUsername());
        user.setEmail(updateProfileDto.getEmail());

        User updatedUser = userRepository.save(user);

        return getUserProfile(updatedUser.getId());
    }

    public void changePassword(Long userId, ChangePasswordDto changePasswordDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileException("User not found"));

        // Vérifier le mot de passe actuel
        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPassword())) {
            throw new ProfileException("Current password is incorrect");
        }

        // Vérifier que les nouveaux mots de passe correspondent
        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword())) {
            throw new ProfileException("New passwords do not match");
        }

        // Vérifier que le nouveau mot de passe est différent de l'ancien
        if (passwordEncoder.matches(changePasswordDto.getNewPassword(), user.getPassword())) {
            throw new ProfileException("New password must be different from current password");
        }

        // Encoder et sauvegarder le nouveau mot de passe
        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);
    }
}