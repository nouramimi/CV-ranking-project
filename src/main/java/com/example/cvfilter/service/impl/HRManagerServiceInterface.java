package com.example.cvfilter.service.impl;

import com.example.cvfilter.dao.entity.HRManager;
import com.example.cvfilter.dto.CreateHRManagerDto;
import com.example.cvfilter.dto.HRManagerResponseDto;
import com.example.cvfilter.dto.UpdateHRManagerDto;

import java.util.List;

public interface HRManagerServiceInterface {

    HRManagerResponseDto createHRManagerByAdmin(CreateHRManagerDto createDto, String adminEmail);
    List<HRManagerResponseDto> getAllHRManagersByAdminCompany(String adminEmail);
    HRManagerResponseDto getHRManagerByIdAndAdminCompany(Long id, String adminEmail);
    HRManagerResponseDto updateHRManagerByAdmin(Long id, UpdateHRManagerDto updateDto, String adminEmail);
    void deleteHRManagerByAdmin(Long id, String adminEmail);

    HRManager createHRManager(HRManager hrManager);
    HRManager createHRManagerByAdmin(HRManager hrManager, String adminEmail);
    List<HRManager> getAllHRManagers();
    HRManager getHRManagerById(Long id);
    HRManager updateHRManager(Long id, HRManager hrManager);
    void deleteHRManager(Long id);
}