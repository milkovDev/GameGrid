package org.acme.PGDB.Repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.PGDB.Entities.Developer;

@ApplicationScoped
public class DeveloperRepository implements PanacheRepository<Developer> {
}
