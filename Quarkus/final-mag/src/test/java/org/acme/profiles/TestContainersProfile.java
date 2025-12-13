package org.acme.profiles;

import io.quarkus.test.junit.QuarkusTestProfile;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;

public class TestContainersProfile implements QuarkusTestProfile {

    static PostgreSQLContainer<?> postgres;
    static Neo4jContainer<?> neo4j;

    static {
        // Start PostgreSQL container using your existing image
        postgres = new PostgreSQLContainer<>("postgres:latest")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
        postgres.start();

        // Start Neo4j container
        neo4j = new Neo4jContainer<>("neo4j:5")
                .withAdminPassword("testpassword")
                .withoutAuthentication();
        neo4j.start();
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> config = new HashMap<>();

        // Disable Keycloak
        config.put("quarkus.oidc.enabled", "false");
        config.put("quarkus.keycloak.policy-enforcer.enable", "false");

        // Configure PostgreSQL
        config.put("quarkus.datasource.jdbc.url", postgres.getJdbcUrl());
        config.put("quarkus.datasource.username", postgres.getUsername());
        config.put("quarkus.datasource.password", postgres.getPassword());
        config.put("quarkus.datasource.db-kind", "postgresql");

        // Run migrations/schema creation for tests
        config.put("quarkus.hibernate-orm.database.generation", "drop-and-create");

        // Configure Neo4j
        config.put("quarkus.neo4j.uri", neo4j.getBoltUrl());
        config.put("quarkus.neo4j.authentication.username", "neo4j");
        config.put("quarkus.neo4j.authentication.password", "testpassword");

        return config;
    }
}