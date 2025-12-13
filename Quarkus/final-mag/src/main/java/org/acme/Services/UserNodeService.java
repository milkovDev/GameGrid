package org.acme.Services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.N4JDB.Nodes.UserNode;
import org.acme.N4JDB.Repositories.UserNodeRepository;
import org.acme.WebSocket.NotificationManager;

import java.util.Set;

@ApplicationScoped
public class UserNodeService {

    @Inject
    UserNodeRepository userNodeRepository;

    @Inject
    NotificationManager notificationManager;

    public void createUserNode(String userId, String displayName) {
        UserNode userNode = new UserNode(userId, displayName);
        userNodeRepository.save(userNode);
    }

    public boolean follow(String followerId, String followedId) {
        if (followerId.equals(followedId)) {
            return false; // User cannot follow themselves
        }

        UserNode follower = userNodeRepository.findById(followerId);
        UserNode followed = userNodeRepository.findById(followedId);

        if (follower == null || followed == null) {
            return false; // One or both users don't exist
        }

        if (follower.getFollowing().contains(followed)) {
            return false; // Already following
        }

        follower.getFollowing().add(followed);
        followed.getFollowers().add(follower);

        userNodeRepository.save(follower);
        userNodeRepository.save(followed);

        // Create notification for the followed user
        notificationManager.notifyFollow(followerId, followedId, follower.getDisplayName());

        return true;
    }

    public boolean unfollow(String followerId, String followedId) {
        UserNode follower = userNodeRepository.findById(followerId);
        UserNode followed = userNodeRepository.findById(followedId);


        if (follower == null || followed == null) {
            return false; // One or both users don't exist
        }

        // Delete the FOLLOWS relationship directly in Neo4j
        boolean relationshipDeleted = userNodeRepository.deleteFollowsRelationship(followerId, followedId);
        if (!relationshipDeleted) {
            return false; // Not following
        }

        // Create notification for the followed user
        notificationManager.notifyUnfollow(followerId, followedId, follower.getDisplayName());

        return true;
    }

    public UserNode getById(String userId) {
        return userNodeRepository.findById(userId);
    }

    public Set<UserNode> getFollowers(String userId) {
        return userNodeRepository.getFollowers(userId);
    }
}
