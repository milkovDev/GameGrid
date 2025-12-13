package org.acme.Validation;

import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.PGDB.Entities.Developer;
import org.acme.PGDB.Entities.Game;
import org.acme.PGDB.Entities.Publisher;
import org.acme.PGDB.Repositories.DeveloperRepository;
import org.acme.PGDB.Repositories.GameRepository;
import org.acme.PGDB.Repositories.PublisherRepository;

@ApplicationScoped
@Unremovable
public class DuplicateValidator {

    @Inject
    DeveloperRepository developerRepository;

    @Inject
    GameRepository gameRepository;

    @Inject
    PublisherRepository publisherRepository;

    public void checkDeveloperName(String name) {
        Developer existing = developerRepository.find("name", name).firstResult();
        if (existing != null) {
            throw new DuplicateException("Developer with name '" + name + "' already exists.");
        }
    }

    public void checkGameTitle(String title) {
        Game existing = gameRepository.find("title", title).firstResult();
        if (existing != null) {
            throw new DuplicateException("Game with title '" + title + "' already exists.");
        }
    }

    public void checkPublisherName(String name) {
        Publisher existing = publisherRepository.find("name", name).firstResult();
        if (existing != null) {
            throw new DuplicateException("Publisher with name '" + name + "' already exists.");
        }
    }
}
