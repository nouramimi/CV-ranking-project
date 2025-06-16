package com.example.cvfilter.service.impl;

import com.example.cvfilter.dao.entity.HRManager;

import java.util.List;

public interface HRManagerServiceInterface {
    HRManager createHRManager(HRManager hrManager);
    List<HRManager> getAllHRManagers();
    HRManager getHRManagerById(Long id);
    HRManager updateHRManager(Long id, HRManager hrManager);
    void deleteHRManager(Long id);
}
