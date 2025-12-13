package org.acme.Services;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.PGDB.DTOs.GenreDTO;
import org.acme.PGDB.Entities.Genre;
import org.acme.PGDB.Mappers.GenreMapper;
import org.acme.PGDB.Repositories.GenreRepository;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class GenreService {

    @Inject
    GenreMapper genreMapper;

    @Inject
    GenreRepository genreRepository;

    @Transactional
    public GenreDTO create(GenreDTO dto) {
        Genre entity = genreMapper.toEntity(dto);
        genreRepository.persist(entity);
        return genreMapper.toDTO(entity);
    }

    public List<GenreDTO> getAll() {
        return genreRepository.listAll().stream()
                .map(genreMapper::toDTO)
                .collect(Collectors.toList());
    }
}
