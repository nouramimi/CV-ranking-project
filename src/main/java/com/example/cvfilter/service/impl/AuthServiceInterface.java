package com.example.cvfilter.service.impl;

import com.example.cvfilter.dao.entity.User;
import com.example.cvfilter.dto.JwtResponse;

import java.util.Optional;

public interface AuthServiceInterface {
    //User register(User user);
    JwtResponse register(User user);
    JwtResponse login(String email, String password);
    //Optional<JwtResponse> login(String email, String password);
}