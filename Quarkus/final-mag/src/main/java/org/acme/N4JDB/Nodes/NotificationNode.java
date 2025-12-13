package org.acme.N4JDB.Nodes;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.time.Instant;

@NodeEntity
public class NotificationNode {
    @Id
    @GeneratedValue
    private Long id;
    private String content;
    private Instant createdAt;
    private Boolean read = false;

    @Relationship(type = "FOR", direction = Relationship.Direction.OUTGOING)
    private UserNode target;

    // Constructors
    public NotificationNode() {}

    public NotificationNode(Long id, String content, Instant createdAt, Boolean read) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.read = read;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
    public UserNode getTarget() { return target; }
    public void setTarget(UserNode target) { this.target = target; }
}
