package org.acme.PGDB.Repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.PGDB.Entities.UserGameListEntry;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserGameListEntryRepository implements PanacheRepository<UserGameListEntry> {

}
