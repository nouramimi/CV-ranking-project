package com.example.cvfilter.controller;

import com.example.cvfilter.dto.CreateHRManagerDto;
import com.example.cvfilter.dto.HRManagerResponseDto;
import com.example.cvfilter.dto.UpdateHRManagerDto;
import com.example.cvfilter.service.HRManagerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr-managers")
public class HRManagerController {

    private final HRManagerService hrManagerService;

    public HRManagerController(HRManagerService hrManagerService) {
        this.hrManagerService = hrManagerService;
    }

    @PostMapping
    public ResponseEntity<HRManagerResponseDto> createHRManager(
            @Valid @RequestBody CreateHRManagerDto createDto,
            Authentication authentication) {

        HRManagerResponseDto created = hrManagerService.createHRManagerByAdmin(
                createDto,
                authentication.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<HRManagerResponseDto>> getAllHRManagers(Authentication authentication) {
        List<HRManagerResponseDto> hrManagers = hrManagerService.getAllHRManagersByAdminCompany(
                authentication.getName()
        );

        return ResponseEntity.ok(hrManagers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HRManagerResponseDto> getHRManagerById(
            @PathVariable Long id,
            Authentication authentication) {

        HRManagerResponseDto hrManager = hrManagerService.getHRManagerByIdAndAdminCompany(
                id,
                authentication.getName()
        );

        return ResponseEntity.ok(hrManager);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HRManagerResponseDto> updateHRManager(
            @PathVariable Long id,
            @Valid @RequestBody UpdateHRManagerDto updateDto,
            Authentication authentication) {

        HRManagerResponseDto updated = hrManagerService.updateHRManagerByAdmin(
                id,
                updateDto,
                authentication.getName()
        );

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHRManager(
            @PathVariable Long id,
            Authentication authentication) {

        hrManagerService.deleteHRManagerByAdmin(id, authentication.getName());

        return ResponseEntity.noContent().build();
    }
}