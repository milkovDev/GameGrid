package org.acme.Resources;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.N4JDB.Nodes.UserNode;
import org.acme.PGDB.DTOs.UserDTO;
import org.acme.PGDB.Entities.User;
import org.acme.profiles.TestContainersProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(TestContainersProfile.class)
@DisplayName("User Resource Integration Tests")
public class UserResourceIntegrationTest {

    @Inject
    Driver neo4jDriver;

    @BeforeEach
    @Transactional
    public void cleanup() {
        // Clean PostgreSQL
        User.deleteAll();

        // Clean Neo4j
        try (Session session = neo4jDriver.session()) {
            session.run("MATCH (n) DETACH DELETE n");
        }
    }

    // Helper methods to create test data - PUBLIC with @Transactional
    @Transactional
    public User createUserInPostgres(UUID id, String displayName) {
        User user = new User();
        user.setId(id);
        user.setDisplayName(displayName);
        user.setBio("Bio for " + displayName);
        user.setAvatarUrl("https://example.com/avatars/" + displayName.toLowerCase() + ".jpg");
        user.persist();
        return user;
    }

    @Transactional
    public UserNode createUserInNeo4j(String userId, String displayName) {
        try (Session session = neo4jDriver.session()) {
            session.run(
                    "CREATE (u:UserNode {userId: $userId, displayName: $displayName})",
                    org.neo4j.driver.Values.parameters("userId", userId, "displayName", displayName)
            );
        }
        return getUserNodeFromNeo4j(userId);
    }

    private UserNode getUserNodeFromNeo4j(String userId) {
        try (Session session = neo4jDriver.session()) {
            var result = session.run(
                    "MATCH (u:UserNode {userId: $userId}) RETURN u.userId as userId, u.displayName as displayName",
                    org.neo4j.driver.Values.parameters("userId", userId)
            );

            if (result.hasNext()) {
                var record = result.single();
                UserNode node = new UserNode();
                node.setUserId(record.get("userId").asString());
                node.setDisplayName(record.get("displayName").asString());
                return node;
            }
            return null;
        }
    }

    private void createFollowRelationship(String followerId, String followedId) {
        try (Session session = neo4jDriver.session()) {
            session.run(
                    "MATCH (follower:UserNode {userId: $followerId}), (followed:UserNode {userId: $followedId}) " +
                            "CREATE (follower)-[:FOLLOWS]->(followed)",
                    org.neo4j.driver.Values.parameters("followerId", followerId, "followedId", followedId)
            );
        }
    }

    private long countFollowRelationships(String followerId, String followedId) {
        try (Session session = neo4jDriver.session()) {
            var result = session.run(
                    "MATCH (follower:UserNode {userId: $followerId})-[:FOLLOWS]->(followed:UserNode {userId: $followedId}) " +
                            "RETURN count(*) as count",
                    org.neo4j.driver.Values.parameters("followerId", followerId, "followedId", followedId)
            );
            return result.single().get("count").asLong();
        }
    }

