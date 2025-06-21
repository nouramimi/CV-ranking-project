package com.example.cvfilter.controller;

import com.example.cvfilter.dao.entity.HRManager;
import com.example.cvfilter.service.impl.HRManagerServiceInterface;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr-managers")
public class HRManagerController {

    private final HRManagerServiceInterface hrManagerService;

    public HRManagerController(HRManagerServiceInterface hrManagerService) {
        this.hrManagerService = hrManagerService;
    }

    @PostMapping
    public ResponseEntity<HRManager> createHRManager(@RequestBody HRManager hrManager, Authentication authentication) {
        HRManager created = hrManagerService.createHRManagerByAdmin(hrManager, authentication.getName());
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<HRManager>> getAllHRManagers() {
        return ResponseEntity.ok(hrManagerService.getAllHRManagers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HRManager> getHRManagerById(@PathVariable Long id) {
        return ResponseEntity.ok(hrManagerService.getHRManagerById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HRManager> updateHRManager(@PathVariable Long id, @RequestBody HRManager hrManager) {
        HRManager updated = hrManagerService.updateHRManager(id, hrManager);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHRManager(@PathVariable Long id) {
        hrManagerService.deleteHRManager(id);
        return ResponseEntity.noContent().build();
    }
}