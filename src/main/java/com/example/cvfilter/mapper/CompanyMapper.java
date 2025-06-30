package com.example.cvfilter.mapper;

import com.example.cvfilter.dao.entity.Company;
import com.example.cvfilter.dto.CompanyDTO;
import com.example.cvfilter.dto.CompanyCreateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    CompanyDTO toDTO(Company company);
    Company toEntity(CompanyDTO companyDTO);
    List<CompanyDTO> toDTOList(List<Company> companies);

    @Mapping(target = "id", ignore = true)
    Company toEntity(CompanyCreateDTO companyCreateDTO);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDTO(CompanyDTO companyDTO, @MappingTarget Company company);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromCreateDTO(CompanyCreateDTO companyCreateDTO, @MappingTarget Company company);
}
