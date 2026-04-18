package com.nexus.onebook.education.service;

import com.nexus.onebook.education.dto.ProgramDto;
import com.nexus.onebook.education.model.EducationProgram;
import com.nexus.onebook.education.repository.EducationProgramRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing education programs.
 */
@Service
public class ProgramService {

    private final EducationProgramRepository programRepository;

    public ProgramService(EducationProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    @Transactional(readOnly = true)
    public List<ProgramDto> listPrograms(String tenantId) {
        return programRepository.findAllByTenantIdAndIsActiveTrue(tenantId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProgramDto createProgram(String name, String code, String tenantId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Program name must not be blank");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Program code must not be blank");
        }
        EducationProgram program = new EducationProgram(tenantId, name, code);
        EducationProgram saved = programRepository.save(program);
        return toDto(saved);
    }

    private ProgramDto toDto(EducationProgram program) {
        return new ProgramDto(
                program.getId(),
                program.getName(),
                program.getCode(),
                program.isActive()
        );
    }
}
