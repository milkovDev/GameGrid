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

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(TestContainersProfile.class)
@DisplayName("UserGameListEntry Resource Integration Tests")
public class UserGameListEntryResourceIntegrationTest {

    @BeforeEach
    @Transactional
    public void cleanup() {
        // Clean database (order matters due to foreign keys)
        UserGameListEntry.deleteAll();
        Game.deleteAll();
        Developer.deleteAll();
        Publisher.deleteAll();
        User.deleteAll();
    }

    // Helper to create test data - creates new unique user each time
    @Transactional
    public UserGameListEntry createTestEntry(String gameName, StatusEnum status) {
        // Create NEW user with unique ID each time
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setDisplayName("Test User");
        user.setBio("Test bio");
        user.setAvatarUrl("test.jpg");
        user.persist();

        // Create developer and publisher
        Developer dev = new Developer();
        dev.setName("Test Dev " + gameName);
        dev.persist();

        Publisher pub = new Publisher();
        pub.setName("Test Pub " + gameName);
        pub.persist();

        // Create game
        Game game = new Game();
        game.setTitle(gameName);
        game.setDescription("Description for " + gameName);
        game.setReleaseDate(LocalDate.now());
        game.setCoverUrl("test.jpg");
        game.setDeveloper(dev);
        game.setPublisher(pub);
        game.persist();

        // Create list entry
        UserGameListEntry entry = new UserGameListEntry();
        entry.setUser(user);
        entry.setGame(game);
        entry.setStatus(status);
        entry.setIsFavorite(false);
        entry.setRating(null);
        entry.setReviewText(null);
        entry.persist();

        return entry;
    }

    // Helper for tests that need specific user ID
    @Transactional
    public UserGameListEntry createTestEntryForUser(UUID userId, String gameName, StatusEnum status) {
        // Check if user exists, if not create it
        User user = User.findById(userId);
        if (user == null) {
            user = new User();
            user.setId(userId);
            user.setDisplayName("Test User");
            user.setBio("Test bio");
            user.setAvatarUrl("test.jpg");
            user.persist();
        }

        // Create developer and publisher
        Developer dev = new Developer();
        dev.setName("Test Dev " + gameName);
        dev.persist();

        Publisher pub = new Publisher();
        pub.setName("Test Pub " + gameName);
        pub.persist();

        // Create game
        Game game = new Game();
        game.setTitle(gameName);
        game.setDescription("Description for " + gameName);
        game.setReleaseDate(LocalDate.now());
        game.setCoverUrl("test.jpg");
        game.setDeveloper(dev);
        game.setPublisher(pub);
        game.persist();

        // Create list entry
        UserGameListEntry entry = new UserGameListEntry();
        entry.setUser(user);
        entry.setGame(game);
        entry.setStatus(status);
        entry.setIsFavorite(false);
        entry.setRating(null);
        entry.setReviewText(null);
        entry.persist();

        return entry;
    }

