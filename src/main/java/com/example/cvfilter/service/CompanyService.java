package com.example.cvfilter.service;

import com.example.cvfilter.dao.CompanyDao;
import com.example.cvfilter.dao.entity.Company;
import com.example.cvfilter.dto.CompanyDTO;
import com.example.cvfilter.dto.CompanyCreateDTO;
import com.example.cvfilter.exception.CompanyNotFoundException;
import com.example.cvfilter.mapper.CompanyMapper;
import com.example.cvfilter.service.impl.CompanyServiceInterface;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService implements CompanyServiceInterface {

    private final CompanyDao companyDao;
    private final CompanyMapper companyMapper;

    public CompanyService(CompanyDao companyDao, CompanyMapper companyMapper) {
        this.companyDao = companyDao;
        this.companyMapper = companyMapper;
    }

    @Override
    public CompanyDTO createCompany(CompanyCreateDTO companyCreateDTO) {
        Company company = companyMapper.toEntity(companyCreateDTO);
        Company savedCompany = companyDao.save(company);
        return companyMapper.toDTO(savedCompany);
    }

    @Override
    public CompanyDTO getCompanyById(Long id) {
        Company company = companyDao.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with id " + id));
        return companyMapper.toDTO(company);
    }

    @Override
    public List<CompanyDTO> getAllCompanies() {
        List<Company> companies = companyDao.findAll();
        return companyMapper.toDTOList(companies);
    }

    @Override
    public CompanyDTO updateCompany(Long id, CompanyDTO companyDTO) {
        Company existing = companyDao.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with id " + id));

        companyMapper.updateEntityFromDTO(companyDTO, existing);
        Company updatedCompany = companyDao.save(existing);
        return companyMapper.toDTO(updatedCompany);
    }

    @Override
    public void deleteCompany(Long id) {
        if (!companyDao.existsById(id)) {
            throw new CompanyNotFoundException("Company not found with id " + id);
        }
        companyDao.deleteById(id);
    }
}