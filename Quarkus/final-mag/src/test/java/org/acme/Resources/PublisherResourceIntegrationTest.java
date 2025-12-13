package org.acme.Resources;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.acme.PGDB.DTOs.PublisherDTO;
import org.acme.PGDB.Entities.Publisher;
import org.acme.profiles.TestContainersProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(TestContainersProfile.class)
@DisplayName("Publisher Resource Integration Tests")
public class PublisherResourceIntegrationTest {

    @BeforeEach
    @Transactional
    public void cleanup() {
        // Clean database before each test
        Publisher.deleteAll();
    }

    // ==================== POST /api/publishers/create Tests ====================

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("POST /create - Success as superuser")
    public void testCreate_AsSuperuser_Success() {
        PublisherDTO newPublisher = new PublisherDTO();
        newPublisher.setName("Electronic Arts");

        given()
                .contentType(ContentType.JSON)
                .body(newPublisher)
                .when()
                .post("/api/publishers/create")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("name", equalTo("Electronic Arts"))
                .body("id", notNullValue());

        // Verify it's actually in the PostgreSQL database
        Publisher found = Publisher.find("name", "Electronic Arts").firstResult();
        assertNotNull(found, "Publisher should exist in database");
        assertEquals("Electronic Arts", found.getName());
        assertNotNull(found.getId(), "ID should be generated");
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("POST /create - Data persisted correctly")
    public void testCreate_DataPersistedInDatabase() {
        PublisherDTO newPublisher = new PublisherDTO();
        newPublisher.setName("Activision Blizzard");

        Integer createdIdInt = given()
                .contentType(ContentType.JSON)
                .body(newPublisher)
                .when()
                .post("/api/publishers/create")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Long createdId = createdIdInt.longValue();

        // Verify in database
        Publisher found = Publisher.findById(createdId);
        assertNotNull(found);
        assertEquals("Activision Blizzard", found.getName());
        assertEquals(createdId, found.getId());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("POST /create - Duplicate name conflict")
    public void testCreate_DuplicateName_Conflict() {
        // First, create a publisher via the API
        PublisherDTO first = new PublisherDTO();
        first.setName("Bandai Namco");

        given()
                .contentType(ContentType.JSON)
                .body(first)
                .when()
                .post("/api/publishers/create")
                .then()
                .statusCode(201);

        // Try to create duplicate via API
        PublisherDTO duplicate = new PublisherDTO();
        duplicate.setName("Bandai Namco");

        given()
                .contentType(ContentType.JSON)
                .body(duplicate)
                .when()
                .post("/api/publishers/create")
                .then()
                .statusCode(409); // Conflict

        // Verify only one exists in database
        long count = Publisher.count("name", "Bandai Namco");
        assertEquals(1, count, "Only one publisher with this name should exist");
    }

    @Test
    @TestSecurity(user = "regularuser", roles = {"user"})
    @DisplayName("POST /create - Forbidden for regular user")
    public void testCreate_AsRegularUser_Forbidden() {
        PublisherDTO newPublisher = new PublisherDTO();
        newPublisher.setName("Test Publisher");

        given()
                .contentType(ContentType.JSON)
                .body(newPublisher)
                .when()
                .post("/api/publishers/create")
                .then()
                .statusCode(403); // Forbidden

        // Verify nothing was created in database
        assertEquals(0, Publisher.count());
    }

    @Test
    @DisplayName("POST /create - Unauthorized without authentication")
    public void testCreate_Unauthenticated_Unauthorized() {
        PublisherDTO newPublisher = new PublisherDTO();
        newPublisher.setName("Test Publisher");

        given()
                .contentType(ContentType.JSON)
                .body(newPublisher)
                .when()
                .post("/api/publishers/create")
                .then()
                .statusCode(401); // Unauthorized

        // Verify nothing was created
        assertEquals(0, Publisher.count());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("POST /create - Name with special characters")
    public void testCreate_NameWithSpecialCharacters_Success() {
        PublisherDTO publisher = new PublisherDTO();
        publisher.setName("Kōei Tecmo");

        given()
                .contentType(ContentType.JSON)
                .body(publisher)
                .when()
                .post("/api/publishers/create")
                .then()
                .statusCode(201)
                .body("name", equalTo("Kōei Tecmo"));

        // Verify in database
        Publisher found = Publisher.find("name", "Kōei Tecmo").firstResult();
        assertNotNull(found);
        assertEquals("Kōei Tecmo", found.getName());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("POST /create - Maximum length name (255 chars)")
    public void testCreate_MaxLengthName_Success() {
        PublisherDTO publisher = new PublisherDTO();
        String maxLengthName = "P".repeat(255);
        publisher.setName(maxLengthName);

        given()
                .contentType(ContentType.JSON)
                .body(publisher)
                .when()
                .post("/api/publishers/create")
                .then()
                .statusCode(201)
                .body("name", equalTo(maxLengthName));

        // Verify in database
        Publisher found = Publisher.find("name", maxLengthName).firstResult();
        assertNotNull(found);
        assertEquals(255, found.getName().length());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("POST /create - Sequence generates unique IDs")
    public void testCreate_SequenceGeneration_UniqueIds() {
        PublisherDTO pub1 = new PublisherDTO();
        pub1.setName("Publisher One");

        PublisherDTO pub2 = new PublisherDTO();
        pub2.setName("Publisher Two");

        PublisherDTO pub3 = new PublisherDTO();
        pub3.setName("Publisher Three");

        Integer id1Int = given()
                .contentType(ContentType.JSON)
                .body(pub1)
                .when()
                .post("/api/publishers/create")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Integer id2Int = given()
                .contentType(ContentType.JSON)
                .body(pub2)
                .when()
                .post("/api/publishers/create")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Integer id3Int = given()
                .contentType(ContentType.JSON)
                .body(pub3)
                .when()
                .post("/api/publishers/create")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Long id1 = id1Int.longValue();
        Long id2 = id2Int.longValue();
        Long id3 = id3Int.longValue();

        // Verify IDs are unique and sequential
        assertNotNull(id1);
        assertNotNull(id2);
        assertNotNull(id3);
        assertNotEquals(id1, id2);
        assertNotEquals(id2, id3);
        assertNotEquals(id1, id3);

        // With allocationSize=1, they should be sequential
        assertEquals(id1 + 1, id2, "IDs should be sequential");
        assertEquals(id2 + 1, id3, "IDs should be sequential");
    }

    // ==================== GET /api/publishers/getAll Tests ====================

    @Test
    @TestSecurity(user = "regularuser", roles = {"user"})
    @DisplayName("GET /getAll - Success as user")
    public void testGetAll_AsUser_Success() {
        given()
                .when()
                .get("/api/publishers/getAll")
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
                .get("/api/publishers/getAll")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("GET /getAll - Unauthorized without authentication")
    public void testGetAll_Unauthenticated_Unauthorized() {
        given()
                .when()
                .get("/api/publishers/getAll")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "wrongroleuser", roles = {"viewer"})
    @DisplayName("GET /getAll - Forbidden with wrong role")
    public void testGetAll_WrongRole_Forbidden() {
        given()
                .when()
                .get("/api/publishers/getAll")
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "regularuser", roles = {"user"})
    @DisplayName("GET /getAll - Empty database returns empty list")
    public void testGetAll_EmptyDatabase_ReturnsEmptyList() {
        given()
                .when()
                .get("/api/publishers/getAll")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("GET /getAll - Returns all persisted data")
    public void testGetAll_ReturnsPersistedData() {
        // Create via API
        PublisherDTO pub1 = new PublisherDTO();
        pub1.setName("Square Enix");
        given().contentType(ContentType.JSON).body(pub1).post("/api/publishers/create");

        PublisherDTO pub2 = new PublisherDTO();
        pub2.setName("Capcom");
        given().contentType(ContentType.JSON).body(pub2).post("/api/publishers/create");

        PublisherDTO pub3 = new PublisherDTO();
        pub3.setName("Sega");
        given().contentType(ContentType.JSON).body(pub3).post("/api/publishers/create");

        // Query via API
        given()
                .when()
                .get("/api/publishers/getAll")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(3))
                .body("name", hasItems("Square Enix", "Capcom", "Sega"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("GET /getAll - Returns data with IDs")
    public void testGetAll_ReturnsDataWithIds() {
        // Create via API
        PublisherDTO pub1 = new PublisherDTO();
        pub1.setName("Ubisoft");
        given().contentType(ContentType.JSON).body(pub1).post("/api/publishers/create");

        PublisherDTO pub2 = new PublisherDTO();
        pub2.setName("Take-Two Interactive");
        given().contentType(ContentType.JSON).body(pub2).post("/api/publishers/create");

        // Query via API
        given()
                .when()
                .get("/api/publishers/getAll")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(2))
                .body("[0].id", notNullValue())
                .body("[1].id", notNullValue())
                .body("[0].name", notNullValue())
                .body("[1].name", notNullValue());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("GET /getAll - Large dataset performance")
    public void testGetAll_LargeDataset() {
        // Create 50 publishers via API
        for (int i = 1; i <= 50; i++) {
            PublisherDTO pub = new PublisherDTO();
            pub.setName("Publisher " + i);
            given().contentType(ContentType.JSON).body(pub).post("/api/publishers/create");
        }

        // Query via API
        given()
                .when()
                .get("/api/publishers/getAll")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(50));
    }

    // ==================== Integration Tests ====================

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("Integration - Create then retrieve")
    public void testIntegration_CreateThenRetrieve() {
        PublisherDTO newPublisher = new PublisherDTO();
        newPublisher.setName("2K Games");

        given()
                .contentType(ContentType.JSON)
                .body(newPublisher)
                .when()
                .post("/api/publishers/create")
                .then()
                .statusCode(201);

        given()
                .when()
                .get("/api/publishers/getAll")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].name", equalTo("2K Games"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("Integration - Multiple creates then retrieve all")
    public void testIntegration_MultipleCreatesThenRetrieve() {
        String[] publishers = {"Nintendo", "Sony Interactive", "Xbox Game Studios", "Konami"};

        for (String name : publishers) {
            PublisherDTO pub = new PublisherDTO();
            pub.setName(name);

            given()
                    .contentType(ContentType.JSON)
                    .body(pub)
                    .when()
                    .post("/api/publishers/create")
                    .then()
                    .statusCode(201);
        }

        given()
                .when()
                .get("/api/publishers/getAll")
                .then()
                .statusCode(200)
                .body("size()", equalTo(4))
                .body("name", hasItems("Nintendo", "Sony Interactive", "Xbox Game Studios", "Konami"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("Integration - Create, duplicate attempt, then retrieve")
    public void testIntegration_CreateDuplicateAttemptThenRetrieve() {
        PublisherDTO pub = new PublisherDTO();
        pub.setName("Paradox Interactive");

        given()
                .contentType(ContentType.JSON)
                .body(pub)
                .when()
                .post("/api/publishers/create")
                .then()
                .statusCode(201);

        // Try duplicate
        given()
                .contentType(ContentType.JSON)
                .body(pub)
                .when()
                .post("/api/publishers/create")
                .then()
                .statusCode(409);

        // Verify only one exists
        given()
                .when()
                .get("/api/publishers/getAll")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].name", equalTo("Paradox Interactive"));
    }
}