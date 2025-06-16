package com.example.cvfilter.dao;

import com.example.cvfilter.dao.entity.HRManager;

import java.util.List;
import java.util.Optional;

public interface HRManagerDao {
    HRManager save(HRManager hrManager);
    Optional<HRManager> findById(Long id);
    List<HRManager> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
}
