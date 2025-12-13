package org.acme.PGDB.Mappers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.PGDB.DTOs.GameDTO;
import org.acme.PGDB.Entities.Developer;
import org.acme.PGDB.Entities.Game;
import org.acme.PGDB.Entities.Publisher;
import org.acme.PGDB.Repositories.DeveloperRepository;
import org.acme.PGDB.Repositories.PublisherRepository;

import java.util.stream.Collectors;

@ApplicationScoped
public class GameMapper {
    @Inject
    DeveloperMapper developerMapper;
    @Inject PublisherMapper publisherMapper;
    @Inject GameGenreMapper gamesGenreMapper;
    @Inject GamePlatformMapper gamesPlatformMapper;
    @Inject DeveloperRepository developerRepository;
    @Inject PublisherRepository publisherRepository;

    public GameDTO toDTO(Game entity) {
        GameDTO dto = new GameDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setReleaseDate(entity.getReleaseDate());
        dto.setCoverUrl(entity.getCoverUrl());
        dto.setDeveloper(developerMapper.toDTO(entity.getDeveloper()));
        dto.setPublisher(publisherMapper.toDTO(entity.getPublisher()));
        dto.setGameGenres(entity.getGamesGenres().stream()
                .map(gamesGenreMapper::toDTO)
                .collect(Collectors.toList()));
        dto.setGamePlatforms(entity.getGamesPlatforms().stream()
                .map(gamesPlatformMapper::toDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    public Game toEntity(GameDTO dto) {
        Game entity = new Game();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setReleaseDate(dto.getReleaseDate());
        entity.setCoverUrl(dto.getCoverUrl());

        Developer developer = developerRepository.findById(dto.getDeveloper().getId());
        Publisher publisher = publisherRepository.findById(dto.getPublisher().getId());

        entity.setDeveloper(developer);
        entity.setPublisher(publisher);

        return entity;
    }
}
