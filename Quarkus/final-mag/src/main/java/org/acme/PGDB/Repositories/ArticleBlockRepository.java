package org.acme.PGDB.Repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.PGDB.Entities.ArticleBlock;

@ApplicationScoped
public class ArticleBlockRepository implements PanacheRepository<ArticleBlock> {
}
