package com.nexus.onebook.education.controller;

import com.nexus.onebook.education.dto.ProgramDto;
import com.nexus.onebook.education.service.ProgramService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for education programs.
 */
@RestController
@RequestMapping("/api/education/programs")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @GetMapping
    public ResponseEntity<List<ProgramDto>> listPrograms(@RequestParam String tenantId) {
        return ResponseEntity.ok(programService.listPrograms(tenantId));
    }

    @PostMapping
    public ResponseEntity<ProgramDto> createProgram(
            @RequestParam String tenantId,
            @RequestParam @NotBlank String name,
            @RequestParam @NotBlank String code) {
        ProgramDto created = programService.createProgram(name, code, tenantId);
        return ResponseEntity.status(201).body(created);
    }
}
