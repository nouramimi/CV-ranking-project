package com.example.cvfilter.dto;

import com.example.cvfilter.dao.entity.Role;

public class HRManagerResponseDto {

    private Long id;
    private String username;
    private String email;
    private Role role;
    private String companyName;
    private Long companyId;

    public HRManagerResponseDto() {}

    public HRManagerResponseDto(Long id, String username, String email, Role role,
                                String companyName, Long companyId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.companyName = companyName;
        this.companyId = companyId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
}