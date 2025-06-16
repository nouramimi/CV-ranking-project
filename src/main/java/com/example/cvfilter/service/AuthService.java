package com.example.cvfilter.service;

import com.example.cvfilter.dao.UserDao;
import com.example.cvfilter.dao.entity.User;
import com.example.cvfilter.dto.JwtResponse;
import com.example.cvfilter.config.JwtUtils;
import com.example.cvfilter.exception.InvalidCredentialsException;
import com.example.cvfilter.service.impl.AuthServiceInterface;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService implements AuthServiceInterface {

    private final UserDao userDao;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserDao userDao, JwtUtils jwtUtils) {
        this.userDao = userDao;
        this.jwtUtils = jwtUtils;
    }

    /*@Override
    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userDao.save(user);
    }*/

    @Override
    public JwtResponse register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userDao.save(user);

        String token = jwtUtils.generateToken(
                savedUser.getEmail(),
                savedUser.getUsername(),
                savedUser.getRole().name()
        );

        return new JwtResponse(token);
    }


    @Override
    public JwtResponse login(String email, String password) {
        if (email == null || password == null) {
            throw new InvalidCredentialsException("Email and password are required");
        }

        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtils.generateToken(
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );

        return new JwtResponse(token);
    }




}
