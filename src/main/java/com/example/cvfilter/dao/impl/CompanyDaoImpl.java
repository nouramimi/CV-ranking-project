package com.example.cvfilter.dao.impl;

import com.example.cvfilter.dao.CompanyDao;
import com.example.cvfilter.dao.entity.Company;
import com.example.cvfilter.dao.repository.CompanyRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CompanyDaoImpl implements CompanyDao {

    private final CompanyRepository companyRepository;

    public CompanyDaoImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Company save(Company company) {
        return companyRepository.save(company);
    }

    @Override
    public Optional<Company> findById(Long id) {
        return companyRepository.findById(id);
    }

    @Override
    public List<Company> findAll() {
        return companyRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        companyRepository.deleteById(id);
    }

    @Override
    public Company update(Company company) {
        // Assuming company.id is set
        return companyRepository.save(company);
    }

    @Override
    public Optional<Company> findByName(String name) {
        return companyRepository.findByName(name);
    }

    @Override
    public boolean existsByName(String name) {
        return companyRepository.existsByName(name);
    }
}
