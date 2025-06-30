package com.example.cvfilter.mapper;

import com.example.cvfilter.dao.entity.CvInfo;
import com.example.cvfilter.dto.CvInfoResponseDto;
import com.example.cvfilter.dto.UpdateCvInfoDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CvUploadMapper {

    @Mapping(target = "yearsOfExperience", source = "yearsOfExperience")
    @Mapping(target = "userUsername", source = "cvInfo", qualifiedByName = "getUserUsername")
    @Mapping(target = "userEmail", source = "cvInfo", qualifiedByName = "getUserEmail")
    CvInfoResponseDto cvInfoToResponseDto(CvInfo cvInfo);

    List<CvInfoResponseDto> cvInfosToResponseDtos(List<CvInfo> cvInfos);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "jobOfferId", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "cvPath", ignore = true)
    @Mapping(target = "extractedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "yearsOfExperience", ignore = true)
    @Mapping(target = "highestDegree", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "jobOffer", ignore = true)
    void updateCvInfoFromDto(UpdateCvInfoDto updateDto, @MappingTarget CvInfo cvInfo);

    @Named("getUserUsername")
    default String getUserUsername(CvInfo cvInfo) {
        return cvInfo.getUser() != null ? cvInfo.getUser().getUsername() : null;
    }

    @Named("getUserEmail")
    default String getUserEmail(CvInfo cvInfo) {
        return cvInfo.getUser() != null ? cvInfo.getUser().getEmail() : null;
    }
}