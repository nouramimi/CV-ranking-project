package com.example.cvfilter.service.impl;


import com.example.cvfilter.exception.UnauthorizedAccessException;

public interface AuthorizationServiceInterface {
    void checkCompanyAccess(String username, Long companyId);
    Long getUserCompanyId(String username) throws UnauthorizedAccessException;
    Long getUserIdByEmail(String email);
}
