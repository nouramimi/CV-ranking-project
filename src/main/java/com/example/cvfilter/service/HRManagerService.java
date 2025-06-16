package com.example.cvfilter.service;

import com.example.cvfilter.dao.HRManagerDao;
import com.example.cvfilter.dao.entity.HRManager;
import com.example.cvfilter.exception.UserNotFoundException;
import com.example.cvfilter.service.impl.HRManagerServiceInterface;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HRManagerService implements HRManagerServiceInterface {

    private final HRManagerDao hrManagerDao;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public HRManagerService(HRManagerDao hrManagerDao) {
        this.hrManagerDao = hrManagerDao;
    }

    @Override
    public HRManager createHRManager(HRManager hrManager) {
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
