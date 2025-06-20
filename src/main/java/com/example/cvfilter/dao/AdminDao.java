package com.example.cvfilter.dao;

import com.example.cvfilter.dao.entity.Admin;

import java.util.List;
import java.util.Optional;

public interface AdminDao {
    Admin save(Admin admin);
    Optional<Admin> findById(Long id);
    List<Admin> findAll();
    Optional<Admin> findByEmail(String email);
    Optional<Admin> findByUsername(String username);
    void deleteById(Long id);
    boolean existsById(Long id);
}