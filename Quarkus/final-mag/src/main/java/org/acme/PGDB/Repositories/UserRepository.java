package org.acme.PGDB.Repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.PGDB.Entities.User;

import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    public User findById(UUID id) {
        return find("id", id).firstResult();
    }
}
