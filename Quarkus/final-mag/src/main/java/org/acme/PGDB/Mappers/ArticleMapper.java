package org.acme.PGDB.Mappers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.PGDB.DTOs.ArticleDTO;
import org.acme.PGDB.Entities.Article;
import org.acme.PGDB.Entities.User;
import org.acme.PGDB.Repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ApplicationScoped
public class ArticleMapper {
    @Inject UserMapper userMapper;
    @Inject ArticleBlockMapper blockMapper;
    @Inject UserRepository userRepository;

    public ArticleDTO toDTO(Article entity) {
        ArticleDTO dto = new ArticleDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setAuthor(entity.getAuthor().getDisplayName());
        dto.setAuthorId(entity.getAuthor().getId());
        dto.setPublishedAt(entity.getPublishedAt());
        dto.setFeaturedImageUrl(entity.getFeaturedImageUrl());
        dto.setArticleBlocks(entity.getArticleBlocks().stream()
                .map(blockMapper::toDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    public Article toEntity(ArticleDTO dto) {
        Article entity = new Article();

        entity.setTitle(dto.getTitle());
        entity.setPublishedAt(LocalDateTime.now());
        entity.setFeaturedImageUrl(dto.getFeaturedImageUrl());

        User author = userRepository.findById(dto.getAuthorId());
        entity.setAuthor(author);

        return entity;
    }
}
