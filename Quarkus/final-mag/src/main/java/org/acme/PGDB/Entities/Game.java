package org.acme.PGDB.Entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "games")
public class Game extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gameSeq")
    @SequenceGenerator(name = "gameSeq", sequenceName = "game_seq", allocationSize = 1)
    private Long id;

    @Column(length = 255, nullable = false, unique = true)
    private String title;

    @Column(length = 1100, nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate releaseDate;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "developer_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Developer developer;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "publisher_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Publisher publisher;

    @Column(length = 255, nullable = false)
    private String coverUrl;

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserGameListEntry> userGameListEntries;

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GamesGenres> gamesGenres;

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GamesPlatforms> gamesPlatforms;

    // Public empty constructor
    public Game() {}

    // Getters and setters
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

    public Developer getDeveloper() {
        return developer;
    }

    public void setDeveloper(Developer developer) {
        this.developer = developer;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public List<UserGameListEntry> getUserGameListEntries() {
        return userGameListEntries;
    }

    public void setUserGameListEntries(List<UserGameListEntry> userGameLists) {
        this.userGameListEntries = userGameLists;
    }

    public List<GamesGenres> getGamesGenres() {
        return gamesGenres;
    }

    public void setGamesGenres(List<GamesGenres> gamesGenres) {
        this.gamesGenres = gamesGenres;
    }

    public List<GamesPlatforms> getGamesPlatforms() {
        return gamesPlatforms;
    }

    public void setGamesPlatforms(List<GamesPlatforms> gamesPlatforms) {
        this.gamesPlatforms = gamesPlatforms;
    }

    // toString method
    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", releaseDate=" + releaseDate +
                ", developer=" + developer +
                ", publisher=" + publisher +
                ", coverUrl='" + coverUrl + '\'' +
                ", userGameLists=" + userGameListEntries +
                ", gamesGenres=" + gamesGenres +
                ", gamesPlatforms=" + gamesPlatforms +
                '}';
    }
}
