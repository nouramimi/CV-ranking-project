package com.example.cvfilter.service;

import com.example.cvfilter.dao.CompanyDao;
import com.example.cvfilter.dao.JobOfferDao;
import com.example.cvfilter.dao.entity.Company;
import com.example.cvfilter.dao.entity.JobOffer;
import com.example.cvfilter.dto.JobOfferDTO;
import com.example.cvfilter.dto.JobOfferCreateDTO;
import com.example.cvfilter.dto.JobOfferWithCompanyDTO;
import com.example.cvfilter.dto.PaginatedResponse;
import com.example.cvfilter.exception.InvalidJobOfferException;
import com.example.cvfilter.exception.JobOfferNotFoundException;
import com.example.cvfilter.exception.UnauthorizedAccessException;
import com.example.cvfilter.mapper.JobOfferMapper;
import com.example.cvfilter.service.impl.AuthorizationServiceInterface;
import com.example.cvfilter.service.impl.JobOfferServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class JobOfferService implements JobOfferServiceInterface {

    private final JobOfferDao jobOfferDao;
    private final AuthorizationServiceInterface authorizationService;
    private final CompanyDao companyDao;
    private final JobOfferMapper jobOfferMapper;
    private static final Logger logger = LoggerFactory.getLogger(JobOfferService.class);

    public JobOfferService(JobOfferDao jobOfferDao,
                           AuthorizationServiceInterface authorizationService,
                           CompanyDao companyDao,
                           JobOfferMapper jobOfferMapper) {
        this.jobOfferDao = jobOfferDao;
        this.authorizationService = authorizationService;
        this.companyDao = companyDao;
        this.jobOfferMapper = jobOfferMapper;
    }

    @Override
    public JobOfferDTO create(JobOfferCreateDTO jobOfferCreateDTO, String email) {
        Long userCompanyId = authorizationService.getUserCompanyId(email);
        if (userCompanyId != null) {
            jobOfferCreateDTO.setCompanyId(userCompanyId);
        }

        JobOffer jobOffer = jobOfferMapper.toEntity(jobOfferCreateDTO);
        validateJobOffer(jobOffer);
        JobOffer savedOffer = jobOfferDao.save(jobOffer);
        return jobOfferMapper.toDTO(savedOffer);
    }

    @Override
    public PaginatedResponse<JobOfferWithCompanyDTO> getAllJobOffersWithCompanyInfo(
            String email, Boolean active, JobOffer.EmploymentType employmentType,
            Double salary, String companyName, String jobTitle, int page, int size) {

        try {
            Long userCompanyId = authorizationService.getUserCompanyId(email);
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postingDate"));

            Page<JobOffer> jobOfferPage;
            if (userCompanyId != null) {
                jobOfferPage = jobOfferDao.findByCompanyIdAndFilters(
                        userCompanyId, active, employmentType, salary, companyName, jobTitle, pageable);
            } else {
                jobOfferPage = jobOfferDao.findAllWithFilters(
                        active, employmentType, salary, companyName, jobTitle, pageable);
            }

            List<JobOfferWithCompanyDTO> content = jobOfferPage.getContent().stream()
                    .map(jobOffer -> {
                        Company company = jobOffer.getCompanyId() != null ?
                                companyDao.findById(jobOffer.getCompanyId()).orElse(null) : null;
                        return new JobOfferWithCompanyDTO(jobOffer, company);
                    })
                    .collect(Collectors.toList());

            return new PaginatedResponse<>(
                    content,
                    jobOfferPage.getNumber(),
                    jobOfferPage.getSize(),
                    (int) jobOfferPage.getTotalElements(),
                    jobOfferPage.getTotalPages()
            );
        } catch (UnauthorizedAccessException e) {
            logger.debug("User has no company association - returning all job offers");

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postingDate"));
            Page<JobOffer> jobOfferPage = jobOfferDao.findAllWithFilters(
                    active, employmentType, salary, companyName, jobTitle, pageable);

            List<JobOfferWithCompanyDTO> content = jobOfferPage.getContent().stream()
                    .map(jobOffer -> {
                        Company company = jobOffer.getCompanyId() != null ?
                                companyDao.findById(jobOffer.getCompanyId()).orElse(null) : null;
                        return new JobOfferWithCompanyDTO(jobOffer, company);
                    })
                    .collect(Collectors.toList());

            return new PaginatedResponse<>(
                    content,
                    jobOfferPage.getNumber(),
                    jobOfferPage.getSize(),
                    (int) jobOfferPage.getTotalElements(),
                    jobOfferPage.getTotalPages()
            );
        }
    }

    @Override
    public List<JobOfferDTO> getAll(String email) {
        try {
            Long userCompanyId = authorizationService.getUserCompanyId(email);
            if (userCompanyId != null) {
                List<JobOffer> jobOffers = jobOfferDao.findAll().stream()
                        .filter(offer -> userCompanyId.equals(offer.getCompanyId()))
                        .collect(Collectors.toList());
                return jobOfferMapper.toDTOList(jobOffers);
            }
        } catch (UnauthorizedAccessException e) {
            logger.debug("User has no company association - returning all job offers");
        }

        return jobOfferMapper.toDTOList(jobOfferDao.findAll());
    }

    @Override
    public List<JobOfferDTO> getActiveOffers(String email) {
        Long userCompanyId = authorizationService.getUserCompanyId(email);

        if (userCompanyId == null) {
            return jobOfferMapper.toDTOList(jobOfferDao.findActiveOffers());
        } else {
            List<JobOffer> activeOffers = jobOfferDao.findActiveOffers().stream()
                    .filter(offer -> userCompanyId.equals(offer.getCompanyId()))
                    .collect(Collectors.toList());
            return jobOfferMapper.toDTOList(activeOffers);
        }
    }

    @Override
    public Optional<JobOfferDTO> getById(Long id, String email) {
        if (id == null || id <= 0) {
            throw new InvalidJobOfferException("ID must be a positive number.");
        }

        Optional<JobOffer> jobOffer = jobOfferDao.findById(id);

        if (jobOffer.isPresent()) {
            authorizationService.checkCompanyAccess(email, jobOffer.get().getCompanyId());
            return Optional.of(jobOfferMapper.toDTO(jobOffer.get()));
        }

        return Optional.empty();
    }

    @Override
    public Optional<JobOfferDTO> update(Long id, JobOfferDTO updated, String email) {
        if (id == null || id <= 0) {
            throw new InvalidJobOfferException("Invalid job offer ID.");
        }

        return jobOfferDao.findById(id)
                .map(existing -> {
                    authorizationService.checkCompanyAccess(email, existing.getCompanyId());
                    jobOfferMapper.updateEntityFromDTO(updated, existing);
                    JobOffer savedOffer = jobOfferDao.save(existing);
                    return jobOfferMapper.toDTO(savedOffer);
                });
    }

    @Override
    public boolean delete(Long id, String email) {
        if (id == null || id <= 0) return false;

        Optional<JobOffer> jobOffer = jobOfferDao.findById(id);
        if (jobOffer.isPresent()) {
            authorizationService.checkCompanyAccess(email, jobOffer.get().getCompanyId());
            jobOfferDao.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean deactivate(Long id, String email) {
        return jobOfferDao.findById(id)
                .map(offer -> {
                    authorizationService.checkCompanyAccess(email, offer.getCompanyId());
                    offer.setActive(false);
                    jobOfferDao.save(offer);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public Optional<JobOfferWithCompanyDTO> getByIdWithCompany(Long id, String email) {
        if (id == null || id <= 0) {
            throw new InvalidJobOfferException("ID must be a positive number.");
        }

        Optional<JobOffer> jobOfferOpt = jobOfferDao.findById(id);

        if (jobOfferOpt.isEmpty()) {
            return Optional.empty();
        }

        JobOffer jobOffer = jobOfferOpt.get();
        authorizationService.checkCompanyAccess(email, jobOffer.getCompanyId());

        Company company = null;
        if (jobOffer.getCompanyId() != null) {
            company = companyDao.findById(jobOffer.getCompanyId()).orElse(null);
        }

        return Optional.of(new JobOfferWithCompanyDTO(jobOffer, company));
    }

    @Override
    public boolean exists(Long id) {
        return id != null && id > 0 && jobOfferDao.existsById(id);
    }

    @Override
    public String getJobDescription(Long jobOfferId, String email) {
        return getById(jobOfferId, email)
                .map(JobOfferDTO::getDescription)
                .orElse(null);
    }

    @Override
    public Company getCompanyDetailsByJobOfferId(Long jobOfferId, String email) {
        Optional<JobOfferDTO> jobOfferOpt = getById(jobOfferId, email);

        if (jobOfferOpt.isEmpty()) {
            throw new JobOfferNotFoundException("Job offer not found with ID: " + jobOfferId);
        }

        JobOfferDTO jobOfferDTO = jobOfferOpt.get();
        Long companyId = jobOfferDTO.getCompanyId();

        if (companyId == null) {
            throw new IllegalStateException("Job offer has no associated company");
        }

        return companyDao.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));
    }

    // Méthodes utilitaires restent identiques
    private void validateJobOffer(JobOffer offer) {
        if (offer.getTitle() == null || offer.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Job title cannot be empty");
        }
        if (offer.getDescription() == null || offer.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Job description cannot be empty");
        }
        if (offer.getLocation() == null || offer.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Job location cannot be empty");
        }
        if (offer.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID must be specified");
        }
        if (offer.getEmploymentType() == null) {
            throw new IllegalArgumentException("Employment type must be specified");
        }
        if (offer.getPostingDate() == null) {
            offer.setPostingDate(LocalDate.now());
        }
        if (offer.getClosingDate() != null && offer.getClosingDate().isBefore(offer.getPostingDate())) {
            throw new IllegalArgumentException("Closing date cannot be before posting date");
        }
        if (offer.getMinSalary() != null && offer.getMaxSalary() != null
                && offer.getMinSalary() > offer.getMaxSalary()) {
            throw new IllegalArgumentException("Minimum salary cannot be greater than maximum salary");
        }
        if (offer.getContactEmail() == null || !offer.getContactEmail().contains("@")) {
            throw new IllegalArgumentException("Valid contact email is required");
        }
        if (offer.getIsActive() == null) {
            offer.setActive(true);
        }
    }

    @Override
    public JobOffer parseJobOfferFromDescription(String jobTitle, String jobDescription) {
        JobOffer jobOffer = new JobOffer();
        jobOffer.setTitle(jobTitle);
        jobOffer.setDescription(jobDescription);

        Set<String> extractedSkills = extractSkillsFromDescription(jobDescription);
        jobOffer.setSkills(new ArrayList<>(extractedSkills));

        Double experienceYears = extractExperienceFromDescription(jobDescription);
        if (experienceYears != null) {
            jobOffer.setYearsOfExperienceRequired(experienceYears.intValue());
        }

        String educationLevel = extractEducationFromDescription(jobDescription);
        if (educationLevel != null) {
            try {
                jobOffer.setRequiredDegree(JobOffer.EducationLevel.valueOf(educationLevel.toUpperCase()));
            } catch (IllegalArgumentException e) {
                jobOffer.setRequiredDegree(JobOffer.EducationLevel.NONE_SPECIFIED);
            }
        } else {
            jobOffer.setRequiredDegree(JobOffer.EducationLevel.NONE_SPECIFIED);
        }

        return jobOffer;
    }

    public Set<String> extractSkillsFromDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return new HashSet<>();
        }

        String descriptionLower = description.toLowerCase();
        Set<String> skills = new HashSet<>();

        String[] commonSkills = {
                "java", "javascript", "python", "react", "angular", "vue", "node.js",
                "spring", "spring boot", "django", "flask", "mysql", "postgresql",
                "mongodb", "sql", "html", "css", "bootstrap", "tailwind",
                "aws", "azure", "docker", "kubernetes", "git", "linux",
                "c++", "c#", "php", "ruby", "go", "rust", "kotlin", "swift"
        };

        for (String skill : commonSkills) {
            if (descriptionLower.contains(skill.toLowerCase())) {
                skills.add(skill.substring(0, 1).toUpperCase() + skill.substring(1));
            }
        }

        return skills;
    }

    public Double extractExperienceFromDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }

        Pattern pattern = Pattern.compile(
                "(\\d+)(?:\\+|\\s*(?:to|-)\\s*\\d+)?\\s*(?:years?|yrs?)(?:\\s*(?:of\\s*)?experience)?",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    public String extractEducationFromDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }

        String descriptionLower = description.toLowerCase();

        if (descriptionLower.contains("phd") || descriptionLower.contains("doctorate")) {
            return "PHD";
        }
        if (descriptionLower.contains("master") || descriptionLower.contains("mba") ||
                descriptionLower.contains("ms") || descriptionLower.contains("m.sc")) {
            return "MASTER";
        }
        if (descriptionLower.contains("bachelor") || descriptionLower.contains("undergraduate") ||
                descriptionLower.contains("bs") || descriptionLower.contains("b.sc")) {
            return "BACHELOR";
        }
        if (descriptionLower.contains("associate") || descriptionLower.contains("a.a") ||
                descriptionLower.contains("a.s")) {
            return "ASSOCIATE";
        }
        if (descriptionLower.contains("high school") || descriptionLower.contains("hs diploma")) {
            return "HIGH_SCHOOL";
        }

        return null;
    }


}
