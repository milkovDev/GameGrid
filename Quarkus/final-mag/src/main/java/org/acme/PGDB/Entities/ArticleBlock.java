package org.acme.PGDB.Entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "article_blocks")
public class ArticleBlock extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "articleBlockSeq")
    @SequenceGenerator(name = "articleBlockSeq", sequenceName = "article_block_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "article_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Article article;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private BlockTypeEnum blockType;

    @Column(length = 2100, nullable = false)
    private String content;

    @Column(nullable = false)
    private int position;

    // Public empty constructor
    public ArticleBlock() {}

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public BlockTypeEnum getBlockType() {
        return blockType;
    }

    public void setBlockType(BlockTypeEnum blockType) {
        this.blockType = blockType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    // toString method
    @Override
    public String toString() {
        return "ArticleBlock{" +
                "id=" + id +
                ", article=" + article +
                ", blockType=" + blockType +
                ", content='" + content + '\'' +
                ", position=" + position +
                '}';
    }
}
