package org.acme.Resources;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.acme.PGDB.DTOs.DeveloperDTO;
import org.acme.PGDB.Entities.Developer;
import org.acme.profiles.TestContainersProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(TestContainersProfile.class)
@DisplayName("Developer Resource Integration Tests")
public class DeveloperResourceIntegrationTest {

    @BeforeEach
    @Transactional
    public void cleanup() {
        // Clean database before each test
        Developer.deleteAll();
    }

    // ==================== POST /api/developers/create Tests ====================

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("POST /create - Success as superuser")
    public void testCreate_AsSuperuser_Success() {
        DeveloperDTO newDeveloper = new DeveloperDTO();
        newDeveloper.setName("Naughty Dog");

        given()
                .contentType(ContentType.JSON)
                .body(newDeveloper)
                .when()
                .post("/api/developers/create")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("name", equalTo("Naughty Dog"))
                .body("id", notNullValue());

        // Verify it's actually in the PostgreSQL database
        Developer found = Developer.find("name", "Naughty Dog").firstResult();
        assertNotNull(found, "Developer should exist in database");
        assertEquals("Naughty Dog", found.getName());
        assertNotNull(found.getId(), "ID should be generated");
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("POST /create - Data persisted correctly")
    public void testCreate_DataPersistedInDatabase() {
        DeveloperDTO newDeveloper = new DeveloperDTO();
        newDeveloper.setName("CD Projekt Red");

        // FIX: Extract as Integer first, then convert to Long
        Integer createdIdInt = given()
                .contentType(ContentType.JSON)
                .body(newDeveloper)
                .when()
                .post("/api/developers/create")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Long createdId = createdIdInt.longValue();

        // Verify in database
        Developer found = Developer.findById(createdId);
        assertNotNull(found);
        assertEquals("CD Projekt Red", found.getName());
        assertEquals(createdId, found.getId());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("POST /create - Duplicate name conflict")
    public void testCreate_DuplicateName_Conflict() {
        // First, create a developer via the API
        DeveloperDTO first = new DeveloperDTO();
        first.setName("Rockstar Games");

        given()
                .contentType(ContentType.JSON)
                .body(first)
                .when()
                .post("/api/developers/create")
                .then()
                .statusCode(201);

        // Try to create duplicate via API
        DeveloperDTO duplicate = new DeveloperDTO();
        duplicate.setName("Rockstar Games");

        given()
                .contentType(ContentType.JSON)
                .body(duplicate)
                .when()
                .post("/api/developers/create")
                .then()
                .statusCode(409); // Conflict

        // Verify only one exists in database
        long count = Developer.count("name", "Rockstar Games");
        assertEquals(1, count, "Only one developer with this name should exist");
    }

    @Test
    @TestSecurity(user = "regularuser", roles = {"user"})
    @DisplayName("POST /create - Forbidden for regular user")
    public void testCreate_AsRegularUser_Forbidden() {
        DeveloperDTO newDeveloper = new DeveloperDTO();
        newDeveloper.setName("Test Studio");

        given()
                .contentType(ContentType.JSON)
                .body(newDeveloper)
                .when()
                .post("/api/developers/create")
                .then()
                .statusCode(403); // Forbidden

        // Verify nothing was created in database
        assertEquals(0, Developer.count());
    }

    @Test
    @DisplayName("POST /create - Unauthorized without authentication")
    public void testCreate_Unauthenticated_Unauthorized() {
        DeveloperDTO newDeveloper = new DeveloperDTO();
        newDeveloper.setName("Test Studio");

        given()
                .contentType(ContentType.JSON)
                .body(newDeveloper)
                .when()
                .post("/api/developers/create")
                .then()
                .statusCode(401); // Unauthorized

        // Verify nothing was created
        assertEquals(0, Developer.count());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("POST /create - Name with special characters")
    public void testCreate_NameWithSpecialCharacters_Success() {
        DeveloperDTO developer = new DeveloperDTO();
        developer.setName("Ubisoft Montréal");

        given()
                .contentType(ContentType.JSON)
                .body(developer)
                .when()
                .post("/api/developers/create")
                .then()
                .statusCode(201)
                .body("name", equalTo("Ubisoft Montréal"));

        // Verify in database
        Developer found = Developer.find("name", "Ubisoft Montréal").firstResult();
        assertNotNull(found);
        assertEquals("Ubisoft Montréal", found.getName());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("POST /create - Maximum length name (255 chars)")
    public void testCreate_MaxLengthName_Success() {
        DeveloperDTO developer = new DeveloperDTO();
        String maxLengthName = "A".repeat(255);
        developer.setName(maxLengthName);

        given()
                .contentType(ContentType.JSON)
                .body(developer)
                .when()
                .post("/api/developers/create")
                .then()
                .statusCode(201)
                .body("name", equalTo(maxLengthName));

        // Verify in database
        Developer found = Developer.find("name", maxLengthName).firstResult();
        assertNotNull(found);
        assertEquals(255, found.getName().length());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("POST /create - Sequence generates unique IDs")
    public void testCreate_SequenceGeneration_UniqueIds() {
        DeveloperDTO dev1 = new DeveloperDTO();
        dev1.setName("Developer One");

        DeveloperDTO dev2 = new DeveloperDTO();
        dev2.setName("Developer Two");

        DeveloperDTO dev3 = new DeveloperDTO();
        dev3.setName("Developer Three");

        // FIX: Extract as Integer, then convert to Long
        Integer id1Int = given()
                .contentType(ContentType.JSON)
                .body(dev1)
                .when()
                .post("/api/developers/create")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Integer id2Int = given()
                .contentType(ContentType.JSON)
                .body(dev2)
                .when()
                .post("/api/developers/create")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Integer id3Int = given()
                .contentType(ContentType.JSON)
                .body(dev3)
                .when()
                .post("/api/developers/create")
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

    // ==================== GET /api/developers/getAll Tests ====================

    @Test
    @TestSecurity(user = "regularuser", roles = {"user"})
    @DisplayName("GET /getAll - Success as user")
    public void testGetAll_AsUser_Success() {
        given()
                .when()
                .get("/api/developers/getAll")
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
                .get("/api/developers/getAll")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("GET /getAll - Unauthorized without authentication")
    public void testGetAll_Unauthenticated_Unauthorized() {
        given()
                .when()
                .get("/api/developers/getAll")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "wrongroleuser", roles = {"viewer"})
    @DisplayName("GET /getAll - Forbidden with wrong role")
    public void testGetAll_WrongRole_Forbidden() {
        given()
                .when()
                .get("/api/developers/getAll")
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "regularuser", roles = {"user"})
    @DisplayName("GET /getAll - Empty database returns empty list")
    public void testGetAll_EmptyDatabase_ReturnsEmptyList() {
        given()
                .when()
                .get("/api/developers/getAll")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("GET /getAll - Returns all persisted data")
    public void testGetAll_ReturnsPersistedData() {
        // FIX: Create via API instead of @Transactional
        DeveloperDTO dev1 = new DeveloperDTO();
        dev1.setName("CD Projekt Red");
        given().contentType(ContentType.JSON).body(dev1).post("/api/developers/create");

        DeveloperDTO dev2 = new DeveloperDTO();
        dev2.setName("FromSoftware");
        given().contentType(ContentType.JSON).body(dev2).post("/api/developers/create");

        DeveloperDTO dev3 = new DeveloperDTO();
        dev3.setName("Rockstar Games");
        given().contentType(ContentType.JSON).body(dev3).post("/api/developers/create");

        // Query via API
        given()
                .when()
                .get("/api/developers/getAll")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(3))
                .body("name", hasItems("CD Projekt Red", "FromSoftware", "Rockstar Games"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("GET /getAll - Returns data with IDs")
    public void testGetAll_ReturnsDataWithIds() {
        // FIX: Create via API
        DeveloperDTO dev1 = new DeveloperDTO();
        dev1.setName("Bethesda");
        given().contentType(ContentType.JSON).body(dev1).post("/api/developers/create");

        DeveloperDTO dev2 = new DeveloperDTO();
        dev2.setName("Activision");
        given().contentType(ContentType.JSON).body(dev2).post("/api/developers/create");

        // Query via API
        given()
                .when()
                .get("/api/developers/getAll")
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
        // FIX: Create via API instead of direct persistence
        for (int i = 1; i <= 50; i++) {
            DeveloperDTO dev = new DeveloperDTO();
            dev.setName("Developer " + i);
            given().contentType(ContentType.JSON).body(dev).post("/api/developers/create");
        }

        // Query via API
        given()
                .when()
                .get("/api/developers/getAll")
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
        DeveloperDTO newDeveloper = new DeveloperDTO();
        newDeveloper.setName("Valve");

        given()
                .contentType(ContentType.JSON)
                .body(newDeveloper)
                .when()
                .post("/api/developers/create")
                .then()
                .statusCode(201);

        given()
                .when()
                .get("/api/developers/getAll")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].name", equalTo("Valve"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("Integration - Multiple creates then retrieve all")
    public void testIntegration_MultipleCreatesThenRetrieve() {
        String[] developers = {"Nintendo", "Sony", "Microsoft", "Sega"};

        for (String name : developers) {
            DeveloperDTO dev = new DeveloperDTO();
            dev.setName(name);

            given()
                    .contentType(ContentType.JSON)
                    .body(dev)
                    .when()
                    .post("/api/developers/create")
                    .then()
                    .statusCode(201);
        }

        given()
                .when()
                .get("/api/developers/getAll")
                .then()
                .statusCode(200)
                .body("size()", equalTo(4))
                .body("name", hasItems("Nintendo", "Sony", "Microsoft", "Sega"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"superuser"})
    @DisplayName("Integration - Create, duplicate attempt, then retrieve")
    public void testIntegration_CreateDuplicateAttemptThenRetrieve() {
        DeveloperDTO dev = new DeveloperDTO();
        dev.setName("Epic Games");

        given()
                .contentType(ContentType.JSON)
                .body(dev)
                .when()
                .post("/api/developers/create")
                .then()
                .statusCode(201);

        // Try duplicate
        given()
                .contentType(ContentType.JSON)
                .body(dev)
                .when()
                .post("/api/developers/create")
                .then()
                .statusCode(409);

        // Verify only one exists
        given()
                .when()
                .get("/api/developers/getAll")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].name", equalTo("Epic Games"));
    }
}