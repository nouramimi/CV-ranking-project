package com.example.cvfilter.mapper;

import com.example.cvfilter.dao.entity.CvScores;
import com.example.cvfilter.dto.*;
import com.example.cvfilter.service.CvScoresService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CvScoresMapper {

    CvScoresResponseDto cvScoresToResponseDto(CvScores cvScores);

    List<CvScoresResponseDto> cvScoresToResponseDtos(List<CvScores> cvScores);

    CvRankResponseDto toCvRankResponseDto(Long userId, Long jobOfferId, Long rank);

    CvExistsResponseDto toCvExistsResponseDto(Long userId, Long jobOfferId, boolean exists);

    DeleteResponseDto toDeleteResponseDto(Long userId, Long jobOfferId, boolean success, String message);

    JobOfferDetailedStatsDto toJobOfferDetailedStatsDto(CvScoresService.JobOfferDetailedStats stats);

    MatchLevelDistributionDto toMatchLevelDistributionDto(CvScoresService.MatchLevelDistribution distribution);

    GlobalScoresSummaryDto toGlobalScoresSummaryDto(CvScoresService.GlobalScoresSummary summary);

    CvComparisonResponseDto toCvComparisonResponseDto(CvScoresService.CvComparisonResponse comparison);

    CvComparisonItemDto toCvComparisonItemDto(CvScoresService.CvComparisonItem item);

    List<CvComparisonItemDto> toCvComparisonItemDtos(List<CvScoresService.CvComparisonItem> items);

    default PaginatedResponse<CvScoresResponseDto> toPageDto(Page<CvScores> page) {
        List<CvScoresResponseDto> content = cvScoresToResponseDtos(page.getContent());
        return new PaginatedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    default CvRankResponseDto createCvRankResponseDto(Long userId, Long jobOfferId, Long rank) {
        return new CvRankResponseDto(userId, jobOfferId, rank);
    }

    default CvExistsResponseDto createCvExistsResponseDto(Long userId, Long jobOfferId, boolean exists) {
        return new CvExistsResponseDto(userId, jobOfferId, exists);
    }

    default DeleteResponseDto createDeleteResponseDto(Long userId, Long jobOfferId, boolean success, String message) {
        return new DeleteResponseDto(userId, jobOfferId, success, message);
    }
}