package com.example.cvfilter.service;

import com.example.cvfilter.dao.UserDao;
import com.example.cvfilter.dao.entity.User;
import com.example.cvfilter.dao.repository.UserRepository;
import com.example.cvfilter.dto.ChangePasswordDto;
import com.example.cvfilter.dto.ProfileDto;
import com.example.cvfilter.dto.UpdateProfileDto;
import com.example.cvfilter.exception.ProfileException;
import com.example.cvfilter.mapper.ProfileMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProfileService {

    private final UserDao userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileMapper profileMapper;

    public ProfileService(UserDao userRepository,
                          PasswordEncoder passwordEncoder,
                          ProfileMapper profileMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.profileMapper = profileMapper;
    }

    public ProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileException("User not found"));

        return profileMapper.userToProfileDto(user);
    }

    public ProfileDto getUserProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ProfileException("User not found"));

        return profileMapper.userToProfileDto(user);
    }

    public ProfileDto updateProfile(Long userId, UpdateProfileDto updateProfileDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileException("User not found"));

        if (!user.getEmail().equals(updateProfileDto.getEmail()) &&
                userRepository.existsByEmail(updateProfileDto.getEmail())) {
            throw new ProfileException("Email already exists");
        }

        if (!user.getUsername().equals(updateProfileDto.getUsername()) &&
                userRepository.existsByUsername(updateProfileDto.getUsername())) {
            throw new ProfileException("Username already exists");
        }

        profileMapper.updateUserFromDto(updateProfileDto, user);

        User updatedUser = userRepository.save(user);

        return profileMapper.userToProfileDto(updatedUser);
    }

    public void changePassword(Long userId, ChangePasswordDto changePasswordDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileException("User not found"));

        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPassword())) {
            throw new ProfileException("Current password is incorrect");
        }

        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword())) {
            throw new ProfileException("New passwords do not match");
        }

        if (passwordEncoder.matches(changePasswordDto.getNewPassword(), user.getPassword())) {
            throw new ProfileException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);
    }
}