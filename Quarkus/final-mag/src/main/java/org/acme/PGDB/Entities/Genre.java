package org.acme.PGDB.Entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "genres")
public class Genre extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "genreSeq")
    @SequenceGenerator(name = "genreSeq", sequenceName = "genre_seq", allocationSize = 1)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false, unique = true)
    private GenreEnum name;

    @OneToMany(mappedBy = "genre", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GamesGenres> gamesGenres;

    // Public empty constructor
    public Genre() {}

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GenreEnum getName() {
        return name;
    }

    public void setName(GenreEnum name) {
        this.name = name;
    }

    public List<GamesGenres> getGamesGenres() {
        return gamesGenres;
    }

    public void setGamesGenres(List<GamesGenres> gamesGenres) {
        this.gamesGenres = gamesGenres;
    }

    // toString method
    @Override
    public String toString() {
        return "Genre{" +
                "id=" + id +
                ", name=" + name +
                ", gamesGenres=" + gamesGenres +
                '}';
    }
}
