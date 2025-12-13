package org.acme.PGDB.Mappers;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.PGDB.DTOs.PublisherDTO;
import org.acme.PGDB.Entities.Publisher;

@ApplicationScoped
public class PublisherMapper {
    public PublisherDTO toDTO(Publisher entity) {
        PublisherDTO dto = new PublisherDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }

    public Publisher toEntity(PublisherDTO dto) {
        Publisher entity = new Publisher();
        entity.setName(dto.getName());
        return entity;
    }
}