package org.acme.N4JDB.Mappers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.N4JDB.DTOs.NotificationNodeDTO;
import org.acme.N4JDB.Nodes.NotificationNode;
import org.acme.N4JDB.Nodes.UserNode;
import org.acme.N4JDB.Repositories.UserNodeRepository;

@ApplicationScoped
public class NotificationNodeMapper {

    @Inject
    UserNodeRepository userNodeRepository;

    public NotificationNodeDTO toDTO(NotificationNode notificationNode) {
        if (notificationNode == null) {
            return null;
        }

        NotificationNodeDTO dto = new NotificationNodeDTO();
        dto.setId(notificationNode.getId());
        dto.setContent(notificationNode.getContent());
        dto.setCreatedAt(notificationNode.getCreatedAt());
        dto.setRead(notificationNode.getRead());
        dto.setTargetId(notificationNode.getTarget() != null ? notificationNode.getTarget().getUserId() : null);

        return dto;
    }

    public NotificationNode toEntity(NotificationNodeDTO dto) {
        if (dto == null) {
            return null;
        }

        NotificationNode notificationNode = new NotificationNode();
        notificationNode.setContent(dto.getContent());
        notificationNode.setCreatedAt(dto.getCreatedAt());
        notificationNode.setRead(dto.getRead());

        if (dto.getTargetId() != null) {
            UserNode target = userNodeRepository.findById(dto.getTargetId());
            notificationNode.setTarget(target);
        }

        return notificationNode;
    }
}
