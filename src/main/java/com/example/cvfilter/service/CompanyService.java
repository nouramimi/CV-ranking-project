package com.example.cvfilter.service;

import com.example.cvfilter.dao.CompanyDao;
import com.example.cvfilter.dao.entity.Company;
import com.example.cvfilter.exception.CompanyNotFoundException;
import com.example.cvfilter.service.impl.CompanyServiceInterface;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyService implements CompanyServiceInterface {

    private final CompanyDao companyDao;

    public CompanyService(CompanyDao companyDao) {
        this.companyDao = companyDao;
    }

    @Override
    public Company createCompany(Company company) {
        return companyDao.save(company);
    }

    public Company getCompanyById(Long id) {
        return companyDao.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with id " + id));
    }


    @Override
    public List<Company> getAllCompanies() {
        return companyDao.findAll();
    }

    @Override
    public Company updateCompany(Long id, Company company) {
        Company existing = companyDao.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with id " + id));

        existing.setName(company.getName());
        existing.setAddress(company.getAddress());
        existing.setPhone(company.getPhone());
        existing.setLogo(company.getLogo());

        return companyDao.save(existing);
    }


    @Override
    public void deleteCompany(Long id) {
        companyDao.deleteById(id);
    }
}
