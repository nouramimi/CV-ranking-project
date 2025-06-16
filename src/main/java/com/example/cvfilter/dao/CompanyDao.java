package com.example.cvfilter.dao;

import com.example.cvfilter.dao.entity.Company;

import java.util.List;
import java.util.Optional;

public interface CompanyDao {

    Company save(Company company);

    Optional<Company> findById(Long id);

    List<Company> findAll();

    void deleteById(Long id);

    Company update(Company company);
}
