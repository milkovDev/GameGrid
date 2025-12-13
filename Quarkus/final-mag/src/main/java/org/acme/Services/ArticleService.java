package org.acme.Services;

import io.quarkus.hibernate.orm.panache.Panache;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.acme.PGDB.DTOs.ArticleBlockDTO;
import org.acme.PGDB.DTOs.ArticleDTO;
import org.acme.PGDB.Entities.Article;
import org.acme.PGDB.Entities.ArticleBlock;
import org.acme.PGDB.Entities.BlockTypeEnum;
import org.acme.PGDB.Entities.User;
import org.acme.PGDB.Mappers.ArticleBlockMapper;
import org.acme.PGDB.Mappers.ArticleMapper;
import org.acme.PGDB.Repositories.ArticleBlockRepository;
import org.acme.PGDB.Repositories.ArticleRepository;
import org.acme.PGDB.Repositories.UserRepository;
import org.acme.WebSocket.NotificationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ArticleService {

    @Inject
    ArticleMapper articleMapper;

    @Inject
    ArticleRepository articleRepository;

    @Inject
    ArticleBlockMapper articleBlockMapper;

    @Inject
    ArticleBlockRepository articleBlockRepository;

    @Inject
    NotificationManager notificationManager;

    @Transactional
    public ArticleDTO create(ArticleDTO dto) {
        Article article = articleMapper.toEntity(dto);
        articleRepository.persist(article);

        article.setArticleBlocks(new ArrayList<>());

        List<ArticleBlockDTO> blockDtos = dto.getArticleBlocks() != null ? dto.getArticleBlocks() : new ArrayList<>();
        for (ArticleBlockDTO blockDto : blockDtos) {
            blockDto.setArticleId(article.getId());
            ArticleBlock block = articleBlockMapper.toEntity(blockDto);
            articleBlockRepository.persist(block);


            article.getArticleBlocks().add(block);
        }

        //push notifications to followers
        notificationManager.notifyArticlePublished(article.getAuthor().getId().toString(), article.getAuthor().getDisplayName(), article.getTitle());

        return articleMapper.toDTO(article);
    }

    @Transactional
    public ArticleDTO update(ArticleDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("ID is required for update");
        }

        Article article = articleRepository.findById(dto.getId());
        if (article == null) {
            throw new NotFoundException("Article not found");
        }

        article.setTitle(dto.getTitle());
        article.setFeaturedImageUrl(dto.getFeaturedImageUrl());

        List<ArticleBlock> existingBlocks = article.getArticleBlocks();
        List<ArticleBlockDTO> newBlockDtos = dto.getArticleBlocks() != null ? dto.getArticleBlocks() : new ArrayList<>();

        Set<Long> newBlockIds = newBlockDtos.stream()
                .map(ArticleBlockDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Delete removed blocks
        List<ArticleBlock> toDelete = existingBlocks.stream()
                .filter(b -> !newBlockIds.contains(b.getId()))
                .toList();
        for (ArticleBlock block : toDelete) {
            article.getArticleBlocks().remove(block);
            articleBlockRepository.delete(block);
        }

        // Create or update blocks
        for (ArticleBlockDTO blockDto : newBlockDtos) {
            if (blockDto.getId() == null) {
                // Create new
                blockDto.setArticleId(article.getId());
                ArticleBlock newBlock = articleBlockMapper.toEntity(blockDto);
                articleBlockRepository.persist(newBlock);
            } else {
                // Update existing
                ArticleBlock block = articleBlockRepository.findById(blockDto.getId());
                if (block != null && block.getArticle().getId().equals(article.getId())) {
                    block.setBlockType(BlockTypeEnum.valueOf(blockDto.getBlockType()));
                    block.setContent(blockDto.getContent());
                    block.setPosition(blockDto.getPosition());

                    articleBlockRepository.persist(block);
                }
            }
        }

        Panache.getEntityManager().flush(); // Ensure changes are flushed

        return articleMapper.toDTO(article);
    }

    public List<ArticleDTO> getAll() {
        return articleRepository.listAll().stream()
                .map(articleMapper::toDTO)
                .collect(Collectors.toList());
    }
}
