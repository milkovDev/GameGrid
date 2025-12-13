package org.acme.PGDB.DTOs;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ArticleDTO {
    private Long id;
    private String title;
    private String author;
    private UUID authorId;
    private LocalDateTime publishedAt;
    private String featuredImageUrl;
    private List<ArticleBlockDTO> articleBlocks;

    public ArticleDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getFeaturedImageUrl() {
        return featuredImageUrl;
    }

    public void setFeaturedImageUrl(String featuredImageUrl) {
        this.featuredImageUrl = featuredImageUrl;
    }

    public List<ArticleBlockDTO> getArticleBlocks() {
        return articleBlocks;
    }

    public void setArticleBlocks(List<ArticleBlockDTO> articleBlocks) {
        this.articleBlocks = articleBlocks;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }
}
