package com.example.cvfilter.dao;

import com.example.cvfilter.dao.entity.User;

import java.util.Optional;

public interface UserDao {
    Optional<User> findByUsername(String username);
    User save(User user);
    Optional<User> findById(Long id);
    boolean existsById(Long id);
    Optional<User> findByEmail(String email);
}
