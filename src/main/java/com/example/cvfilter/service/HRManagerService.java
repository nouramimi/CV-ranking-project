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
    public HRManager createHRManagerByAdmin(HRManager hrManager, String adminIdentifier) {
        Admin admin = adminDao.findByUsername(adminIdentifier)
                .orElse(adminDao.findByEmail(adminIdentifier)
                        .orElseThrow(() -> new UserNotFoundException("Admin not found with identifier: " + adminIdentifier)));

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
    public void deleteHRManager(Long id) {
        if (!hrManagerDao.existsById(id)) {
            throw new UserNotFoundException("HR Manager not found with id: " + id);
        }
        hrManagerDao.deleteById(id);
    }
}