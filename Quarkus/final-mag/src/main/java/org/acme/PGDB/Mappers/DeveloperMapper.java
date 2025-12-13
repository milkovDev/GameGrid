package org.acme.PGDB.Mappers;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.PGDB.DTOs.DeveloperDTO;
import org.acme.PGDB.Entities.Developer;

@ApplicationScoped
public class DeveloperMapper {
    public DeveloperDTO toDTO(Developer entity) {
        DeveloperDTO dto = new DeveloperDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }

    public Developer toEntity(DeveloperDTO dto) {
        Developer entity = new Developer();
        entity.setName(dto.getName());
        return entity;
    }
}
