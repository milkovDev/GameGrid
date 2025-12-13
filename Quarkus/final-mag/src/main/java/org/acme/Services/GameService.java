package org.acme.Services;

import io.quarkus.hibernate.orm.panache.Panache;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.PGDB.DTOs.GameDTO;
import org.acme.PGDB.DTOs.GameGenreDTO;
import org.acme.PGDB.DTOs.GamePlatformDTO;
import org.acme.PGDB.Entities.*;
import org.acme.PGDB.Mappers.*;
import org.acme.PGDB.Repositories.GameRepository;
import org.acme.PGDB.Repositories.GamesGenresRepository;
import org.acme.PGDB.Repositories.GamesPlatformsRepository;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class GameService {

    @Inject
    GameMapper gameMapper;

    @Inject
    GameRepository gameRepository;

    @Inject
    GamesGenresRepository gamesGenresRepository;

    @Inject
    GamesPlatformsRepository gamesPlatformsRepository;

    @Inject
    GamePlatformMapper gamePlatformMapper;

    @Inject
    GameGenreMapper gameGenreMapper;

    @Transactional
    public GameDTO create(GameDTO dto) {
        Game game = gameMapper.toEntity(dto);

        gameRepository.persist(game);

        game.setGamesGenres(new ArrayList<>());
        game.setGamesPlatforms(new ArrayList<>());

        List<GameGenreDTO> gameGenreDTOS = dto.getGameGenres() != null ? dto.getGameGenres() : new ArrayList<>();
        for (GameGenreDTO gameGenreDTO : gameGenreDTOS) {
            gameGenreDTO.setGameId(game.getId());
            GamesGenres gg = gameGenreMapper.toEntity(gameGenreDTO);
            gamesGenresRepository.persist(gg);

            game.getGamesGenres().add(gg);
        }

        List<GamePlatformDTO> gamePlatformDTOS = dto.getGamePlatforms() != null ? dto.getGamePlatforms() : new ArrayList<>();
        for (GamePlatformDTO gamePlatformDTO : gamePlatformDTOS) {
            gamePlatformDTO.setGameId(game.getId());
            GamesPlatforms gp = gamePlatformMapper.toEntity(gamePlatformDTO);
            gamesPlatformsRepository.persist(gp);

            game.getGamesPlatforms().add(gp);
        }

        return gameMapper.toDTO(game);
    }

    @Transactional
    public GameDTO update(GameDTO dto) {
        Game game = gameRepository.findById(dto.getId());

        game.setTitle(dto.getTitle());
        game.setDescription(dto.getDescription());
        game.setReleaseDate(dto.getReleaseDate());
        game.setCoverUrl(dto.getCoverUrl());

        // Handle genres
        List<GamesGenres> existingGameGenres = game.getGamesGenres();
        List<GameGenreDTO> newGameGenres = dto.getGameGenres() != null ? dto.getGameGenres() : new ArrayList<>();

        Set<Long> newGameGenreIds = newGameGenres.stream()
                .map(GameGenreDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Delete removed genre associations
        List<GamesGenres> gameGenresToDelete = existingGameGenres.stream()
                .filter(gg -> !newGameGenreIds.contains(gg.getId()))
                .toList();

        for (GamesGenres gg : gameGenresToDelete) {
            game.getGamesGenres().remove(gg);
            gg.getGenre().getGamesGenres().remove(gg);
            gamesGenresRepository.delete(gg);
        }

        for (GameGenreDTO gameGenreDTO : newGameGenres) {
            if (gameGenreDTO.getId() == null) {
                gameGenreDTO.setGameId(game.getId());
                GamesGenres gg = gameGenreMapper.toEntity(gameGenreDTO);
                gamesGenresRepository.persist(gg);

                game.getGamesGenres().add(gg);
            }
        }

        List<GamesPlatforms> existingGamePlatforms = game.getGamesPlatforms();
        List<GamePlatformDTO> newGamePlatforms = dto.getGamePlatforms() != null ? dto.getGamePlatforms() : new ArrayList<>();

        Set<Long> newGamePlatformIds = newGamePlatforms.stream()
                .map(GamePlatformDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Delete removed platform associations
        List<GamesPlatforms> gamePlatformsToDelete = existingGamePlatforms.stream()
                .filter(gp -> !newGamePlatformIds.contains(gp.getId()))
                .toList();
        for (GamesPlatforms gp : gamePlatformsToDelete) {
            game.getGamesPlatforms().remove(gp);
            gp.getPlatform().getGamesPlatforms().remove(gp);
            gamesPlatformsRepository.delete(gp);
        }

        for (GamePlatformDTO gamePlatformDTO : newGamePlatforms) {
            if (gamePlatformDTO.getId() == null) {
                gamePlatformDTO.setGameId(game.getId());
                GamesPlatforms gp = gamePlatformMapper.toEntity(gamePlatformDTO);
                gamesPlatformsRepository.persist(gp);

                game.getGamesPlatforms().add(gp);
            }
        }

        Panache.getEntityManager().flush(); // Ensure changes are flushed

        return gameMapper.toDTO(game);
    }

    public List<GameDTO> getAll() {
        return gameRepository.listAll().stream()
                .map(gameMapper::toDTO)
                .collect(Collectors.toList());
    }
}
