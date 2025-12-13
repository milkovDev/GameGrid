package org.acme.PGDB.Mappers;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.PGDB.DTOs.GenreDTO;
import org.acme.PGDB.Entities.Genre;
import org.acme.PGDB.Entities.GenreEnum;

@ApplicationScoped
public class GenreMapper {
    public GenreDTO toDTO(Genre entity) {
        GenreDTO dto = new GenreDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName().name());
        return dto;
    }

    public Genre toEntity(GenreDTO dto) {
        Genre entity = new Genre();
        entity.setName(GenreEnum.valueOf(dto.getName()));
        return entity;
    }
}
