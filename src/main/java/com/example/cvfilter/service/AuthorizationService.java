package com.example.cvfilter.service;

import com.example.cvfilter.dao.UserDao;
import com.example.cvfilter.dao.entity.Admin;
import com.example.cvfilter.dao.entity.HRManager;
import com.example.cvfilter.dao.entity.Role;
import com.example.cvfilter.dao.entity.User;
import com.example.cvfilter.exception.UnauthorizedAccessException;
import com.example.cvfilter.service.impl.AuthorizationServiceInterface;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthorizationService implements AuthorizationServiceInterface {

    private final UserDao userDao;

    public AuthorizationService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void checkCompanyAccess(String email, Long companyId) {
        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            return;
        }

        if (user.getRole() == Role.USER) {
            return;
        }

        if (user.getRole() == Role.HR_MANAGER) {
            HRManager hrManager = (HRManager) user;
            if (!hrManager.getCompany().getId().equals(companyId)) {
                throw new UnauthorizedAccessException("Access denied: Different company");
            }
        } else {
            throw new UnauthorizedAccessException("Access denied: Insufficient privileges");
        }
    }

    public Long getUserCompanyId(String email) {
        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            Admin admin = (Admin) user;
            return admin.getCompany() != null ? admin.getCompany().getId() : null;
        }

        if (user.getRole() == Role.HR_MANAGER) {
            HRManager hrManager = (HRManager) user;
            return hrManager.getCompany().getId();
        }

        throw new UnauthorizedAccessException("User has no company association");
    }

    @Override
    public Long getUserIdByEmail(String email) {
        Optional<User> user = userDao.findByEmail(email);
        return user.map(User::getId)
                .orElseThrow(() -> new UnauthorizedAccessException("User not found with email: " + email));
    }

}