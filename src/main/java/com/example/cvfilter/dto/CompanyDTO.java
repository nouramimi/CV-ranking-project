package com.example.cvfilter.dto;

public class CompanyDTO {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String logo;

    public CompanyDTO() {}

    public CompanyDTO(Long id, String name, String address, String phone, String logo) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.logo = logo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
}