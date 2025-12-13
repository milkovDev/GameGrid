package org.acme.PGDB.Entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "articles")
public class Article extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "articleSeq")
    @SequenceGenerator(name = "articleSeq", sequenceName = "article_seq", allocationSize = 1)
    private Long id;

    @Column(length = 255, nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "author_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User author;

    @Column(nullable = false)
    private LocalDateTime publishedAt;

    @Column(length = 255, nullable = true)
    private String featuredImageUrl;

    @OneToMany(mappedBy = "article", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticleBlock> articleBlocks;

    // Public empty constructor
    public Article() {}

    // Getters and setters
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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
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

    public List<ArticleBlock> getArticleBlocks() {
        return articleBlocks;
    }

    public void setArticleBlocks(List<ArticleBlock> articleBlocks) {
        this.articleBlocks = articleBlocks;
    }

    // toString method
    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author=" + author +
                ", publishedAt=" + publishedAt +
                ", featuredImageUrl='" + featuredImageUrl + '\'' +
                ", articleBlocks=" + articleBlocks +
                '}';
    }
}
