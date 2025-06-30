package com.example.cvfilter.mapper;

import com.example.cvfilter.dao.entity.Admin;
import com.example.cvfilter.dao.entity.HRManager;
import com.example.cvfilter.dao.entity.User;
import com.example.cvfilter.dto.ProfileDto;
import com.example.cvfilter.dto.UpdateProfileDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    @Mapping(target = "companyName", source = "user", qualifiedByName = "getCompanyName")
    ProfileDto userToProfileDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateUserFromDto(UpdateProfileDto updateProfileDto, @MappingTarget User user);

    @Named("getCompanyName")
    default String getCompanyName(User user) {
        if (user instanceof Admin) {
            Admin admin = (Admin) user;
            return admin.getCompany() != null ? admin.getCompany().getName() : null;
        } else if (user instanceof HRManager) {
            HRManager hrManager = (HRManager) user;
            return hrManager.getCompany() != null ? hrManager.getCompany().getName() : null;
        }
        return null;
    }
}