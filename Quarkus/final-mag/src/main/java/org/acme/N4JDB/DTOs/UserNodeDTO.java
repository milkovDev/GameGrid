package org.acme.N4JDB.DTOs;

import java.util.Set;
import java.util.UUID;

public class UserNodeDTO {
    private Long id;
    private String userId;
    private String displayName;
    private Set<String> following;
    private Set<String> followers;

    // Default Constructor
    public UserNodeDTO() {}

    // Getters and Setters
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Set<String> getFollowing() {
        return following;
    }

    public void setFollowing(Set<String> following) {
        this.following = following;
    }

    public Set<String> getFollowers() {
        return followers;
    }

    public void setFollowers(Set<String> followers) {
        this.followers = followers;
    }

}