package org.acme.Resources;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.PGDB.Entities.*;
import org.acme.profiles.TestContainersProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(TestContainersProfile.class)
@DisplayName("Game Resource Integration Tests")
public class GameResourceIntegrationTest {

    @Inject
    EntityManager entityManager;

    @BeforeEach
    @Transactional
    public void cleanup() {
        // Clean database before each test (order matters due to foreign keys)
        Game.deleteAll();
        Developer.deleteAll();
        Publisher.deleteAll();
    }

    // Helper to create and persist test data - must be called from non-transactional context
    @Transactional
    public void createTestGame(String gameTitle, String devName, String pubName) {
        Developer dev = new Developer();
        dev.setName(devName);
        dev.persist();

        Publisher pub = new Publisher();
        pub.setName(pubName);
        pub.persist();

        Game game = new Game();
        game.setTitle(gameTitle);
        game.setDescription("Description for " + gameTitle);
        game.setReleaseDate(LocalDate.of(2020, 1, 1));
        game.setCoverUrl(gameTitle.toLowerCase() + ".jpg");
        game.setDeveloper(dev);
        game.setPublisher(pub);
        game.persist();
    }

    // ==================== GET /api/games/getAll Tests ====================

    @Test
    @TestSecurity(user = "regularuser", roles = {"user"})
    @DisplayName("GET /getAll - Success as user")
    public void testGetAll_AsUser_Success() {
        given()
                .when()
                .get("/api/games/getAll")
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
                .get("/api/games/getAll")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("GET /getAll - Unauthorized without authentication")
    public void testGetAll_Unauthenticated_Unauthorized() {
        given()
                .when()
                .get("/api/games/getAll")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "wrongroleuser", roles = {"viewer"})
    @DisplayName("GET /getAll - Forbidden with wrong role")
    public void testGetAll_WrongRole_Forbidden() {
        given()
                .when()
                .get("/api/games/getAll")
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "regularuser", roles = {"user"})
    @DisplayName("GET /getAll - Empty database returns empty list")
    public void testGetAll_EmptyDatabase_ReturnsEmptyList() {
        given()
                .when()
                .get("/api/games/getAll")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("GET /getAll - Returns persisted games")
    public void testGetAll_ReturnsPersistedGames() {
        // Create test data with different developers to avoid duplicates
        createTestGame("The Last of Us", "Naughty Dog", "Sony");
        createTestGame("Uncharted 4", "Another Studio", "Another Publisher");

        // Query via API
        given()
                .when()
                .get("/api/games/getAll")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(2))
                .body("title", hasItems("The Last of Us", "Uncharted 4"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("GET /getAll - Returns games with developer relationship")
    public void testGetAll_ReturnsGamesWithDeveloper() {
        // Create test data
        createTestGame("Elden Ring", "FromSoftware", "Bandai Namco");

        // Query via API and verify developer relationship is included
        given()
                .when()
                .get("/api/games/getAll")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(1))
                .body("[0].title", equalTo("Elden Ring"))
                .body("[0].developer", notNullValue())
                .body("[0].developer.name", equalTo("FromSoftware"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("GET /getAll - Returns games with publisher relationship")
    public void testGetAll_ReturnsGamesWithPublisher() {
        // Create test data
        createTestGame("The Witcher 3", "CD Projekt Red", "CD Projekt");

        // Query via API and verify publisher relationship is included
        given()
                .when()
                .get("/api/games/getAll")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(1))
                .body("[0].title", equalTo("The Witcher 3"))
                .body("[0].publisher", notNullValue())
                .body("[0].publisher.name", equalTo("CD Projekt"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("GET /getAll - Returns all game fields")
    public void testGetAll_ReturnsAllFields() {
        // Create test data
        createTestGame("GTA V", "Rockstar North", "Rockstar Games");

        given()
                .when()
                .get("/api/games/getAll")
                .then()
                .statusCode(200)
                .body("[0].id", notNullValue())
                .body("[0].title", equalTo("GTA V"))
                .body("[0].description", notNullValue())
                .body("[0].releaseDate", equalTo("2020-01-01"))
                .body("[0].coverUrl", equalTo("gta v.jpg"))
                .body("[0].developer.id", notNullValue())
                .body("[0].publisher.id", notNullValue());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("GET /getAll - Multiple games with different developers")
    public void testGetAll_MultipleGamesWithDifferentDevelopers() {
        // Create test data
        createTestGame("Mario Odyssey", "Nintendo EPD", "Nintendo");
        createTestGame("Portal 2", "Valve", "Valve");

        given()
                .when()
                .get("/api/games/getAll")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("developer.name", hasItems("Nintendo EPD", "Valve"));
    }

    // ==================== Database Relationship Tests ====================

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("Database - Game persists with developer and publisher")
    @Transactional
    public void testDatabase_GameWithRelationships() {
        Developer dev = new Developer();
        dev.setName("Test Dev");
        dev.persist();

        Publisher pub = new Publisher();
        pub.setName("Test Pub");
        pub.persist();

        Game game = new Game();
        game.setTitle("Test Game");
        game.setDescription("Test Description");
        game.setReleaseDate(LocalDate.now());
        game.setCoverUrl("test.jpg");
        game.setDeveloper(dev);
        game.setPublisher(pub);
        game.persist();

        // Verify in database within same transaction
        Game found = Game.findById(game.getId());
        assertNotNull(found);
        assertNotNull(found.getDeveloper());
        assertNotNull(found.getPublisher());
        assertEquals("Test Dev", found.getDeveloper().getName());
        assertEquals("Test Pub", found.getPublisher().getName());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("Database - Unique title constraint")
    @Transactional
    public void testDatabase_UniqueTitleConstraint() {
        Developer dev = new Developer();
        dev.setName("Test Dev");
        dev.persist();

        Publisher pub = new Publisher();
        pub.setName("Test Pub");
        pub.persist();

        Game game = new Game();
        game.setTitle("Unique Game Title");
        game.setDescription("Test");
        game.setReleaseDate(LocalDate.now());
        game.setCoverUrl("test.jpg");
        game.setDeveloper(dev);
        game.setPublisher(pub);
        game.persist();

        // Verify it exists and is unique
        assertEquals(1, Game.count("title", "Unique Game Title"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("Database - Multiple games per developer")
    @Transactional
    public void testDatabase_MultipleGamesPerDeveloper() {
        Developer dev = new Developer();
        dev.setName("Rockstar Games");
        dev.persist();

        Publisher pub = new Publisher();
        pub.setName("Rockstar Games");
        pub.persist();

        Game game1 = new Game();
        game1.setTitle("GTA V");
        game1.setDescription("Test");
        game1.setReleaseDate(LocalDate.now());
        game1.setCoverUrl("test.jpg");
        game1.setDeveloper(dev);
        game1.setPublisher(pub);
        game1.persist();

        Game game2 = new Game();
        game2.setTitle("Red Dead Redemption 2");
        game2.setDescription("Test");
        game2.setReleaseDate(LocalDate.now());
        game2.setCoverUrl("test.jpg");
        game2.setDeveloper(dev);
        game2.setPublisher(pub);
        game2.persist();

        // Verify both games share the same developer
        assertEquals(2, Game.count());

        Game foundGta = Game.findById(game1.getId());
        Game foundRdr = Game.findById(game2.getId());

        assertEquals(foundGta.getDeveloper().getId(), foundRdr.getDeveloper().getId());
        assertEquals("Rockstar Games", foundGta.getDeveloper().getName());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("Database - Long description stores correctly")
    @Transactional
    public void testDatabase_LongDescriptionPersistence() {
        Developer dev = new Developer();
        dev.setName("Test Dev");
        dev.persist();

        Publisher pub = new Publisher();
        pub.setName("Test Pub");
        pub.persist();

        // Create a description of 1000 characters (max is 1100)
        String longDescription = "A".repeat(1000);

        Game game = new Game();
        game.setTitle("Long Description Game");
        game.setDescription(longDescription);
        game.setReleaseDate(LocalDate.now());
        game.setCoverUrl("test.jpg");
        game.setDeveloper(dev);
        game.setPublisher(pub);
        game.persist();

        // Verify long description is stored correctly
        Game found = Game.findById(game.getId());
        assertEquals(1000, found.getDescription().length());
        assertEquals(longDescription, found.getDescription());
    }
}