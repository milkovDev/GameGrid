package org.acme.PGDB.Mappers;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.PGDB.DTOs.PlatformDTO;
import org.acme.PGDB.Entities.Platform;
import org.acme.PGDB.Entities.PlatformEnum;

@ApplicationScoped
public class PlatformMapper {
    public PlatformDTO toDTO(Platform entity) {
        PlatformDTO dto = new PlatformDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName().name());
        return dto;
    }

    public Platform toEntity(PlatformDTO dto) {
        Platform entity = new Platform();
        entity.setName(PlatformEnum.valueOf(dto.getName()));
        return entity;
    }
}
