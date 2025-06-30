package com.example.cvfilter.dto;

public class CompanyCreateDTO {
    private String name;
    private String address;
    private String phone;
    private String logo;

    public CompanyCreateDTO() {}

    public CompanyCreateDTO(String name, String address, String phone, String logo) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.logo = logo;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
}