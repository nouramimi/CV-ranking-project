package com.example.cvfilter.dao.repository;

import com.example.cvfilter.dao.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByName(String name);

    boolean existsByName(String name);
    boolean existsById(Long id);
    }
