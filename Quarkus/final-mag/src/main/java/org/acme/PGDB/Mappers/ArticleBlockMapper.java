package org.acme.PGDB.Mappers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.PGDB.DTOs.ArticleBlockDTO;
import org.acme.PGDB.Entities.ArticleBlock;
import org.acme.PGDB.Entities.BlockTypeEnum;
import org.acme.PGDB.Repositories.ArticleRepository;

@ApplicationScoped
public class ArticleBlockMapper {
    @Inject
    ArticleRepository articleRepository;

    public ArticleBlockDTO toDTO(ArticleBlock entity) {
        ArticleBlockDTO dto = new ArticleBlockDTO();
        dto.setId(entity.getId());
        dto.setArticleId(entity.getArticle().getId());
        dto.setBlockType(entity.getBlockType().name());
        dto.setContent(entity.getContent());
        dto.setPosition(entity.getPosition());
        return dto;
    }

    public ArticleBlock toEntity(ArticleBlockDTO dto) {
        ArticleBlock entity = new ArticleBlock();
        if (dto.getArticleId() != null) {
            entity.setArticle(articleRepository.findById(dto.getArticleId()));
        }
        if (dto.getBlockType() != null) {
            entity.setBlockType(BlockTypeEnum.valueOf(dto.getBlockType()));
        }
        entity.setContent(dto.getContent());
        entity.setPosition(dto.getPosition());
        return entity;
    }
}
