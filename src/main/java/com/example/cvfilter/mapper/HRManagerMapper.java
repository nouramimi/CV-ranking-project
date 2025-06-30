package com.example.cvfilter.mapper;

import com.example.cvfilter.dao.entity.HRManager;
import com.example.cvfilter.dao.entity.Role;
import com.example.cvfilter.dto.CreateHRManagerDto;
import com.example.cvfilter.dto.HRManagerResponseDto;
import com.example.cvfilter.dto.UpdateHRManagerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HRManagerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", constant = "HR_MANAGER")
    @Mapping(target = "company", ignore = true)
    HRManager createDtoToEntity(CreateHRManagerDto createDto);

    @Mapping(target = "companyName", source = "hrManager", qualifiedByName = "getCompanyName")
    @Mapping(target = "companyId", source = "hrManager", qualifiedByName = "getCompanyId")
    HRManagerResponseDto entityToResponseDto(HRManager hrManager);

    List<HRManagerResponseDto> entitiesToResponseDtos(List<HRManager> hrManagers);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "company", ignore = true)
    void updateEntityFromDto(UpdateHRManagerDto updateDto, @MappingTarget HRManager hrManager);

    @Named("getCompanyName")
    default String getCompanyName(HRManager hrManager) {
        return hrManager.getCompany() != null ? hrManager.getCompany().getName() : null;
    }

    @Named("getCompanyId")
    default Long getCompanyId(HRManager hrManager) {
        return hrManager.getCompany() != null ? hrManager.getCompany().getId() : null;
    }
}