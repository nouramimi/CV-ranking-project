package com.example.cvfilter.dao.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@DiscriminatorValue("ADMIN")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends User {

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    public Admin() {
    }

    public Admin(String username, String email, String password, Role role, Company company) {
        super(null, username, email, password, role);
        this.company = company;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}