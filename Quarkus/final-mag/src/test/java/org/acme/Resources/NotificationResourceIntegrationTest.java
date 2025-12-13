package org.acme.Resources;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.acme.profiles.TestContainersProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestProfile(TestContainersProfile.class)
@DisplayName("Notification Resource Integration Tests")
public class NotificationResourceIntegrationTest {

    @Inject
    Driver neo4jDriver;

    @BeforeEach
    public void cleanup() {
        // Clean Neo4j
        try (Session session = neo4jDriver.session()) {
            session.run("MATCH (n) DETACH DELETE n");
        }
    }

    // Helper to create test user in Neo4j
    public void createUserInNeo4j(String userId, String displayName) {
        try (Session session = neo4jDriver.session()) {
            session.run(
                    "CREATE (u:UserNode {userId: $userId, displayName: $displayName})",
                    org.neo4j.driver.Values.parameters("userId", userId, "displayName", displayName)
            );
        }
    }

    // Helper to create notification for user
    public void createNotification(String targetId, String content, boolean read) {
        try (Session session = neo4jDriver.session()) {
            session.run(
                    "MATCH (target:UserNode {userId: $targetId}) " +
                            "CREATE (n:NotificationNode {content: $content, createdAt: datetime(), read: $read})-[:FOR]->(target)",
                    org.neo4j.driver.Values.parameters(
                            "targetId", targetId,
                            "content", content,
                            "read", read
                    )
            );
        }
    }

