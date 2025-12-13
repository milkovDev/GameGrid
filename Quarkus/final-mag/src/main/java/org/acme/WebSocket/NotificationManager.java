package org.acme.WebSocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.N4JDB.DTOs.NotificationNodeDTO;
import org.acme.N4JDB.Nodes.UserNode;
import org.acme.Services.NotificationNodeService;
import org.acme.Services.UserNodeService;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class NotificationManager {

    @Inject
    NotificationNodeService notificationNodeService;

    @Inject
    UserNodeService userNodeService;

    public void notifyFollow(String followerId, String followedId, String followerDisplayName) {
        NotificationNodeDTO dto = new NotificationNodeDTO();
        dto.setTargetId(followedId);
        dto.setContent(followerDisplayName + " followed you");
        dto.setCreatedAt(Instant.now());
        dto.setRead(false);
        notificationNodeService.create(dto);
    }

    public void notifyUnfollow(String followerId, String followedId, String followerDisplayName) {
        NotificationNodeDTO dto = new NotificationNodeDTO();
        dto.setTargetId(followedId);
        dto.setContent(followerDisplayName + " unfollowed you");
        dto.setCreatedAt(Instant.now());
        dto.setRead(false);
        notificationNodeService.create(dto);
    }

    public void notifyGameAdded(String userId, String userDisplayName, String gameName) {
        Set<UserNode> followers = userNodeService.getFollowers(userId);

        NotificationNodeDTO dto = new NotificationNodeDTO();
        dto.setContent(userDisplayName + " added \"" + gameName + "\" to their list");
        dto.setCreatedAt(Instant.now());
        dto.setRead(false);

        for (UserNode follower : followers) {
            dto.setTargetId(follower.getUserId());
            notificationNodeService.create(dto);
        }
    }

    public void notifyArticlePublished(String authorId, String authorDisplayName, String articleTitle) {
        Set<UserNode> followers = userNodeService.getFollowers(authorId);

        NotificationNodeDTO dto = new NotificationNodeDTO();
        dto.setContent(authorDisplayName + " published an article: \"" + articleTitle + "\"");
        dto.setCreatedAt(Instant.now());
        dto.setRead(false);

        for (UserNode follower : followers) {
            dto.setTargetId(follower.getUserId());
            notificationNodeService.create(dto);
        }
    }
}
