package org.acme.PGDB.DTOs;

import java.util.List;
import java.util.UUID;

public class UserDTO {
    private UUID id;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private List<UserGameListEntryDTO> userGameListEntries;

    public UserDTO() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public List<UserGameListEntryDTO> getUserGameListEntries() {
        return userGameListEntries;
    }

    public void setUserGameListEntries(List<UserGameListEntryDTO> userGameListEntries) {
        this.userGameListEntries = userGameListEntries;
    }
}
