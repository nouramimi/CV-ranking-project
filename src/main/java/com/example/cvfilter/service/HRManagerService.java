package com.example.cvfilter.service;

import com.example.cvfilter.dao.HRManagerDao;
import com.example.cvfilter.dao.AdminDao;
import com.example.cvfilter.dao.entity.HRManager;
import com.example.cvfilter.dao.entity.Admin;
import com.example.cvfilter.dao.entity.Role;
import com.example.cvfilter.dto.CreateHRManagerDto;
import com.example.cvfilter.dto.HRManagerResponseDto;
import com.example.cvfilter.dto.UpdateHRManagerDto;
import com.example.cvfilter.exception.UserNotFoundException;
import com.example.cvfilter.mapper.HRManagerMapper;
import com.example.cvfilter.service.impl.HRManagerServiceInterface;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HRManagerService implements HRManagerServiceInterface {

    private final HRManagerDao hrManagerDao;
    private final AdminDao adminDao;
    private final PasswordEncoder passwordEncoder;
    private final HRManagerMapper hrManagerMapper;

    public HRManagerService(HRManagerDao hrManagerDao,
                            AdminDao adminDao,
                            PasswordEncoder passwordEncoder,
                            HRManagerMapper hrManagerMapper) {
        this.hrManagerDao = hrManagerDao;
        this.adminDao = adminDao;
        this.passwordEncoder = passwordEncoder;
        this.hrManagerMapper = hrManagerMapper;
    }

    // === NOUVELLES MÉTHODES UTILISANT LES DTOs ===

    @Override
    public HRManagerResponseDto createHRManagerByAdmin(CreateHRManagerDto createDto, String adminEmail) {
        Admin admin = adminDao.findByEmail(adminEmail)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with email: " + adminEmail));

        HRManager hrManager = hrManagerMapper.createDtoToEntity(createDto);
        hrManager.setCompany(admin.getCompany());
        hrManager.setRole(Role.HR_MANAGER);
        hrManager.setPassword(passwordEncoder.encode(createDto.getPassword()));

        HRManager savedHRManager = hrManagerDao.save(hrManager);
        return hrManagerMapper.entityToResponseDto(savedHRManager);
    }

    @Override
    public List<HRManagerResponseDto> getAllHRManagersByAdminCompany(String adminEmail) {
        Admin admin = adminDao.findByEmail(adminEmail)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with email: " + adminEmail));

        List<HRManager> hrManagers = hrManagerDao.findByCompanyId(admin.getCompany().getId());
        return hrManagerMapper.entitiesToResponseDtos(hrManagers);
    }

    @Override
    public HRManagerResponseDto getHRManagerByIdAndAdminCompany(Long id, String adminEmail) {
        Admin admin = adminDao.findByEmail(adminEmail)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with email: " + adminEmail));

        HRManager hrManager = hrManagerDao.findByIdAndCompanyId(id, admin.getCompany().getId())
                .orElseThrow(() -> new UserNotFoundException(
                        "HR Manager not found with id: " + id + " or doesn't belong to your company"));

        return hrManagerMapper.entityToResponseDto(hrManager);
    }

    @Override
    public HRManagerResponseDto updateHRManagerByAdmin(Long id, UpdateHRManagerDto updateDto, String adminEmail) {
        Admin admin = adminDao.findByEmail(adminEmail)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with email: " + adminEmail));

        HRManager existing = hrManagerDao.findByIdAndCompanyId(id, admin.getCompany().getId())
                .orElseThrow(() -> new UserNotFoundException("HR Manager not found or doesn't belong to your company"));

        // Utiliser MapStruct pour mettre à jour
        hrManagerMapper.updateEntityFromDto(updateDto, existing);

        // Encoder le mot de passe si fourni
        if (updateDto.getPassword() != null && !updateDto.getPassword().trim().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        HRManager savedHRManager = hrManagerDao.save(existing);
        return hrManagerMapper.entityToResponseDto(savedHRManager);
    }

    @Override
    public void deleteHRManagerByAdmin(Long id, String adminEmail) {
        Admin admin = adminDao.findByEmail(adminEmail)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with email: " + adminEmail));

        HRManager hrManager = hrManagerDao.findByIdAndCompanyId(id, admin.getCompany().getId())
                .orElseThrow(() -> new UserNotFoundException("HR Manager not found or doesn't belong to your company"));

        hrManagerDao.delete(hrManager);
    }

    // === MÉTHODES HÉRITÉES POUR COMPATIBILITÉ ===

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
    public void deleteHRManager(Long id) {
        if (!hrManagerDao.existsById(id)) {
            throw new UserNotFoundException("HR Manager not found with id: " + id);
        }
        hrManagerDao.deleteById(id);
    }
}