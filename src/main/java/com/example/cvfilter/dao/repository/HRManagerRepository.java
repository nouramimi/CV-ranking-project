package com.example.cvfilter.dao.repository;

import com.example.cvfilter.dao.entity.HRManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HRManagerRepository extends JpaRepository<HRManager, Long> {
    Optional<HRManager> findByEmail(String email);
}
