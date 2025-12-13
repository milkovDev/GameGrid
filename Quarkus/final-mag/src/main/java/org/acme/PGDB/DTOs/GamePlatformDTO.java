package org.acme.PGDB.DTOs;

public class GamePlatformDTO {
    private Long id;
    private Long gameId;
    private PlatformDTO platform;

    public GamePlatformDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public PlatformDTO getPlatform() {
        return platform;
    }

    public void setPlatform(PlatformDTO platform) {
        this.platform = platform;
    }
}
