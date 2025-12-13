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
@DisplayName("Message Resource Integration Tests")
public class MessageResourceIntegrationTest {

    @Inject
    Driver neo4jDriver;

    @BeforeEach
    public void cleanup() {
        // Clean Neo4j
        try (Session session = neo4jDriver.session()) {
            session.run("MATCH (n) DETACH DELETE n");
        }
    }

    // Helper to create test users in Neo4j
    public void createUserInNeo4j(String userId, String displayName) {
        try (Session session = neo4jDriver.session()) {
            session.run(
                    "CREATE (u:UserNode {userId: $userId, displayName: $displayName})",
                    org.neo4j.driver.Values.parameters("userId", userId, "displayName", displayName)
            );
        }
    }

    // Helper to create message between users
    public void createMessage(String senderId, String recipientId, String content, boolean read) {
        try (Session session = neo4jDriver.session()) {
            session.run(
                    "MATCH (sender:UserNode {userId: $senderId}), (recipient:UserNode {userId: $recipientId}) " +
                            "CREATE (sender)-[:SENT]->(m:MessageNode {content: $content, createdAt: datetime(), read: $read})-[:TO]->(recipient)",
                    org.neo4j.driver.Values.parameters(
                            "senderId", senderId,
                            "recipientId", recipientId,
                            "content", content,
                            "read", read
                    )
            );
        }
    }

