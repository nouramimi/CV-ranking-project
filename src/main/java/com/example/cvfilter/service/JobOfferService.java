package com.example.cvfilter.service;

import com.example.cvfilter.dao.CompanyDao;
import com.example.cvfilter.dao.JobOfferDao;
import com.example.cvfilter.dao.entity.Company;
import com.example.cvfilter.dao.entity.JobOffer;
import com.example.cvfilter.dto.JobOfferDTO;
import com.example.cvfilter.dto.JobOfferWithCompanyDTO;
import com.example.cvfilter.dto.PaginatedResponse;
import com.example.cvfilter.exception.InvalidJobOfferException;
import com.example.cvfilter.exception.JobOfferNotFoundException;
import com.example.cvfilter.exception.UnauthorizedAccessException;
import com.example.cvfilter.service.impl.AuthorizationServiceInterface;
import com.example.cvfilter.service.impl.JobOfferServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private static final Logger logger = LoggerFactory.getLogger(JobOfferService.class);

    public JobOfferService(JobOfferDao jobOfferDao, AuthorizationServiceInterface authorizationService, CompanyDao companyDao) {
        this.jobOfferDao = jobOfferDao;
        this.authorizationService = authorizationService;
        this.companyDao = companyDao;
    }

    @Override
    public JobOfferDTO create(JobOffer offer, String email) {
        Long userCompanyId = authorizationService.getUserCompanyId(email);
        if (userCompanyId != null) {
            offer.setCompanyId(userCompanyId);
        }

        validateJobOffer(offer);
        JobOffer savedOffer = jobOfferDao.save(offer);
        return new JobOfferDTO(savedOffer);
    }

    @Override
    public PaginatedResponse<JobOfferWithCompanyDTO> getAllJobOffersWithCompanyInfo(
            String email, Boolean active, JobOffer.EmploymentType employmentType,
            Double salary, String companyName, String jobTitle, int page, int size) {

        try {
            Long userCompanyId = authorizationService.getUserCompanyId(email);
            Pageable pageable = PageRequest.of(page, size);

            Page<JobOffer> jobOfferPage;
            if (userCompanyId != null) {
                // For users associated with a company, only show their company's job offers
                jobOfferPage = jobOfferDao.findByCompanyIdAndFilters(
                        userCompanyId,
                        active,
                        employmentType,
                        salary,
                        companyName,
                        jobTitle,
                        pageable);
            } else {
                jobOfferPage = jobOfferDao.findAllWithFilters(
                        active,
                        employmentType,
                        salary,
                        companyName,
                        jobTitle,
                        pageable);
            }

            List<JobOfferWithCompanyDTO> content = jobOfferPage.getContent().stream()
                    .map(jobOffer -> {
                        Company company = jobOffer.getCompanyId() != null ?
                                companyDao.findById(jobOffer.getCompanyId()).orElse(null) :
                                null;
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
            Pageable pageable = PageRequest.of(page, size);
            Page<JobOffer> jobOfferPage = jobOfferDao.findAllWithFilters(
                    active,
                    employmentType,
                    salary,
                    companyName,
                    jobTitle,
                    pageable);

            List<JobOfferWithCompanyDTO> content = jobOfferPage.getContent().stream()
                    .map(jobOffer -> {
                        Company company = jobOffer.getCompanyId() != null ?
                                companyDao.findById(jobOffer.getCompanyId()).orElse(null) :
                                null;
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


    /*@Override
    public PaginatedResponse<JobOfferWithCompanyDTO> getAllJobOffersWithCompanyInfo(
            String email, Boolean active, int page, int size) {

        List<JobOfferDTO> offers = Boolean.TRUE.equals(active) ?
                getActiveOffers(email) : getAll(email);

        int totalElements = offers.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        if (fromIndex > totalElements) {
            fromIndex = totalElements;
        }

        List<JobOfferDTO> paginatedOffers = offers.subList(fromIndex, toIndex);

        List<JobOfferWithCompanyDTO> content = paginatedOffers.stream()
                .map(jobOfferDTO -> {
                    JobOffer jobOffer = convertDtoToEntity(jobOfferDTO);
                    Company company = jobOffer.getCompanyId() != null ?
                            companyDao.findById(jobOffer.getCompanyId()).orElse(null) :
                            null;
                    return new JobOfferWithCompanyDTO(jobOffer, company);
                })
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages
        );
    }*/

    /*@Override
    public List<JobOfferWithCompanyDTO> getAllJobOffersWithCompanyInfo(String email, Boolean active) {
        List<JobOfferDTO> offers;
        if (Boolean.TRUE.equals(active)) {
            offers = getActiveOffers(email);
        } else {
            offers = getAll(email);
        }

        return offers.stream()
                .map(jobOfferDTO -> {
                    JobOffer jobOffer = convertDtoToEntity(jobOfferDTO);
                    Company company = jobOffer.getCompanyId() != null ?
                            companyDao.findById(jobOffer.getCompanyId()).orElse(null) :
                            null;
                    return new JobOfferWithCompanyDTO(jobOffer, company);
                })
                .collect(Collectors.toList());
    }*/

    private JobOffer convertDtoToEntity(JobOfferDTO dto) {
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(dto.getId());
        jobOffer.setCompanyId(dto.getCompanyId());
        jobOffer.setTitle(dto.getTitle());
        jobOffer.setDescription(dto.getDescription());
        jobOffer.setDetailedDescription(dto.getDetailedDescription());
        jobOffer.setLocation(dto.getLocation());
        jobOffer.setDepartment(dto.getDepartment());
        jobOffer.setEmploymentType(dto.getEmploymentType());
        jobOffer.setPostingDate(dto.getPostingDate());
        jobOffer.setClosingDate(dto.getClosingDate());
        jobOffer.setMinSalary(dto.getMinSalary());
        jobOffer.setMaxSalary(dto.getMaxSalary());
        jobOffer.setSalaryCurrency(dto.getSalaryCurrency());
        jobOffer.setYearsOfExperienceRequired(dto.getYearsOfExperienceRequired());
        jobOffer.setContactEmail(dto.getContactEmail());
        jobOffer.setContactPhone(dto.getContactPhone());
        jobOffer.setActive(dto.getActive());
        jobOffer.setSkills(dto.getSkills());
        jobOffer.setTechnologies(dto.getTechnologies());
        jobOffer.setRequiredDegree(dto.getRequiredDegree());
        return jobOffer;
    }

    @Override
    public List<JobOfferDTO> getAll(String email) {
        try {
            Long userCompanyId = authorizationService.getUserCompanyId(email);
            if (userCompanyId != null) {
                return jobOfferDao.findAll().stream()
                        .filter(offer -> userCompanyId.equals(offer.getCompanyId()))
                        .map(JobOfferDTO::new)
                        .collect(Collectors.toList());
            }
        } catch (UnauthorizedAccessException e) {
            logger.debug("User has no company association - returning all job offers");
        }

        return jobOfferDao.findAll().stream()
                .map(JobOfferDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobOfferDTO> getActiveOffers(String email) {
        Long userCompanyId = authorizationService.getUserCompanyId(email);

        if (userCompanyId == null) {
            return jobOfferDao.findActiveOffers().stream()
                    .map(JobOfferDTO::new)
                    .collect(Collectors.toList());
        } else {
            return jobOfferDao.findActiveOffers().stream()
                    .filter(offer -> userCompanyId.equals(offer.getCompanyId()))
                    .map(JobOfferDTO::new)
                    .collect(Collectors.toList());
        }
    }



    /*@Override
    public List<JobOfferDTO> getAll(String email) {
        try {
            Long userCompanyId = authorizationService.getUserCompanyId(email);
            if (userCompanyId != null) {
                return jobOfferDao.findAll().stream()
                        .filter(offer -> userCompanyId.equals(offer.getCompanyId()))
                        .map(JobOfferDTO::new)
                        .collect(Collectors.toList());
            }
        } catch (UnauthorizedAccessException e) {
            logger.debug("User has no company association - returning all job offers");
        }

        return jobOfferDao.findActiveOffers().stream()
                .map(JobOfferDTO::new)
                .collect(Collectors.toList());
    }


    @Override
    public List<JobOfferDTO> getActiveOffers(String email) {
        Long userCompanyId = authorizationService.getUserCompanyId(email);

        if (userCompanyId == null) {
            return jobOfferDao.findActiveOffers().stream()
                    .map(JobOfferDTO::new)
                    .collect(Collectors.toList());
        } else {
            return jobOfferDao.findActiveOffers().stream()
                    .filter(offer -> userCompanyId.equals(offer.getCompanyId()))
                    .map(JobOfferDTO::new)
                    .collect(Collectors.toList());
        }
    }*/

    @Override
    public Optional<JobOfferDTO> getById(Long id, String email) {
        if (id == null || id <= 0) {
            throw new InvalidJobOfferException("ID must be a positive number.");
        }

        Optional<JobOffer> jobOffer = jobOfferDao.findById(id);

        if (jobOffer.isPresent()) {
            authorizationService.checkCompanyAccess(email, jobOffer.get().getCompanyId());
            return Optional.of(new JobOfferDTO(jobOffer.get()));
        }

        return Optional.empty();
    }

    @Override
    public Optional<JobOfferDTO> update(Long id, JobOffer updated, String email) {
        if (id == null || id <= 0) {
            throw new InvalidJobOfferException("Invalid job offer ID.");
        }

        return jobOfferDao.findById(id)
                .map(existing -> {
                    authorizationService.checkCompanyAccess(email, existing.getCompanyId());
                    updateJobOfferFields(existing, updated);
                    JobOffer savedOffer = jobOfferDao.save(existing);
                    return new JobOfferDTO(savedOffer);
                });
    }


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

    private void updateJobOfferFields(JobOffer existing, JobOffer updated) {
        if (updated.getTitle() != null && !updated.getTitle().trim().isEmpty()) {
            existing.setTitle(updated.getTitle().trim());
        }
        if (updated.getDescription() != null && !updated.getDescription().trim().isEmpty()) {
            existing.setDescription(updated.getDescription().trim());
        }
        if (updated.getDetailedDescription() != null) {
            existing.setDetailedDescription(updated.getDetailedDescription());
        }
        if (updated.getLocation() != null && !updated.getLocation().trim().isEmpty()) {
            existing.setLocation(updated.getLocation().trim());
        }
        if (updated.getTitle() != null && !updated.getTitle().trim().isEmpty()) {
            existing.setTitle(updated.getTitle().trim());
        }
        if (updated.getDepartment() != null) {
            existing.setDepartment(updated.getDepartment());
        }
        if (updated.getEmploymentType() != null) {
            existing.setEmploymentType(updated.getEmploymentType());
        }
        if (updated.getPostingDate() != null) {
            existing.setPostingDate(updated.getPostingDate());
        }
        if (updated.getClosingDate() != null) {
            existing.setClosingDate(updated.getClosingDate());
        }
        if (updated.getMinSalary() != null) {
            existing.setMinSalary(updated.getMinSalary());
        }
        if (updated.getMaxSalary() != null) {
            existing.setMaxSalary(updated.getMaxSalary());
        }
        if (updated.getSalaryCurrency() != null) {
            existing.setSalaryCurrency(updated.getSalaryCurrency());
        }
        if (updated.getYearsOfExperienceRequired() != null) {
            existing.setYearsOfExperienceRequired(updated.getYearsOfExperienceRequired());
        }
        if (updated.getContactEmail() != null && updated.getContactEmail().contains("@")) {
            existing.setContactEmail(updated.getContactEmail());
        }
        if (updated.getContactPhone() != null) {
            existing.setContactPhone(updated.getContactPhone());
        }
        if (updated.getIsActive() != null) {
            existing.setActive(updated.getIsActive());
        }
        if (updated.getRequiredDegree() != null) {
            existing.setRequiredDegree(updated.getRequiredDegree());
        }
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
