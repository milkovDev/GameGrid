package org.acme.N4JDB.Nodes;

import org.neo4j.ogm.annotation.*;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class UserNode {
    @Id
    @GeneratedValue
    private Long id;
    @Index(unique = true)
    private String userId; // Keycloak ID
    private String displayName;

    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.OUTGOING)
    private Set<UserNode> following = new HashSet<>();

    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.INCOMING)
    private Set<UserNode> followers = new HashSet<>();

    @Relationship(type = "TO", direction = Relationship.Direction.INCOMING)
    private Set<MessageNode> receivedMessages = new HashSet<>();

    @Relationship(type = "FOR", direction = Relationship.Direction.INCOMING)
    private Set<NotificationNode> receivedNotifications = new HashSet<>();

    // Constructors
    public UserNode() {}

    public UserNode(String userId, String username) {
        this.userId = userId;
        this.displayName = username;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Set<UserNode> getFollowing() { return following; }
    public void setFollowing(Set<UserNode> following) { this.following = following; }
    public Set<UserNode> getFollowers() { return followers; }
    public void setFollowers(Set<UserNode> followers) { this.followers = followers; }
    public Set<MessageNode> getReceivedMessages() { return receivedMessages; }
    public void setReceivedMessages(Set<MessageNode> receivedMessages) { this.receivedMessages = receivedMessages; }
    public Set<NotificationNode> getReceivedNotifications() { return receivedNotifications; }
    public void setReceivedNotifications(Set<NotificationNode> receivedNotifications) { this.receivedNotifications = receivedNotifications; }
}