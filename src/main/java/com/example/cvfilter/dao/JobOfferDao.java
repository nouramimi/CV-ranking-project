package com.example.cvfilter.dao;

import com.example.cvfilter.dao.entity.JobOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobOfferDao {

    JobOffer save(JobOffer offer);

    List<JobOffer> findAll();

    List<JobOffer> findActiveOffers();

    Optional<JobOffer> findById(Long id);

    void deleteById(Long id);

    boolean existsById(Long id);

    @Query("SELECT jo FROM JobOffer jo " +
            "LEFT JOIN Company c ON jo.companyId = c.id " +
            "WHERE (:active IS NULL OR jo.isActive = :active) " +
            "AND (:employmentType IS NULL OR jo.employmentType = :employmentType) " +
            "AND (:salary IS NULL OR (jo.minSalary <= :salary AND jo.maxSalary >= :salary)) " +
            "AND (:companyName IS NULL OR (c.name LIKE %:companyName%)) " +
            "AND (:jobTitle IS NULL OR (jo.title LIKE %:jobTitle%))")
    Page<JobOffer> findAllWithFilters(
            @Param("active") Boolean active,
            @Param("employmentType") JobOffer.EmploymentType employmentType,
            @Param("salary") Double salary,
            @Param("companyName") String companyName,
            @Param("jobTitle") String jobTitle,
            Pageable pageable);
    @Query("SELECT jo FROM JobOffer jo " +
            "LEFT JOIN Company c ON jo.companyId = c.id " +
            "WHERE jo.companyId = :companyId " +
            "AND (:active IS NULL OR jo.isActive = :active) " +
            "AND (:employmentType IS NULL OR jo.employmentType = :employmentType) " +
            "AND (:salary IS NULL OR (jo.minSalary <= :salary AND jo.maxSalary >= :salary)) " +
            "AND (:companyName IS NULL OR (c.name LIKE %:companyName%)) " +
            "AND (:jobTitle IS NULL OR (jo.title LIKE %:jobTitle%))")
    Page<JobOffer> findByCompanyIdAndFilters(
            @Param("companyId") Long companyId,
            @Param("active") Boolean active,
            @Param("employmentType") JobOffer.EmploymentType employmentType,
            @Param("salary") Double salary,
            @Param("companyName") String companyName,
            @Param("jobTitle") String jobTitle,
            Pageable pageable);

}
