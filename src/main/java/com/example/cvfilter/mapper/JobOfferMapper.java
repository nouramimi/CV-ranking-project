package com.example.cvfilter.mapper;

import com.example.cvfilter.dao.entity.JobOffer;
import com.example.cvfilter.dto.JobOfferDTO;
import com.example.cvfilter.dto.JobOfferCreateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JobOfferMapper {

    JobOfferDTO toDTO(JobOffer jobOffer);

    JobOffer toEntity(JobOfferDTO jobOfferDTO);

    List<JobOfferDTO> toDTOList(List<JobOffer> jobOffers);
    List<JobOffer> toEntityList(List<JobOfferDTO> jobOfferDTOs);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cvInfos", ignore = true)
    JobOffer toEntity(JobOfferCreateDTO jobOfferCreateDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cvInfos", ignore = true)
    void updateEntityFromDTO(JobOfferDTO jobOfferDTO, @MappingTarget JobOffer jobOffer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cvInfos", ignore = true)
    void updateEntityFromCreateDTO(JobOfferCreateDTO jobOfferCreateDTO, @MappingTarget JobOffer jobOffer);
}