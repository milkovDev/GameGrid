package org.acme.Services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.PGDB.DTOs.DeveloperDTO;
import org.acme.PGDB.Entities.Developer;
import org.acme.PGDB.Mappers.DeveloperMapper;
import org.acme.PGDB.Repositories.DeveloperRepository;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class DeveloperService {

    @Inject
    DeveloperMapper developerMapper;

    @Inject
    DeveloperRepository developerRepository;

    @Transactional
    public DeveloperDTO create(DeveloperDTO dto) {
        Developer entity = developerMapper.toEntity(dto);
        developerRepository.persist(entity);
        return developerMapper.toDTO(entity);
    }

    public List<DeveloperDTO> getAll() {
        return developerRepository.listAll().stream()
                .map(developerMapper::toDTO)
                .collect(Collectors.toList());
    }
}