    // ==================== GET /api/users/getAll Tests ====================

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /getAll - Success as user")
    public void testGetAll_AsUser_Success() {
        given()
                .when()
                .get("/api/users/getAll")
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
                .get("/api/users/getAll")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("GET /getAll - Unauthorized without authentication")
    public void testGetAll_Unauthenticated_Unauthorized() {
        given()
                .when()
                .get("/api/users/getAll")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /getAll - Empty database returns empty list")
    public void testGetAll_EmptyDatabase_ReturnsEmptyList() {
        given()
                .when()
                .get("/api/users/getAll")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /getAll - Returns all users from PostgreSQL")
    public void testGetAll_ReturnsAllUsers() {
        // Create test users in PostgreSQL
        createUserInPostgres(UUID.randomUUID(), "Alice");
        createUserInPostgres(UUID.randomUUID(), "Bob");
        createUserInPostgres(UUID.randomUUID(), "Charlie");

        given()
                .when()
                .get("/api/users/getAll")
                .then()
                .statusCode(200)
                .body("size()", equalTo(3))
                .body("displayName", hasItems("Alice", "Bob", "Charlie"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /getAll - Returns user with all fields")
    public void testGetAll_ReturnsAllFields() {
        UUID userId = UUID.randomUUID();
        createUserInPostgres(userId, "TestUser");

        given()
                .when()
                .get("/api/users/getAll")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].id", notNullValue())
                .body("[0].displayName", equalTo("TestUser"))
                .body("[0].bio", equalTo("Bio for TestUser"))
                .body("[0].avatarUrl", containsString("testuser.jpg"));
    }

    // ==================== GET /api/users/{userId} Tests ====================

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /{userId} - Success retrieving existing user")
    public void testGetUser_ExistingUser_Success() {
        UUID userId = UUID.randomUUID();
        createUserInPostgres(userId, "JohnDoe");

        given()
                .pathParam("userId", userId.toString())
                .when()
                .get("/api/users/{userId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(userId.toString()))
                .body("displayName", equalTo("JohnDoe"))
                .body("bio", equalTo("Bio for JohnDoe"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /{userId} - Not found for non-existent user")
    public void testGetUser_NonExistentUser_NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        given()
                .pathParam("userId", nonExistentId.toString())
                .when()
                .get("/api/users/{userId}")
                .then()
                .statusCode(500); // Currently returns 500 with "User not found" message
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /{userId} - Returns complete user profile")
    public void testGetUser_ReturnsCompleteProfile() {
        UUID userId = UUID.randomUUID();
        User user = createUserInPostgres(userId, "ProfileUser");

        given()
                .pathParam("userId", userId.toString())
                .when()
                .get("/api/users/{userId}")
                .then()
                .statusCode(200)
                .body("displayName", equalTo("ProfileUser"))
                .body("bio", notNullValue())
                .body("avatarUrl", notNullValue());
    }

    // ==================== GET /api/users/followers/{userId} Tests ====================

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /followers/{userId} - Returns empty list when no followers")
    public void testGetFollowers_NoFollowers_ReturnsEmptyList() {
        UUID userId = UUID.randomUUID();
        createUserInPostgres(userId, "LonelyUser");
        createUserInNeo4j(userId.toString(), "LonelyUser");

        given()
                .pathParam("userId", userId.toString())
                .when()
                .get("/api/users/followers/{userId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /followers/{userId} - Returns follower list from Neo4j")
    public void testGetFollowers_WithFollowers_ReturnsList() {
        // Create users in both databases
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();
        UUID user3Id = UUID.randomUUID();

        createUserInPostgres(user1Id, "PopularUser");
        createUserInPostgres(user2Id, "Follower1");
        createUserInPostgres(user3Id, "Follower2");

        createUserInNeo4j(user1Id.toString(), "PopularUser");
        createUserInNeo4j(user2Id.toString(), "Follower1");
        createUserInNeo4j(user3Id.toString(), "Follower2");

        // Create follow relationships in Neo4j
        createFollowRelationship(user2Id.toString(), user1Id.toString());
        createFollowRelationship(user3Id.toString(), user1Id.toString());

        given()
                .pathParam("userId", user1Id.toString())
                .when()
                .get("/api/users/followers/{userId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("displayName", hasItems("Follower1", "Follower2"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /followers/{userId} - Single follower")
    public void testGetFollowers_SingleFollower() {
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();

        createUserInPostgres(user1Id, "User1");
        createUserInPostgres(user2Id, "User2");

        createUserInNeo4j(user1Id.toString(), "User1");
        createUserInNeo4j(user2Id.toString(), "User2");

        createFollowRelationship(user2Id.toString(), user1Id.toString());

        given()
                .pathParam("userId", user1Id.toString())
                .when()
                .get("/api/users/followers/{userId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].displayName", equalTo("User2"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /followers/{userId} - User not in Neo4j returns error")
    public void testGetFollowers_UserNotInNeo4j_Error() {
        UUID userId = UUID.randomUUID();
        createUserInPostgres(userId, "PostgresOnlyUser");
        // Don't create in Neo4j

        given()
                .pathParam("userId", userId.toString())
                .when()
                .get("/api/users/followers/{userId}")
                .then()
                .statusCode(500); // Returns 500 with "User not found in Neo4j"
    }

    // ==================== GET /api/users/following/{userId} Tests ====================

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /following/{userId} - Returns empty list when following nobody")
    public void testGetFollowing_FollowingNobody_ReturnsEmptyList() {
        UUID userId = UUID.randomUUID();
        createUserInPostgres(userId, "SoloUser");
        createUserInNeo4j(userId.toString(), "SoloUser");

        given()
                .pathParam("userId", userId.toString())
                .when()
                .get("/api/users/following/{userId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /following/{userId} - Returns following list from Neo4j")
    public void testGetFollowing_WithFollowing_ReturnsList() {
        // Create users in both databases
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();
        UUID user3Id = UUID.randomUUID();

        createUserInPostgres(user1Id, "ActiveUser");
        createUserInPostgres(user2Id, "Celebrity1");
        createUserInPostgres(user3Id, "Celebrity2");

        createUserInNeo4j(user1Id.toString(), "ActiveUser");
        createUserInNeo4j(user2Id.toString(), "Celebrity1");
        createUserInNeo4j(user3Id.toString(), "Celebrity2");

        // User1 follows User2 and User3
        createFollowRelationship(user1Id.toString(), user2Id.toString());
        createFollowRelationship(user1Id.toString(), user3Id.toString());

        given()
                .pathParam("userId", user1Id.toString())
                .when()
                .get("/api/users/following/{userId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("displayName", hasItems("Celebrity1", "Celebrity2"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /following/{userId} - Single following")
    public void testGetFollowing_SingleFollowing() {
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();

        createUserInPostgres(user1Id, "Follower");
        createUserInPostgres(user2Id, "Leader");

        createUserInNeo4j(user1Id.toString(), "Follower");
        createUserInNeo4j(user2Id.toString(), "Leader");

        createFollowRelationship(user1Id.toString(), user2Id.toString());

        given()
                .pathParam("userId", user1Id.toString())
                .when()
                .get("/api/users/following/{userId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].displayName", equalTo("Leader"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("GET /following/{userId} - User not in Neo4j returns error")
    public void testGetFollowing_UserNotInNeo4j_Error() {
        UUID userId = UUID.randomUUID();
        createUserInPostgres(userId, "PostgresOnlyUser");
        // Don't create in Neo4j

        given()
                .pathParam("userId", userId.toString())
                .when()
                .get("/api/users/following/{userId}")
                .then()
                .statusCode(500); // Returns 500 with "User not found in Neo4j"
    }

    // ==================== Integration Tests (Dual Database) ====================

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("Integration - User exists in both PostgreSQL and Neo4j")
    public void testIntegration_UserInBothDatabases() {
        UUID userId = UUID.randomUUID();
        createUserInPostgres(userId, "DualUser");
        createUserInNeo4j(userId.toString(), "DualUser");

        // Query PostgreSQL via REST
        given()
                .pathParam("userId", userId.toString())
                .get("/api/users/{userId}")
                .then()
                .statusCode(200)
                .body("displayName", equalTo("DualUser"));

        // Query Neo4j relationships via REST
        given()
                .pathParam("userId", userId.toString())
                .get("/api/users/followers/{userId}")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("Integration - Follow relationships work across databases")
    public void testIntegration_FollowRelationshipsAcrossDatabases() {
        // Create 3 users in both databases
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();
        UUID user3Id = UUID.randomUUID();

        createUserInPostgres(user1Id, "Alice");
        createUserInPostgres(user2Id, "Bob");
        createUserInPostgres(user3Id, "Charlie");

        createUserInNeo4j(user1Id.toString(), "Alice");
        createUserInNeo4j(user2Id.toString(), "Bob");
        createUserInNeo4j(user3Id.toString(), "Charlie");

        // Create follow graph: Alice -> Bob -> Charlie
        createFollowRelationship(user1Id.toString(), user2Id.toString());
        createFollowRelationship(user2Id.toString(), user3Id.toString());

        // Verify Alice follows Bob
        given()
                .pathParam("userId", user1Id.toString())
                .get("/api/users/following/{userId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].displayName", equalTo("Bob"));

        // Verify Bob has Alice as follower
        given()
                .pathParam("userId", user2Id.toString())
                .get("/api/users/followers/{userId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].displayName", equalTo("Alice"));

        // Verify Bob follows Charlie
        given()
                .pathParam("userId", user2Id.toString())
                .get("/api/users/following/{userId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].displayName", equalTo("Charlie"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("Integration - Mutual following")
    public void testIntegration_MutualFollowing() {
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();

        createUserInPostgres(user1Id, "Friend1");
        createUserInPostgres(user2Id, "Friend2");

        createUserInNeo4j(user1Id.toString(), "Friend1");
        createUserInNeo4j(user2Id.toString(), "Friend2");

        // Create mutual following
        createFollowRelationship(user1Id.toString(), user2Id.toString());
        createFollowRelationship(user2Id.toString(), user1Id.toString());

        // Verify Friend1's relationships
        given()
                .pathParam("userId", user1Id.toString())
                .get("/api/users/following/{userId}")
                .then()
                .body("size()", equalTo(1))
                .body("[0].displayName", equalTo("Friend2"));

        given()
                .pathParam("userId", user1Id.toString())
                .get("/api/users/followers/{userId}")
                .then()
                .body("size()", equalTo(1))
                .body("[0].displayName", equalTo("Friend2"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"user"})
    @DisplayName("Integration - Complex follow network")
    public void testIntegration_ComplexFollowNetwork() {
        // Create 5 users
        UUID[] userIds = new UUID[5];
        String[] names = {"Alice", "Bob", "Charlie", "Diana", "Eve"};

        for (int i = 0; i < 5; i++) {
            userIds[i] = UUID.randomUUID();
            createUserInPostgres(userIds[i], names[i]);
            createUserInNeo4j(userIds[i].toString(), names[i]);
        }

        // Create follow network:
        // Alice follows everyone
        // Bob follows Charlie and Diana
        // Charlie follows Diana
        createFollowRelationship(userIds[0].toString(), userIds[1].toString());
        createFollowRelationship(userIds[0].toString(), userIds[2].toString());
        createFollowRelationship(userIds[0].toString(), userIds[3].toString());
        createFollowRelationship(userIds[0].toString(), userIds[4].toString());

        createFollowRelationship(userIds[1].toString(), userIds[2].toString());
        createFollowRelationship(userIds[1].toString(), userIds[3].toString());

        createFollowRelationship(userIds[2].toString(), userIds[3].toString());

        // Verify Alice follows 4 people
        given()
                .pathParam("userId", userIds[0].toString())
                .get("/api/users/following/{userId}")
                .then()
                .body("size()", equalTo(4));

        // Verify Diana has 3 followers
        given()
                .pathParam("userId", userIds[3].toString())
                .get("/api/users/followers/{userId}")
                .then()
                .body("size()", equalTo(3))
                .body("displayName", hasItems("Alice", "Bob", "Charlie"));
    }

    // ==================== Neo4j Specific Tests ====================

    @Test
    @DisplayName("Neo4j - Follow relationship persists correctly")
    public void testNeo4j_FollowRelationshipPersists() {
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();

        createUserInNeo4j(user1Id.toString(), "User1");
        createUserInNeo4j(user2Id.toString(), "User2");

        createFollowRelationship(user1Id.toString(), user2Id.toString());

        // Verify in Neo4j directly
        long count = countFollowRelationships(user1Id.toString(), user2Id.toString());
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Neo4j - Can query user node directly")
    public void testNeo4j_CanQueryUserNode() {
        UUID userId = UUID.randomUUID();
        createUserInNeo4j(userId.toString(), "TestUser");

        UserNode found = getUserNodeFromNeo4j(userId.toString());
        assertNotNull(found);
        assertEquals(userId.toString(), found.getUserId());
        assertEquals("TestUser", found.getDisplayName());
    }
}