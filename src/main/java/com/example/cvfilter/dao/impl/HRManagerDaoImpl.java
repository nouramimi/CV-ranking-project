package com.example.cvfilter.dao.impl;

import com.example.cvfilter.dao.HRManagerDao;
import com.example.cvfilter.dao.entity.HRManager;
import com.example.cvfilter.dao.repository.HRManagerRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class HRManagerDaoImpl implements HRManagerDao {

    private final HRManagerRepository hrManagerRepository;

    public HRManagerDaoImpl(HRManagerRepository hrManagerRepository) {
        this.hrManagerRepository = hrManagerRepository;
    }

    @Override
    public HRManager save(HRManager hrManager) {
        return hrManagerRepository.save(hrManager);
    }

    @Override
    public Optional<HRManager> findById(Long id) {
        return hrManagerRepository.findById(id);
    }

    @Override
    public List<HRManager> findAll() {
        return hrManagerRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        hrManagerRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return hrManagerRepository.existsById(id);
    }
}
