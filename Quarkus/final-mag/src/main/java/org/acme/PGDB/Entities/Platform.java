package org.acme.PGDB.Entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "platforms")
public class Platform extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "platformSeq")
    @SequenceGenerator(name = "platformSeq", sequenceName = "platform_seq", allocationSize = 1)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false, unique = true)
    private PlatformEnum name;

    @OneToMany(mappedBy = "platform", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GamesPlatforms> gamesPlatforms;

    // Public empty constructor
    public Platform() {}

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PlatformEnum getName() {
        return name;
    }

    public void setName(PlatformEnum name) {
        this.name = name;
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
        return "Platform{" +
                "id=" + id +
                ", name=" + name +
                ", gamesPlatforms=" + gamesPlatforms +
                '}';
    }
}
