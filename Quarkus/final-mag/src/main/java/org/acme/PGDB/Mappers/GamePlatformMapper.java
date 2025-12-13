package org.acme.PGDB.Mappers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.PGDB.DTOs.GamePlatformDTO;
import org.acme.PGDB.Entities.GamesPlatforms;
import org.acme.PGDB.Entities.PlatformEnum;
import org.acme.PGDB.Repositories.GameRepository;
import org.acme.PGDB.Repositories.PlatformRepository;

@ApplicationScoped
public class GamePlatformMapper {
    @Inject
    PlatformMapper platformMapper;
    @Inject
    GameRepository gameRepository;
    @Inject
    PlatformRepository platformRepository;

    public GamePlatformDTO toDTO(GamesPlatforms entity) {
        GamePlatformDTO dto = new GamePlatformDTO();
        dto.setId(entity.getId());
        dto.setGameId(entity.getGame().getId());
        dto.setPlatform(platformMapper.toDTO(entity.getPlatform()));
        return dto;
    }

    public GamesPlatforms toEntity(GamePlatformDTO dto) {
        GamesPlatforms entity = new GamesPlatforms();
        if (dto.getGameId() != null) {
            entity.setGame(gameRepository.findById(dto.getGameId()));
        }
        if (dto.getPlatform() != null) {
            PlatformEnum platformEnum = PlatformEnum.valueOf(dto.getPlatform().getName().toUpperCase());
            entity.setPlatform(platformRepository.find("name", platformEnum).firstResult());
        }
        return entity;
    }
}
