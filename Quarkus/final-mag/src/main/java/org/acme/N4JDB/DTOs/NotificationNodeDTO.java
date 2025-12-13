package org.acme.N4JDB.DTOs;

import java.time.Instant;
import java.util.UUID;

public class NotificationNodeDTO {
    private Long id;
    private String content;
    private Instant createdAt;
    private Boolean read;
    private String targetId;

    public NotificationNodeDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
}
