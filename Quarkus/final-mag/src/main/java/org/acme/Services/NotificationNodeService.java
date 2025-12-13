package org.acme.Services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.websocket.Session;
import jakarta.ws.rs.NotFoundException;
import org.acme.N4JDB.DTOs.NotificationNodeDTO;
import org.acme.N4JDB.Mappers.NotificationNodeMapper;
import org.acme.N4JDB.Nodes.NotificationNode;
import org.acme.N4JDB.Repositories.NotificationNodeRepository;
import org.acme.WebSocket.OnlineUserManager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class NotificationNodeService {

    @Inject
    NotificationNodeMapper notificationNodeMapper;

    @Inject
    NotificationNodeRepository notificationNodeRepository;

    @Inject
    OnlineUserManager onlineUserManager;

    private final Jsonb jsonb = JsonbBuilder.create();

    public NotificationNodeDTO create(NotificationNodeDTO dto) {
        NotificationNode entity = notificationNodeMapper.toEntity(dto);
        if (entity.getTarget() == null) {
            throw new IllegalArgumentException("Target ID is required");
        }
        notificationNodeRepository.save(entity);

        NotificationNodeDTO created = notificationNodeMapper.toDTO(entity);

        // Push to target if online
        Session targetSession = onlineUserManager.getSession(dto.getTargetId());
        if (targetSession != null && targetSession.isOpen()) {
            Map<String, Object> response = Map.of(
                    "type", "new_notification",
                    "data", created
            );
            String json = jsonb.toJson(response);
            targetSession.getAsyncRemote().sendText(json);
        }

        return created;
    }

    public void markAsRead(Long id) {
        NotificationNode entity = notificationNodeRepository.findById(id);
        if (entity == null) {
            throw new NotFoundException("Notification not found");
        }
        entity.setRead(true);
        notificationNodeRepository.save(entity);
    }

    public void delete(Long id) {
        notificationNodeRepository.delete(id);
    }

    public List<NotificationNodeDTO> getNotificationsForUser(String targetId) {
        return notificationNodeRepository.findByTargetId(targetId).stream()
                .map(notificationNodeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationNodeDTO> getUnreadForUser(String userId) {
        return notificationNodeRepository.findUnreadForUser(userId).stream()
                .map(notificationNodeMapper::toDTO)
                .collect(Collectors.toList());
    }
}
