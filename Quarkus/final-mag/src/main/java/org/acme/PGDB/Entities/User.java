package org.acme.PGDB.Entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User extends PanacheEntityBase {
    @Id
    private UUID id;

    @Column(length = 50, nullable = false)
    private String displayName;

    @Column(length = 1100, nullable = true)
    private String bio;

    @Column(length = 255, nullable = true)
    private String avatarUrl;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserGameListEntry> userGameListEntries;

    @OneToMany(mappedBy = "author", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Article> articles;

    // Public empty constructor
    public User() {}

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String username) {
        this.displayName = username;
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

    public List<UserGameListEntry> getUserGameListEntries() {
        return userGameListEntries;
    }

    public void setUserGameListEntries(List<UserGameListEntry> userGameLists) {
        this.userGameListEntries = userGameLists;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    // toString method
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + displayName + '\'' +
                ", bio='" + bio + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", userGameLists=" + userGameListEntries +
                ", articles=" + articles +
                '}';
    }
}