    // ==================== GET /api/usergamelistentries/getEntriesForUser/{userId} Tests ====================

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /getEntriesForUser/{userId} - Success as user")
    public void testGetEntriesForUser_AsUser_Success() {
        UUID userId = UUID.randomUUID();

        given()
                .pathParam("userId", userId.toString())
                .when()
                .get("/api/usergamelistentries/getEntriesForUser/{userId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("GET /getEntriesForUser/{userId} - Success as superuser")
    public void testGetEntriesForUser_AsSuperuser_Success() {
        UUID userId = UUID.randomUUID();

        given()
                .pathParam("userId", userId.toString())
                .when()
                .get("/api/usergamelistentries/getEntriesForUser/{userId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("GET /getEntriesForUser/{userId} - Unauthorized without authentication")
    public void testGetEntriesForUser_Unauthenticated_Unauthorized() {
        UUID userId = UUID.randomUUID();

        given()
                .pathParam("userId", userId.toString())
                .when()
                .get("/api/usergamelistentries/getEntriesForUser/{userId}")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /getEntriesForUser/{userId} - Empty list when no entries")
    public void testGetEntriesForUser_NoEntries_ReturnsEmptyList() {
        UUID userId = UUID.randomUUID();

        given()
                .pathParam("userId", userId.toString())
                .when()
                .get("/api/usergamelistentries/getEntriesForUser/{userId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /getEntriesForUser/{userId} - Returns user's entries")
    public void testGetEntriesForUser_ReturnsEntries() {
        UUID userId = UUID.randomUUID();

        // Create test entries for the same user
        createTestEntryForUser(userId, "Game 1", StatusEnum.PLAYING);
        createTestEntryForUser(userId, "Game 2", StatusEnum.FINISHED);
        createTestEntryForUser(userId, "Game 3", StatusEnum.WISHLIST);

        given()
                .pathParam("userId", userId.toString())
                .when()
                .get("/api/usergamelistentries/getEntriesForUser/{userId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(3))
                .body("game.title", hasItems("Game 1", "Game 2", "Game 3"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /getEntriesForUser/{userId} - Returns entry with all fields")
    public void testGetEntriesForUser_ReturnsAllFields() {
        UUID userId = UUID.randomUUID();
        createTestEntryForUser(userId, "Test Game", StatusEnum.PLAYING);

        given()
                .pathParam("userId", userId.toString())
                .when()
                .get("/api/usergamelistentries/getEntriesForUser/{userId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].id", notNullValue())
                .body("[0].userId", equalTo(userId.toString()))
                .body("[0].game", notNullValue())
                .body("[0].status", equalTo("PLAYING"))
                .body("[0].isFavorite", equalTo(false));
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /getEntriesForUser/{userId} - Only returns entries for specified user")
    public void testGetEntriesForUser_OnlyReturnsUserEntries() {
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();

        // Create entries for two different users
        createTestEntryForUser(user1Id, "User1 Game", StatusEnum.PLAYING);
        createTestEntryForUser(user2Id, "User2 Game", StatusEnum.FINISHED);

        // Query for user1 - should only get their entry
        given()
                .pathParam("userId", user1Id.toString())
                .when()
                .get("/api/usergamelistentries/getEntriesForUser/{userId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].game.title", equalTo("User1 Game"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /getEntriesForUser/{userId} - Returns entries with different statuses")
    public void testGetEntriesForUser_DifferentStatuses() {
        UUID userId = UUID.randomUUID();

        createTestEntryForUser(userId, "Playing Game", StatusEnum.PLAYING);
        createTestEntryForUser(userId, "Finished Game", StatusEnum.FINISHED);
        createTestEntryForUser(userId, "Wishlist Game", StatusEnum.WISHLIST);

        given()
                .pathParam("userId", userId.toString())
                .when()
                .get("/api/usergamelistentries/getEntriesForUser/{userId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(3))
                .body("status", hasItems("PLAYING", "FINISHED", "WISHLIST"));
    }

    // ==================== Database Tests ====================

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("Database - Entry persists with user and game relationships")
    @Transactional
    public void testDatabase_EntryWithRelationships() {
        UserGameListEntry entry = createTestEntry("Test Game", StatusEnum.PLAYING);

        // Verify in database
        UserGameListEntry found = UserGameListEntry.findById(entry.getId());
        assertNotNull(found);
        assertNotNull(found.getUser());
        assertNotNull(found.getGame());
        assertEquals("Test Game", found.getGame().getTitle());
        assertEquals(StatusEnum.PLAYING, found.getStatus());
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("Database - Multiple entries for same user")
    @Transactional
    public void testDatabase_MultipleEntriesPerUser() {
        UUID userId = UUID.randomUUID();

        createTestEntryForUser(userId, "Game 1", StatusEnum.PLAYING);
        createTestEntryForUser(userId, "Game 2", StatusEnum.FINISHED);

        // Verify count
        long count = UserGameListEntry.count();
        assertEquals(2, count);
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("Database - Entry fields store correctly")
    @Transactional
    public void testDatabase_EntryFieldsPersistence() {
        UUID userId = UUID.randomUUID();

        // Create user
        User user = new User();
        user.setId(userId);
        user.setDisplayName("Test User");
        user.persist();

        // Create game
        Developer dev = new Developer();
        dev.setName("Test Dev");
        dev.persist();

        Publisher pub = new Publisher();
        pub.setName("Test Pub");
        pub.persist();

        Game game = new Game();
        game.setTitle("Test Game");
        game.setDescription("Description");
        game.setReleaseDate(LocalDate.now());
        game.setCoverUrl("test.jpg");
        game.setDeveloper(dev);
        game.setPublisher(pub);
        game.persist();

        // Create entry with all fields
        UserGameListEntry entry = new UserGameListEntry();
        entry.setUser(user);
        entry.setGame(game);
        entry.setStatus(StatusEnum.FINISHED);
        entry.setIsFavorite(true);
        entry.setRating(5);
        entry.setReviewText("Great game!");
        entry.persist();

        // Verify all fields
        UserGameListEntry found = UserGameListEntry.findById(entry.getId());
        assertEquals(StatusEnum.FINISHED, found.getStatus());
        assertTrue(found.getIsFavorite());
        assertEquals(5, found.getRating());
        assertEquals("Great game!", found.getReviewText());
    }
}