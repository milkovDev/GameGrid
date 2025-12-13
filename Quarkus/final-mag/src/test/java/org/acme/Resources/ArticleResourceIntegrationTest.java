package org.acme.Resources;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.acme.PGDB.Entities.*;
import org.acme.profiles.TestContainersProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(TestContainersProfile.class)
@DisplayName("Article Resource Integration Tests")
public class ArticleResourceIntegrationTest {

    @BeforeEach
    @Transactional
    public void cleanup() {
        // Clean database (order matters due to foreign keys)
        ArticleBlock.deleteAll();
        Article.deleteAll();
        User.deleteAll();
    }

    // Helper to create test article
    @Transactional
    public Article createTestArticle(String title, String authorName) {
        // Create author
        User author = new User();
        author.setId(UUID.randomUUID());
        author.setDisplayName(authorName);
        author.setBio("Bio for " + authorName);
        author.setAvatarUrl("avatar.jpg");
        author.persist();

        // Create article
        Article article = new Article();
        article.setTitle(title);
        article.setAuthor(author);
        article.setPublishedAt(LocalDateTime.now());
        article.setFeaturedImageUrl("featured.jpg");
        article.setArticleBlocks(new ArrayList<>());
        article.persist();

        return article;
    }

    // ==================== GET /api/articles/getAll Tests ====================

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /getAll - Success as user")
    public void testGetAll_AsUser_Success() {
        given()
                .when()
                .get("/api/articles/getAll")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("GET /getAll - Success as superuser")
    public void testGetAll_AsSuperuser_Success() {
        given()
                .when()
                .get("/api/articles/getAll")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("GET /getAll - Unauthorized without authentication")
    public void testGetAll_Unauthenticated_Unauthorized() {
        given()
                .when()
                .get("/api/articles/getAll")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "wrongrole", roles = {"viewer"})
    @DisplayName("GET /getAll - Forbidden with wrong role")
    public void testGetAll_WrongRole_Forbidden() {
        given()
                .when()
                .get("/api/articles/getAll")
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /getAll - Empty database returns empty list")
    public void testGetAll_EmptyDatabase_ReturnsEmptyList() {
        given()
                .when()
                .get("/api/articles/getAll")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /getAll - Returns all articles")
    public void testGetAll_ReturnsAllArticles() {
        // Create test articles
        createTestArticle("Article 1", "Author One");
        createTestArticle("Article 2", "Author Two");
        createTestArticle("Article 3", "Author Three");

        given()
                .when()
                .get("/api/articles/getAll")
                .then()
                .statusCode(200)
                .body("size()", equalTo(3))
                .body("title", hasItems("Article 1", "Article 2", "Article 3"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /getAll - Returns article with all fields")
    public void testGetAll_ReturnsAllFields() {
        createTestArticle("Test Article", "Test Author");

        given()
                .when()
                .get("/api/articles/getAll")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].id", notNullValue())
                .body("[0].title", equalTo("Test Article"))
                .body("[0].author", equalTo("Test Author"))
                .body("[0].publishedAt", notNullValue())
                .body("[0].featuredImageUrl", equalTo("featured.jpg"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /getAll - Returns articles with author information")
    public void testGetAll_ReturnsArticlesWithAuthor() {
        createTestArticle("Gaming News", "John Doe");
        createTestArticle("Review Article", "Jane Smith");

        given()
                .when()
                .get("/api/articles/getAll")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("author", hasItems("John Doe", "Jane Smith"));
    }

    // ==================== Database Tests ====================

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("Database - Article persists with author relationship")
    @Transactional
    public void testDatabase_ArticleWithAuthor() {
        Article article = createTestArticle("Test Article", "Test Author");

        // Verify in database
        Article found = Article.findById(article.getId());
        assertNotNull(found);
        assertNotNull(found.getAuthor());
        assertEquals("Test Article", found.getTitle());
        assertEquals("Test Author", found.getAuthor().getDisplayName());
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("Database - Multiple articles by same author")
    @Transactional
    public void testDatabase_MultipleArticlesSameAuthor() {
        // Create author
        User author = new User();
        author.setId(UUID.randomUUID());
        author.setDisplayName("Prolific Author");
        author.setBio("Bio");
        author.setAvatarUrl("avatar.jpg");
        author.persist();

        // Create multiple articles
        Article article1 = new Article();
        article1.setTitle("Article 1");
        article1.setAuthor(author);
        article1.setPublishedAt(LocalDateTime.now());
        article1.setFeaturedImageUrl("img1.jpg");
        article1.persist();

        Article article2 = new Article();
        article2.setTitle("Article 2");
        article2.setAuthor(author);
        article2.setPublishedAt(LocalDateTime.now());
        article2.setFeaturedImageUrl("img2.jpg");
        article2.persist();

        // Verify both articles share same author
        Article found1 = Article.findById(article1.getId());
        Article found2 = Article.findById(article2.getId());

        assertEquals(found1.getAuthor().getId(), found2.getAuthor().getId());
        assertEquals(2, Article.count());
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("Database - Article fields persist correctly")
    @Transactional
    public void testDatabase_ArticleFieldsPersistence() {
        User author = new User();
        author.setId(UUID.randomUUID());
        author.setDisplayName("Test Author");
        author.persist();

        LocalDateTime publishTime = LocalDateTime.of(2024, 1, 15, 10, 30);

        Article article = new Article();
        article.setTitle("Important News");
        article.setAuthor(author);
        article.setPublishedAt(publishTime);
        article.setFeaturedImageUrl("https://example.com/image.jpg");
        article.persist();

        // Verify all fields
        Article found = Article.findById(article.getId());
        assertEquals("Important News", found.getTitle());
        assertEquals(publishTime, found.getPublishedAt());
        assertEquals("https://example.com/image.jpg", found.getFeaturedImageUrl());
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("Database - Article title can be long")
    @Transactional
    public void testDatabase_LongTitle() {
        String longTitle = "A".repeat(200); // Max is 255
        Article article = createTestArticle(longTitle, "Author");

        Article found = Article.findById(article.getId());
        assertEquals(200, found.getTitle().length());
        assertEquals(longTitle, found.getTitle());
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("Database - Count all articles")
    @Transactional
    public void testDatabase_CountArticles() {
        createTestArticle("Article 1", "Author 1");
        createTestArticle("Article 2", "Author 2");
        createTestArticle("Article 3", "Author 3");

        long count = Article.count();
        assertEquals(3, count);
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("Database - Query articles by author")
    @Transactional
    public void testDatabase_QueryByAuthor() {
        User author1 = new User();
        author1.setId(UUID.randomUUID());
        author1.setDisplayName("Author One");
        author1.persist();

        User author2 = new User();
        author2.setId(UUID.randomUUID());
        author2.setDisplayName("Author Two");
        author2.persist();

        Article article1 = new Article();
        article1.setTitle("Article by Author 1");
        article1.setAuthor(author1);
        article1.setPublishedAt(LocalDateTime.now());
        article1.persist();

        Article article2 = new Article();
        article2.setTitle("Another by Author 1");
        article2.setAuthor(author1);
        article2.setPublishedAt(LocalDateTime.now());
        article2.persist();

        Article article3 = new Article();
        article3.setTitle("Article by Author 2");
        article3.setAuthor(author2);
        article3.setPublishedAt(LocalDateTime.now());
        article3.persist();

        // Verify can query by author
        long countForAuthor1 = Article.count("author", author1);
        assertEquals(2, countForAuthor1);
    }
}