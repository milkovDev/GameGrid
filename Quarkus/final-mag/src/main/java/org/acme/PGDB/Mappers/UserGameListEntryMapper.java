package org.acme.PGDB.Mappers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.PGDB.DTOs.UserGameListEntryDTO;
import org.acme.PGDB.Entities.StatusEnum;
import org.acme.PGDB.Entities.UserGameListEntry;
import org.acme.PGDB.Repositories.UserRepository;

@ApplicationScoped
public class UserGameListEntryMapper {
    @Inject
    GameMapper gameMapper;
    @Inject
    UserRepository userRepository;

    public UserGameListEntryDTO toDTO(UserGameListEntry entity) {
        UserGameListEntryDTO dto = new UserGameListEntryDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser().getId());
        dto.setGame(gameMapper.toDTO(entity.getGame()));
        dto.setStatus(entity.getStatus().name());
        dto.setIsFavorite(entity.getIsFavorite());
        dto.setRating(entity.getRating());
        dto.setReviewText(entity.getReviewText());
        return dto;
    }

    public UserGameListEntry toEntity(UserGameListEntryDTO dto) {
        UserGameListEntry entity = new UserGameListEntry();
        if (dto.getUserId() != null) {
            entity.setUser(userRepository.findById(dto.getUserId()));
        }
        if (dto.getGame() != null) {
            entity.setGame(gameMapper.toEntity(dto.getGame()));
        }
        if (dto.getStatus() != null) {
            entity.setStatus(StatusEnum.valueOf(dto.getStatus()));
        }
        entity.setIsFavorite(dto.getIsFavorite());
        entity.setRating(dto.getRating());
        entity.setReviewText(dto.getReviewText());
        return entity;
    }
}
