package com.example.cvfilter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UpdateCvInfoDto {

    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Email(message = "Email should be valid")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Size(max = 5000, message = "Skills must not exceed 5000 characters")
    private String skills;

    @Size(max = 2000, message = "Experience must not exceed 2000 characters")
    private String experience;

    @Size(max = 5000, message = "Education must not exceed 5000 characters")
    private String education;

    public UpdateCvInfoDto() {}

    public UpdateCvInfoDto(String name, String email, String phone, String description,
                           String skills, String experience, String education) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.description = description;
        this.skills = skills;
        this.experience = experience;
        this.education = education;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
}