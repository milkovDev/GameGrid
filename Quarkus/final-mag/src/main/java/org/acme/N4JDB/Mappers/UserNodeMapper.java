package org.acme.N4JDB.Mappers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.N4JDB.DTOs.UserNodeDTO;
import org.acme.N4JDB.Nodes.MessageNode;
import org.acme.N4JDB.Nodes.NotificationNode;
import org.acme.N4JDB.Nodes.UserNode;
import org.acme.N4JDB.Repositories.UserNodeRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserNodeMapper {

    @Inject
    UserNodeRepository userNodeRepository;

    public UserNodeDTO toDTO(UserNode userNode) {
        if (userNode == null) {
            return null;
        }

        UserNodeDTO dto = new UserNodeDTO();
        dto.setId(userNode.getId());
        dto.setUserId(userNode.getUserId());
        dto.setDisplayName(userNode.getDisplayName());

        Set<String> following = userNode.getFollowing() != null
                ? userNode.getFollowing().stream()
                .map(UserNode::getUserId)
                .collect(Collectors.toSet())
                : new HashSet<>();
        dto.setFollowing(following);

        Set<String> followers = userNode.getFollowers() != null
                ? userNode.getFollowers().stream()
                .map(UserNode::getUserId)
                .collect(Collectors.toSet())
                : new HashSet<>();
        dto.setFollowers(followers);

        return dto;
    }

    public UserNode toEntity(UserNodeDTO dto) {
        if (dto == null) {
            return null;
        }

        UserNode userNode = new UserNode();
        userNode.setUserId(dto.getUserId());
        userNode.setDisplayName(dto.getDisplayName());

        if (dto.getFollowing() != null) {
            Set<UserNode> following = dto.getFollowing().stream()
                    .map(userNodeRepository::findById)
                    .filter(follow -> follow != null)
                    .collect(Collectors.toSet());
            userNode.setFollowing(following);
        }

        if (dto.getFollowers() != null) {
            Set<UserNode> followers = dto.getFollowers().stream()
                    .map(userNodeRepository::findById)
                    .filter(follow -> follow != null)
                    .collect(Collectors.toSet());
            userNode.setFollowers(followers);
        }

        return userNode;
    }
}