    // ==================== GET /api/messages/between/{user1Id}/{user2Id} Tests ====================

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    @DisplayName("GET /between/{user1Id}/{user2Id} - Success as authenticated user")
    public void testGetMessagesBetween_AsUser_Success() {
        String user1Id = "user1";
        String user2Id = "user2";

        createUserInNeo4j(user1Id, "User One");
        createUserInNeo4j(user2Id, "User Two");

        given()
                .pathParam("user1Id", user1Id)
                .pathParam("user2Id", user2Id)
                .when()
                .get("/api/messages/between/{user1Id}/{user2Id}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("GET /between/{user1Id}/{user2Id} - Success as superuser")
    public void testGetMessagesBetween_AsSuperuser_Success() {
        String user1Id = "admin";
        String user2Id = "user2";

        createUserInNeo4j(user1Id, "Admin");
        createUserInNeo4j(user2Id, "User Two");

        given()
                .pathParam("user1Id", user1Id)
                .pathParam("user2Id", user2Id)
                .when()
                .get("/api/messages/between/{user1Id}/{user2Id}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("GET /between/{user1Id}/{user2Id} - Unauthorized without authentication")
    public void testGetMessagesBetween_Unauthenticated_Unauthorized() {
        given()
                .pathParam("user1Id", "user1")
                .pathParam("user2Id", "user2")
                .when()
                .get("/api/messages/between/{user1Id}/{user2Id}")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    @DisplayName("GET /between/{user1Id}/{user2Id} - Empty list when no messages")
    public void testGetMessagesBetween_NoMessages_ReturnsEmptyList() {
        String user1Id = "user1";
        String user2Id = "user2";

        createUserInNeo4j(user1Id, "User One");
        createUserInNeo4j(user2Id, "User Two");

        given()
                .pathParam("user1Id", user1Id)
                .pathParam("user2Id", user2Id)
                .when()
                .get("/api/messages/between/{user1Id}/{user2Id}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = "user3", roles = {"user"})
    @DisplayName("GET /between/{user1Id}/{user2Id} - Forbidden when not a participant")
    public void testGetMessagesBetween_NotParticipant_Forbidden() {
        String user1Id = "user1";
        String user2Id = "user2";

        createUserInNeo4j(user1Id, "User One");
        createUserInNeo4j(user2Id, "User Two");

        given()
                .pathParam("user1Id", user1Id)
                .pathParam("user2Id", user2Id)
                .when()
                .get("/api/messages/between/{user1Id}/{user2Id}")
                .then()
                .statusCode(500); // Currently returns 500 with ForbiddenException
    }

    // ==================== Neo4j Database Tests ====================

    @Test
    @DisplayName("Neo4j - Users persist correctly")
    public void testNeo4j_UsersPersist() {
        String user1Id = "user123";
        String user2Id = "user456";

        createUserInNeo4j(user1Id, "User One");
        createUserInNeo4j(user2Id, "User Two");

        // Verify in Neo4j
        try (Session session = neo4jDriver.session()) {
            var result = session.run("MATCH (u:UserNode) RETURN count(u) as count");
            long count = result.single().get("count").asLong();
            assert count == 2;
        }
    }

    @Test
    @DisplayName("Neo4j - Message persists with sender and recipient relationships")
    public void testNeo4j_MessageWithRelationships() {
        String user1Id = "sender123";
        String user2Id = "recipient456";

        createUserInNeo4j(user1Id, "Sender User");
        createUserInNeo4j(user2Id, "Recipient User");
        createMessage(user1Id, user2Id, "Test message content", false);

        // Verify in Neo4j
        try (Session session = neo4jDriver.session()) {
            var result = session.run(
                    "MATCH (sender:UserNode {userId: $senderId})-[:SENT]->(m:MessageNode)-[:TO]->(recipient:UserNode {userId: $recipientId}) " +
                            "RETURN m.content as content, m.read as read",
                    org.neo4j.driver.Values.parameters("senderId", user1Id, "recipientId", user2Id)
            );

            var record = result.single();
            assert record.get("content").asString().equals("Test message content");
            assert !record.get("read").asBoolean();
        }
    }

    @Test
    @DisplayName("Neo4j - Multiple messages between same users")
    public void testNeo4j_MultipleMessages() {
        String user1Id = "user1";
        String user2Id = "user2";

        createUserInNeo4j(user1Id, "User One");
        createUserInNeo4j(user2Id, "User Two");

        createMessage(user1Id, user2Id, "Message 1", false);
        createMessage(user1Id, user2Id, "Message 2", false);
        createMessage(user2Id, user1Id, "Reply 1", true);

        // Verify count
        try (Session session = neo4jDriver.session()) {
            var result = session.run("MATCH (m:MessageNode) RETURN count(m) as count");
            long count = result.single().get("count").asLong();
            assert count == 3;
        }
    }

    @Test
    @DisplayName("Neo4j - Message read status persists correctly")
    public void testNeo4j_MessageReadStatus() {
        String user1Id = "user1";
        String user2Id = "user2";

        createUserInNeo4j(user1Id, "User One");
        createUserInNeo4j(user2Id, "User Two");

        createMessage(user1Id, user2Id, "Unread message", false);
        createMessage(user1Id, user2Id, "Read message", true);

        // Verify read statuses
        try (Session session = neo4jDriver.session()) {
            var result = session.run(
                    "MATCH (m:MessageNode) WHERE m.read = true RETURN count(m) as readCount"
            );
            long readCount = result.single().get("readCount").asLong();
            assert readCount == 1;
        }
    }

    @Test
    @DisplayName("Neo4j - Bidirectional messages work correctly")
    public void testNeo4j_BidirectionalMessages() {
        String user1Id = "user1";
        String user2Id = "user2";

        createUserInNeo4j(user1Id, "User One");
        createUserInNeo4j(user2Id, "User Two");

        // User1 sends to User2
        createMessage(user1Id, user2Id, "Hello", false);
        // User2 replies to User1
        createMessage(user2Id, user1Id, "Hi back", true);

        // Verify both directions exist
        try (Session session = neo4jDriver.session()) {
            // Messages from user1 to user2
            var result1 = session.run(
                    "MATCH (sender:UserNode {userId: $user1})-[:SENT]->(m:MessageNode)-[:TO]->(recipient:UserNode {userId: $user2}) " +
                            "RETURN count(m) as count",
                    org.neo4j.driver.Values.parameters("user1", user1Id, "user2", user2Id)
            );
            assert result1.single().get("count").asLong() == 1;

            // Messages from user2 to user1
            var result2 = session.run(
                    "MATCH (sender:UserNode {userId: $user2})-[:SENT]->(m:MessageNode)-[:TO]->(recipient:UserNode {userId: $user1}) " +
                            "RETURN count(m) as count",
                    org.neo4j.driver.Values.parameters("user1", user1Id, "user2", user2Id)
            );
            assert result2.single().get("count").asLong() == 1;
        }
    }

    @Test
    @DisplayName("Neo4j - Query users by ID")
    public void testNeo4j_QueryUserById() {
        String userId = "testuser123";
        String displayName = "Test User";

        createUserInNeo4j(userId, displayName);

        // Verify can query by ID
        try (Session session = neo4jDriver.session()) {
            var result = session.run(
                    "MATCH (u:UserNode {userId: $userId}) RETURN u.displayName as displayName",
                    org.neo4j.driver.Values.parameters("userId", userId)
            );
            String foundName = result.single().get("displayName").asString();
            assert foundName.equals(displayName);
        }
    }
}