package org.acme.Misc;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.PGDB.Entities.Genre;
import org.acme.PGDB.Entities.GenreEnum;
import org.acme.PGDB.Entities.Platform;
import org.acme.PGDB.Entities.PlatformEnum;
import org.acme.PGDB.Repositories.GenreRepository;
import org.acme.PGDB.Repositories.PlatformRepository;

@ApplicationScoped
public class DataInitializer {

    @Inject
    GenreRepository genreRepository;

    @Inject
    PlatformRepository platformRepository;

    @Transactional
    public void init(@Observes StartupEvent e) {
        // Insert GenreEnum values
        if (Genre.count() == 0) {
            for (GenreEnum genreEnum : GenreEnum.values()) {
                Genre genre = new Genre();
                genre.setName(genreEnum);
                genreRepository.persist(genre);
            }
        }

        // Insert PlatformEnum values
        if (Platform.count() == 0) {
            for (PlatformEnum platformEnum : PlatformEnum.values()) {
                Platform platform = new Platform();
                platform.setName(platformEnum);
                platformRepository.persist(platform);
            }
        }
    }
}
