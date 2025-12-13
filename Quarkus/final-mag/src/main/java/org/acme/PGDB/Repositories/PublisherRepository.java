package org.acme.PGDB.Repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.PGDB.Entities.Publisher;

@ApplicationScoped
public class PublisherRepository implements PanacheRepository<Publisher> {
}
