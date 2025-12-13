package org.acme.PGDB.Mappers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.PGDB.DTOs.GameGenreDTO;
import org.acme.PGDB.Entities.GamesGenres;
import org.acme.PGDB.Entities.GenreEnum;
import org.acme.PGDB.Repositories.GameRepository;
import org.acme.PGDB.Repositories.GenreRepository;

@ApplicationScoped
public class GameGenreMapper {
    @Inject
    GenreMapper genreMapper;
    @Inject
    GameRepository gameRepository;
    @Inject
    GenreRepository genreRepository;

    public GameGenreDTO toDTO(GamesGenres entity) {
        GameGenreDTO dto = new GameGenreDTO();
        dto.setId(entity.getId());
        dto.setGameId(entity.getGame().getId());
        dto.setGenre(genreMapper.toDTO(entity.getGenre()));
        return dto;
    }

    public GamesGenres toEntity(GameGenreDTO dto) {
        GamesGenres entity = new GamesGenres();
        if (dto.getGameId() != null) {
            entity.setGame(gameRepository.findById(dto.getGameId()));
        }
        if (dto.getGenre() != null) {
            GenreEnum genreEnum = GenreEnum.valueOf(dto.getGenre().getName().toUpperCase());
            entity.setGenre(genreRepository.find("name", genreEnum).firstResult());
        }
        return entity;
    }
}
