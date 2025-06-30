package com.example.cvfilter.service.impl;

import com.example.cvfilter.dao.entity.Company;
import com.example.cvfilter.dto.CompanyCreateDTO;
import com.example.cvfilter.dto.CompanyDTO;

import java.util.List;
import java.util.Optional;

public interface CompanyServiceInterface {
    CompanyDTO createCompany(CompanyCreateDTO companyCreateDTO);
    CompanyDTO getCompanyById(Long id);
    List<CompanyDTO> getAllCompanies();
    CompanyDTO updateCompany(Long id, CompanyDTO companyDTO);
    void deleteCompany(Long id);
}
