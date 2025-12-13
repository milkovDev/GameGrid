package org.acme.PGDB.DTOs;

import java.time.LocalDate;
import java.util.List;

public class GameDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate releaseDate;
    private String coverUrl;
    private DeveloperDTO developer;
    private PublisherDTO publisher;
    private List<GameGenreDTO> gameGenres;
    private List<GamePlatformDTO> gamePlatforms;

    public GameDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public DeveloperDTO getDeveloper() {
        return developer;
    }

    public void setDeveloper(DeveloperDTO developer) {
        this.developer = developer;
    }

    public PublisherDTO getPublisher() {
        return publisher;
    }

    public void setPublisher(PublisherDTO publisher) {
        this.publisher = publisher;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public List<GameGenreDTO> getGameGenres() {
        return gameGenres;
    }

    public void setGameGenres(List<GameGenreDTO> gameGenres) {
        this.gameGenres = gameGenres;
    }

    public List<GamePlatformDTO> getGamePlatforms() {
        return gamePlatforms;
    }

    public void setGamePlatforms(List<GamePlatformDTO> gamePlatforms) {
        this.gamePlatforms = gamePlatforms;
    }
}
