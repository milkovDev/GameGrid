package org.acme.Services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.PGDB.DTOs.PlatformDTO;
import org.acme.PGDB.Entities.Genre;
import org.acme.PGDB.Entities.Platform;
import org.acme.PGDB.Mappers.PlatformMapper;
import org.acme.PGDB.Repositories.PlatformRepository;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PlatformService {

    @Inject
    PlatformMapper platformMapper;

    @Inject
    PlatformRepository platformRepository;

    @Transactional
    public PlatformDTO create(PlatformDTO dto) {
        Platform entity = platformMapper.toEntity(dto);
        platformRepository.persist(entity);
        return platformMapper.toDTO(entity);
    }

    public List<PlatformDTO> getAll() {
        return platformRepository.listAll().stream()
                .map(platformMapper::toDTO)
                .collect(Collectors.toList());
    }
}
