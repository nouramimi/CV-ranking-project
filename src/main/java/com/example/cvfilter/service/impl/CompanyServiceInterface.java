package com.example.cvfilter.service.impl;

import com.example.cvfilter.dao.entity.Company;

import java.util.List;
import java.util.Optional;

public interface CompanyServiceInterface {
    Company createCompany(Company company);
    Company getCompanyById(Long id);
    List<Company> getAllCompanies();
    Company updateCompany(Long id, Company company);
    void deleteCompany(Long id);
}