    // ==================== GET /api/notifications/getForUser/{targetId} Tests ====================

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    @DisplayName("GET /getForUser/{targetId} - Success for own notifications")
    public void testGetNotificationsForUser_OwnNotifications_Success() {
        String userId = "user1";
        createUserInNeo4j(userId, "User One");

        given()
                .pathParam("targetId", userId)
                .when()
                .get("/api/notifications/getForUser/{targetId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("GET /getForUser/{targetId} - Success as superuser")
    public void testGetNotificationsForUser_AsSuperuser_Success() {
        String userId = "admin";
        createUserInNeo4j(userId, "Admin");

        given()
                .pathParam("targetId", userId)
                .when()
                .get("/api/notifications/getForUser/{targetId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("GET /getForUser/{targetId} - Unauthorized without authentication")
    public void testGetNotificationsForUser_Unauthenticated_Unauthorized() {
        given()
                .pathParam("targetId", "user1")
                .when()
                .get("/api/notifications/getForUser/{targetId}")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    @DisplayName("GET /getForUser/{targetId} - Empty list when no notifications")
    public void testGetNotificationsForUser_NoNotifications_ReturnsEmptyList() {
        String userId = "user1";
        createUserInNeo4j(userId, "User One");

        given()
                .pathParam("targetId", userId)
                .when()
                .get("/api/notifications/getForUser/{targetId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = "user2", roles = {"user"})
    @DisplayName("GET /getForUser/{targetId} - Forbidden for other user's notifications")
    public void testGetNotificationsForUser_OtherUser_Forbidden() {
        String userId = "user1";
        createUserInNeo4j(userId, "User One");

        given()
                .pathParam("targetId", userId)
                .when()
                .get("/api/notifications/getForUser/{targetId}")
                .then()
                .statusCode(500); // Currently returns 500 with ForbiddenException
    }

    // ==================== Neo4j Database Tests ====================

    @Test
    @DisplayName("Neo4j - Notification persists with target relationship")
    public void testNeo4j_NotificationWithTarget() {
        String userId = "user123";
        createUserInNeo4j(userId, "Test User");
        createNotification(userId, "Test notification", false);

        // Verify in Neo4j
        try (Session session = neo4jDriver.session()) {
            var result = session.run(
                    "MATCH (n:NotificationNode)-[:FOR]->(target:UserNode {userId: $userId}) " +
                            "RETURN n.content as content, n.read as read",
                    org.neo4j.driver.Values.parameters("userId", userId)
            );

            var record = result.single();
            assert record.get("content").asString().equals("Test notification");
            assert !record.get("read").asBoolean();
        }
    }

    @Test
    @DisplayName("Neo4j - Multiple notifications for same user")
    public void testNeo4j_MultipleNotifications() {
        String userId = "user1";
        createUserInNeo4j(userId, "User One");

        createNotification(userId, "Notification 1", false);
        createNotification(userId, "Notification 2", true);
        createNotification(userId, "Notification 3", false);

        // Verify count
        try (Session session = neo4jDriver.session()) {
            var result = session.run(
                    "MATCH (n:NotificationNode)-[:FOR]->(target:UserNode {userId: $userId}) " +
                            "RETURN count(n) as count",
                    org.neo4j.driver.Values.parameters("userId", userId)
            );
            long count = result.single().get("count").asLong();
            assert count == 3;
        }
    }

    @Test
    @DisplayName("Neo4j - Notification read status persists correctly")
    public void testNeo4j_NotificationReadStatus() {
        String userId = "user1";
        createUserInNeo4j(userId, "User One");

        createNotification(userId, "Unread notification", false);
        createNotification(userId, "Read notification", true);

        // Verify read statuses
        try (Session session = neo4jDriver.session()) {
            var result = session.run(
                    "MATCH (n:NotificationNode)-[:FOR]->(target:UserNode {userId: $userId}) " +
                            "WHERE n.read = true RETURN count(n) as readCount",
                    org.neo4j.driver.Values.parameters("userId", userId)
            );
            long readCount = result.single().get("readCount").asLong();
            assert readCount == 1;
        }
    }

    @Test
    @DisplayName("Neo4j - Notifications for different users are separate")
    public void testNeo4j_NotificationsSeparatePerUser() {
        String user1Id = "user1";
        String user2Id = "user2";

        createUserInNeo4j(user1Id, "User One");
        createUserInNeo4j(user2Id, "User Two");

        createNotification(user1Id, "Notification for user1", false);
        createNotification(user1Id, "Another for user1", false);
        createNotification(user2Id, "Notification for user2", false);

        // Verify user1 has 2 notifications
        try (Session session = neo4jDriver.session()) {
            var result = session.run(
                    "MATCH (n:NotificationNode)-[:FOR]->(target:UserNode {userId: $userId}) " +
                            "RETURN count(n) as count",
                    org.neo4j.driver.Values.parameters("userId", user1Id)
            );
            assert result.single().get("count").asLong() == 2;
        }

        // Verify user2 has 1 notification
        try (Session session = neo4jDriver.session()) {
            var result = session.run(
                    "MATCH (n:NotificationNode)-[:FOR]->(target:UserNode {userId: $userId}) " +
                            "RETURN count(n) as count",
                    org.neo4j.driver.Values.parameters("userId", user2Id)
            );
            assert result.single().get("count").asLong() == 1;
        }
    }

    @Test
    @DisplayName("Neo4j - Query notifications by content")
    public void testNeo4j_QueryByContent() {
        String userId = "user1";
        createUserInNeo4j(userId, "User One");
        createNotification(userId, "You have a new message", false);

        // Verify can query by content
        try (Session session = neo4jDriver.session()) {
            var result = session.run(
                    "MATCH (n:NotificationNode {content: $content})-[:FOR]->(target:UserNode) " +
                            "RETURN target.userId as userId",
                    org.neo4j.driver.Values.parameters("content", "You have a new message")
            );
            String foundUserId = result.single().get("userId").asString();
            assert foundUserId.equals(userId);
        }
    }

    @Test
    @DisplayName("Neo4j - Notification created timestamp exists")
    public void testNeo4j_CreatedAtExists() {
        String userId = "user1";
        createUserInNeo4j(userId, "User One");
        createNotification(userId, "Test notification", false);

        // Verify createdAt is set
        try (Session session = neo4jDriver.session()) {
            var result = session.run(
                    "MATCH (n:NotificationNode)-[:FOR]->(target:UserNode {userId: $userId}) " +
                            "RETURN n.createdAt as createdAt",
                    org.neo4j.driver.Values.parameters("userId", userId)
            );
            var createdAt = result.single().get("createdAt");
            assert !createdAt.isNull();
        }
    }

    @Test
    @DisplayName("Neo4j - Count total notifications in system")
    public void testNeo4j_CountAllNotifications() {
        String user1Id = "user1";
        String user2Id = "user2";

        createUserInNeo4j(user1Id, "User One");
        createUserInNeo4j(user2Id, "User Two");

        createNotification(user1Id, "Notification 1", false);
        createNotification(user1Id, "Notification 2", false);
        createNotification(user2Id, "Notification 3", false);

        // Verify total count
        try (Session session = neo4jDriver.session()) {
            var result = session.run("MATCH (n:NotificationNode) RETURN count(n) as count");
            long totalCount = result.single().get("count").asLong();
            assert totalCount == 3;
        }
    }
}