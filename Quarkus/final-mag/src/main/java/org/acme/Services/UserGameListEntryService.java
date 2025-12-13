package org.acme.Services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.acme.PGDB.DTOs.UserGameListEntryDTO;
import org.acme.PGDB.Entities.Game;
import org.acme.PGDB.Entities.StatusEnum;
import org.acme.PGDB.Entities.User;
import org.acme.PGDB.Entities.UserGameListEntry;
import org.acme.PGDB.Mappers.UserGameListEntryMapper;
import org.acme.PGDB.Repositories.GameRepository;
import org.acme.PGDB.Repositories.UserGameListEntryRepository;
import org.acme.PGDB.Repositories.UserRepository;
import org.acme.WebSocket.NotificationManager;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserGameListEntryService {

    @Inject
    UserGameListEntryMapper userGameListEntryMapper;

    @Inject
    UserGameListEntryRepository userGameListEntryRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    GameRepository gameRepository;

    @Inject
    NotificationManager notificationManager;

    @Transactional
    public UserGameListEntryDTO create(UserGameListEntryDTO dto) {
        UserGameListEntry entry = new UserGameListEntry();

        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        User user = userRepository.findById(dto.getUserId());
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        entry.setUser(user);

        if (dto.getGame() == null || dto.getGame().getId() == null) {
            throw new IllegalArgumentException("Existing game ID is required");
        }
        Game game = gameRepository.findById(dto.getGame().getId());
        if (game == null) {
            throw new NotFoundException("Game not found");
        }
        entry.setGame(game);

        entry.setStatus(StatusEnum.valueOf(dto.getStatus()));
        entry.setIsFavorite(dto.getIsFavorite());
        entry.setRating(dto.getRating());
        entry.setReviewText(dto.getReviewText());

        userGameListEntryRepository.persist(entry);

        //push notifications to followers
        notificationManager.notifyGameAdded(user.getId().toString(), user.getDisplayName(), game.getTitle());

        return userGameListEntryMapper.toDTO(entry);
    }

    @Transactional
    public UserGameListEntryDTO update(UserGameListEntryDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("ID is required for update");
        }

        UserGameListEntry entry = userGameListEntryRepository.findById(dto.getId());
        if (entry == null) {
            throw new NotFoundException("UserGameListEntry not found");
        }

        if (dto.getStatus() != null) {
            entry.setStatus(StatusEnum.valueOf(dto.getStatus()));
        }
        entry.setIsFavorite(dto.getIsFavorite());
        entry.setRating(dto.getRating());
        entry.setReviewText(dto.getReviewText());

        userGameListEntryRepository.persist(entry);

        return userGameListEntryMapper.toDTO(entry);
    }

    public List<UserGameListEntryDTO> getEntriesForUser(UUID userId) {
        return userGameListEntryRepository.find("user.id", userId).stream()
                .map(userGameListEntryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long id) {
        UserGameListEntry entry = userGameListEntryRepository.findById(id);
        if (entry == null) {
            throw new NotFoundException("UserGameListEntry not found");
        }
        entry.getUser().getUserGameListEntries().remove(entry);
        entry.getGame().getUserGameListEntries().remove(entry);

        userGameListEntryRepository.delete(entry);
    }
}
