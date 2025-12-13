package org.acme.PGDB.Mappers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.PGDB.DTOs.UserDTO;
import org.acme.PGDB.Entities.User;

import java.util.stream.Collectors;

@ApplicationScoped
public class UserMapper {
    @Inject
    UserGameListEntryMapper entryMapper;

    public UserDTO toDTO(User entity) {
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setDisplayName(entity.getDisplayName());
        dto.setBio(entity.getBio());
        dto.setAvatarUrl(entity.getAvatarUrl());
        dto.setUserGameListEntries(entity.getUserGameListEntries().stream()
                .map(entryMapper::toDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    public User toEntity(UserDTO dto) {
        User entity = new User();
        entity.setDisplayName(dto.getDisplayName());
        entity.setBio(dto.getBio());
        entity.setAvatarUrl(dto.getAvatarUrl());

        return entity;
    }
}
