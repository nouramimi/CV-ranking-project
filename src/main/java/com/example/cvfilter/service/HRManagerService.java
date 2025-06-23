package com.example.cvfilter.service;

import com.example.cvfilter.dao.HRManagerDao;
import com.example.cvfilter.dao.AdminDao;
import com.example.cvfilter.dao.entity.HRManager;
import com.example.cvfilter.dao.entity.Admin;
import com.example.cvfilter.exception.UserNotFoundException;
import com.example.cvfilter.service.impl.HRManagerServiceInterface;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HRManagerService implements HRManagerServiceInterface {

    private final HRManagerDao hrManagerDao;
    private final AdminDao adminDao;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public HRManagerService(HRManagerDao hrManagerDao, AdminDao adminDao) {
        this.hrManagerDao = hrManagerDao;
        this.adminDao = adminDao;
    }

    @Override
    public HRManager createHRManager(HRManager hrManager) {
        hrManager.setPassword(passwordEncoder.encode(hrManager.getPassword()));
        return hrManagerDao.save(hrManager);
    }

    @Override
    public HRManager createHRManagerByAdmin(HRManager hrManager, String adminEmail) {
        Admin admin = adminDao.findByEmail(adminEmail)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with email: " + adminEmail));

        hrManager.setCompany(admin.getCompany());
        hrManager.setPassword(passwordEncoder.encode(hrManager.getPassword()));

        return hrManagerDao.save(hrManager);
    }

    @Override
    public List<HRManager> getAllHRManagers() {
        return hrManagerDao.findAll();
    }

    @Override
    public HRManager getHRManagerById(Long id) {
        return hrManagerDao.findById(id)
                .orElseThrow(() -> new UserNotFoundException("HR Manager not found"));
    }

    @Override
    public HRManager updateHRManager(Long id, HRManager hrManager) {
        HRManager existing = getHRManagerById(id);
        existing.setUsername(hrManager.getUsername());
        existing.setEmail(hrManager.getEmail());
        existing.setCompany(hrManager.getCompany());
        existing.setPassword(passwordEncoder.encode(hrManager.getPassword()));
        return hrManagerDao.save(existing);
    }

    @Override
    public List<HRManager> getAllHRManagersByAdminCompany(String adminEmail) {
        Admin admin = adminDao.findByEmail(adminEmail)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with email: " + adminEmail));

        return hrManagerDao.findByCompanyId(admin.getCompany().getId());
    }

    @Override
    public HRManager updateHRManagerByAdmin(Long id, HRManager hrManager, String adminEmail) {
        Admin admin = adminDao.findByEmail(adminEmail)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with email: " + adminEmail));

        HRManager existing = hrManagerDao.findByIdAndCompanyId(id, admin.getCompany().getId())
                .orElseThrow(() -> new UserNotFoundException("HR Manager not found or doesn't belong to your company"));

        existing.setUsername(hrManager.getUsername());
        existing.setEmail(hrManager.getEmail());
        existing.setPassword(passwordEncoder.encode(hrManager.getPassword()));

        return hrManagerDao.save(existing);
    }

    @Override
    public HRManager getHRManagerByIdAndAdminCompany(Long id, String adminEmail) {
        Admin admin = adminDao.findByEmail(adminEmail)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with email: " + adminEmail));

        return hrManagerDao.findByIdAndCompanyId(id, admin.getCompany().getId())
                .orElseThrow(() -> new UserNotFoundException(
                        "HR Manager not found with id: " + id + " or doesn't belong to your company"));
    }

    @Override
    public void deleteHRManagerByAdmin(Long id, String adminEmail) {
        Admin admin = adminDao.findByEmail(adminEmail)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with email: " + adminEmail));

        HRManager hrManager = hrManagerDao.findByIdAndCompanyId(id, admin.getCompany().getId())
                .orElseThrow(() -> new UserNotFoundException("HR Manager not found or doesn't belong to your company"));

        hrManagerDao.delete(hrManager);
    }

    @Override
    public void deleteHRManager(Long id) {
        if (!hrManagerDao.existsById(id)) {
            throw new UserNotFoundException("HR Manager not found with id: " + id);
        }
        hrManagerDao.deleteById(id);
    }
}