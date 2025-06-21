package com.example.cvfilter.service;

import com.example.cvfilter.dao.UserDao;
import com.example.cvfilter.dao.AdminDao;
import com.example.cvfilter.dao.HRManagerDao;
import com.example.cvfilter.dao.CompanyDao;
import com.example.cvfilter.dao.entity.User;
import com.example.cvfilter.dao.entity.Admin;
import com.example.cvfilter.dao.entity.HRManager;
import com.example.cvfilter.dao.entity.Company;
import com.example.cvfilter.dao.entity.Role;
import com.example.cvfilter.dto.JwtResponse;
import com.example.cvfilter.dto.RegisterRequest;
import com.example.cvfilter.config.JwtUtils;
import com.example.cvfilter.exception.InvalidCredentialsException;
import com.example.cvfilter.service.impl.AuthServiceInterface;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService implements AuthServiceInterface {

    private final UserDao userDao;
    private final AdminDao adminDao;
    private final HRManagerDao hrManagerDao;
    private final CompanyDao companyDao;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserDao userDao, AdminDao adminDao, HRManagerDao hrManagerDao,
                       CompanyDao companyDao, JwtUtils jwtUtils) {
        this.userDao = userDao;
        this.adminDao = adminDao;
        this.hrManagerDao = hrManagerDao;
        this.companyDao = companyDao;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public JwtResponse register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userDao.save(user);

        String token = jwtUtils.generateToken(
                savedUser.getEmail(),
                savedUser.getUsername(),
                savedUser.getRole().name()
        );

        return new JwtResponse(token);
    }

    public JwtResponse registerWithCompany(RegisterRequest registerRequest) {

        if (registerRequest == null) {
            throw new InvalidCredentialsException("Registration request cannot be null");
        }

        if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
            throw new InvalidCredentialsException("Username is required");
        }

        if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
            throw new InvalidCredentialsException("Email is required");
        }

        if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
            throw new InvalidCredentialsException("Password is required");
        }

        if (registerRequest.getRole() == null) {
            throw new InvalidCredentialsException("Role is required");
        }

        if (findUserByEmail(registerRequest.getEmail()).isPresent()) {
            throw new InvalidCredentialsException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());

        try {
            switch (registerRequest.getRole()) {
                case ADMIN:
                    Admin admin = new Admin();
                    admin.setUsername(registerRequest.getUsername());
                    admin.setEmail(registerRequest.getEmail());
                    admin.setPassword(encodedPassword);
                    admin.setRole(Role.ADMIN);

                    if (registerRequest.getCompanyId() != null) {
                        Optional<Company> company = companyDao.findById(registerRequest.getCompanyId());
                        if (company.isPresent()) {
                            admin.setCompany(company.get());
                        } else {
                            throw new InvalidCredentialsException("Company not found");
                        }
                    }

                    Admin savedAdmin = adminDao.save(admin);
                    String adminToken = jwtUtils.generateToken(
                            savedAdmin.getEmail(),
                            savedAdmin.getUsername(),
                            savedAdmin.getRole().name()
                    );
                    return new JwtResponse(adminToken);

                case HR_MANAGER:
                    HRManager hrManager = new HRManager();
                    hrManager.setUsername(registerRequest.getUsername());
                    hrManager.setEmail(registerRequest.getEmail());
                    hrManager.setPassword(encodedPassword);
                    hrManager.setRole(Role.HR_MANAGER);

                    if (registerRequest.getCompanyId() != null) {
                        Optional<Company> company = companyDao.findById(registerRequest.getCompanyId());
                        if (company.isPresent()) {
                            hrManager.setCompany(company.get());
                        } else {
                            throw new InvalidCredentialsException("Company not found");
                        }
                    }

                    HRManager savedHRManager = hrManagerDao.save(hrManager);
                    String hrToken = jwtUtils.generateToken(
                            savedHRManager.getEmail(),
                            savedHRManager.getUsername(),
                            savedHRManager.getRole().name()
                    );
                    return new JwtResponse(hrToken);

                case USER:
                default:
                    User user = new User();
                    user.setUsername(registerRequest.getUsername());
                    user.setEmail(registerRequest.getEmail());
                    user.setPassword(encodedPassword);
                    user.setRole(Role.USER);

                    User savedUser = userDao.save(user);
                    String userToken = jwtUtils.generateToken(
                            savedUser.getEmail(),
                            savedUser.getUsername(),
                            savedUser.getRole().name()
                    );
                    return new JwtResponse(userToken);
            }
        } catch (Exception e) {
            if (e instanceof InvalidCredentialsException) {
                throw e;
            }
            throw new InvalidCredentialsException("Registration failed: " + e.getMessage());
        }
    }

    @Override
    public JwtResponse login(String email, String password) {
        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            throw new InvalidCredentialsException("Email and password are required");
        }

        try {
            User user = findUserByEmail(email)
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new InvalidCredentialsException("Invalid email or password");
            }

            String token = jwtUtils.generateToken(
                    user.getEmail(),
                    user.getUsername(),
                    user.getRole().name()
            );

            return new JwtResponse(token);
        } catch (Exception e) {
            if (e instanceof InvalidCredentialsException) {
                throw e;
            }
            throw new InvalidCredentialsException("Login failed: " + e.getMessage());
        }
    }

    private Optional<User> findUserByEmail(String email) {
        Optional<User> user = userDao.findByEmail(email);
        if (user.isPresent()) {
            return user;
        }

        Optional<Admin> admin = adminDao.findByEmail(email);
        if (admin.isPresent()) {
            return Optional.of((User) admin.get());
        }

        Optional<HRManager> hrManager = hrManagerDao.findByEmail(email);
        if (hrManager.isPresent()) {
            return Optional.of((User) hrManager.get());
        }

        return Optional.empty();
    }
}