package org.acme.WebSocket;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.ws.rs.NotFoundException;
import org.acme.N4JDB.DTOs.MessageNodeDTO;
import org.acme.N4JDB.DTOs.NotificationNodeDTO;
import org.acme.N4JDB.Nodes.MessageNode;
import org.acme.N4JDB.Repositories.MessageNodeRepository;
import org.acme.Services.MessageNodeService;
import org.acme.Services.NotificationNodeService;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@ApplicationScoped
@ServerEndpoint("/ws/realtime/{userId}")
public class RealTimeWebSocket {

    @Inject
    MessageNodeService messageNodeService;

    @Inject
    NotificationNodeService notificationNodeService;

    @Inject
    OnlineUserManager onlineUserManager;

    @Inject
    WebSocketValidator webSocketValidator;

    @Inject
    MessageNodeRepository messageNodeRepository;

    private final Jsonb jsonb = JsonbBuilder.create();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) throws IOException {
        try {
            // Extract authentication info from WebSocket connection
            String authenticatedUserId = webSocketValidator.extractUserIdFromToken(session);
            String userRole = webSocketValidator.extractUserRoleFromToken(session);

            // Check if user is authenticated
            if (authenticatedUserId == null) {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "No valid authentication"));
                return;
            }

            // Check if user has required role
            if (!webSocketValidator.hasRequiredRole(userRole)) {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Insufficient permissions"));
                return;
            }

            // Check if the path userId matches the authenticated user
            if (!userId.equals(authenticatedUserId)) {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "User ID mismatch"));
                return;
            }

            // Store authenticated user info in session for later use
            session.getUserProperties().put("authenticatedUserId", authenticatedUserId);
            session.getUserProperties().put("userRole", userRole);

            // Mark user as online
            onlineUserManager.addUser(userId, session);

            // Send unread messages
            List<MessageNodeDTO> unreadMessages = messageNodeService.getUnreadForUser(userId);
            Map<String, Object> messagesResponse = Map.of(
                    "type", "unread_messages",
                    "data", unreadMessages
            );
            session.getAsyncRemote().sendText(jsonb.toJson(messagesResponse));

            // Send unread notifications
            List<NotificationNodeDTO> unreadNotifications = notificationNodeService.getUnreadForUser(userId);
            Map<String, Object> notificationsResponse = Map.of(
                    "type", "unread_notifications",
                    "data", unreadNotifications
            );
            session.getAsyncRemote().sendText(jsonb.toJson(notificationsResponse));

        } catch (Exception e) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Authentication error"));
            } catch (Exception ignored) {
                // Ignore cleanup errors
            }
        }


    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        try {
            // Get authenticated user info from session properties
            String authenticatedUserId = (String) session.getUserProperties().get("authenticatedUserId");
            String userRole = (String) session.getUserProperties().get("userRole");

            // Verify authentication is still valid
            if (!userId.equals(authenticatedUserId)) {
                sendError(session, "Authentication required or user mismatch");
                return;
            }

            // Verify user still has required role
            if (!webSocketValidator.hasRequiredRole(userRole)) {
                sendError(session, "Insufficient permissions");
                return;
            }

            // Parse incoming message
            Map<?, ?> incoming = jsonb.fromJson(message, Map.class);
            String type = (String) incoming.get("type");
            Map<?, ?> data = (Map<?, ?>) incoming.get("data");

            switch (type) {
                case "ping":
                    // Respond with pong to keep connection alive
                    handlePing(session);
                    break;
                case "send_message":
                    handleSendMessage(data, authenticatedUserId, session);
                    break;
                case "edit_message":
                    handleEditMessage(data, authenticatedUserId, session);
                    break;
                case "delete_message":
                    handleDeleteMessage(data, authenticatedUserId, session);
                    break;
                default:
                    // Ignore unknown types
                    break;
            }

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            sendError(session, "Failed to process message");
        }
    }

    private void handleSendMessage(Map<?, ?> data, String authenticatedUserId, Session session) {
        try {
            String recipientId = (String) data.get("recipientId");
            String content = (String) data.get("content");

            if (recipientId == null || content == null) {
                sendError(session, "Invalid message format");
                return;
            }

            // Security: Force sender to be the authenticated user
            MessageNodeDTO dto = new MessageNodeDTO();
            dto.setSenderId(authenticatedUserId);
            dto.setRecipientId(recipientId);
            dto.setContent(content);
            dto.setCreatedAt(Instant.now());
            dto.setRead(false);

            // Persist the message
            MessageNodeDTO created = messageNodeService.create(dto);

            // Prepare response wrapper
            Map<String, Object> response = Map.of(
                    "type", "new_message",
                    "data", created
            );
            String json = jsonb.toJson(response);

            // Send to sender (confirmation)
            session.getAsyncRemote().sendText(json);

            // Send to recipient if online
            Session recipientSession = onlineUserManager.getSession(recipientId);
            if (recipientSession != null && recipientSession.isOpen()) {
                recipientSession.getAsyncRemote().sendText(json);
            }

        } catch (Exception e) {
            System.err.println("Send message error: " + e.getMessage());
            sendError(session, e.getMessage());
        }
    }

    private void handleEditMessage(Map<?, ?> data, String authenticatedUserId, Session session) {
        try {
            Number messageIdNum = (Number) data.get("messageId");
            String newContent = (String) data.get("content");

            if (messageIdNum == null || newContent == null) {
                sendError(session, "Invalid edit message format");
                return;
            }

            Long messageId = messageIdNum.longValue();

            // Create DTO for update
            MessageNodeDTO updateDto = new MessageNodeDTO();
            updateDto.setId(messageId);
            updateDto.setContent(newContent);

            // Update the message (service will validate ownership)
            MessageNodeDTO updated = messageNodeService.update(updateDto);

            // Prepare response wrapper
            Map<String, Object> response = Map.of(
                    "type", "message_edited",
                    "data", updated
            );
            String json = jsonb.toJson(response);

            // Send to sender (confirmation)
            session.getAsyncRemote().sendText(json);

            // Send to recipient if online
            String recipientId = updated.getRecipientId();
            Session recipientSession = onlineUserManager.getSession(recipientId);
            if (recipientSession != null && recipientSession.isOpen()) {
                recipientSession.getAsyncRemote().sendText(json);
            }

        } catch (Exception e) {
            System.err.println("Edit message error: " + e.getMessage());
            sendError(session, e.getMessage());
        }
    }

    private void handleDeleteMessage(Map<?, ?> data, String authenticatedUserId, Session session) {
        try {
            Number messageIdNum = (Number) data.get("messageId");

            if (messageIdNum == null) {
                sendError(session, "Invalid delete message format");
                return;
            }

            Long messageId = messageIdNum.longValue();

            // Get message info before deletion for broadcasting
            MessageNode message = messageNodeRepository.findById(messageId);
            if (message == null) {
                sendError(session, "Message not found");
                return;
            }

            String recipientId = message.getRecipient().getUserId();

            // Delete the message (service will validate ownership)
            messageNodeService.delete(messageId);

            // Prepare response wrapper
            Map<String, Object> response = Map.of(
                    "type", "message_deleted",
                    "data", Map.of("messageId", messageId)
            );
            String json = jsonb.toJson(response);

            // Send to sender (confirmation)
            session.getAsyncRemote().sendText(json);

            // Send to recipient if online
            Session recipientSession = onlineUserManager.getSession(recipientId);
            if (recipientSession != null && recipientSession.isOpen()) {
                recipientSession.getAsyncRemote().sendText(json);
            }

        } catch (Exception e) {
            System.err.println("Delete message error: " + e.getMessage());
            sendError(session, e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        onlineUserManager.removeUser(userId);
    }

    @OnError
    public void onError(Session session, Throwable throwable, @PathParam("userId") String userId) {
        throwable.printStackTrace();

        // Clean up the user session since the connection failed
        if (userId != null) {
            onlineUserManager.removeUser(userId);
        }

        // Only send error if session is still open
        if (session.isOpen()) {
            sendError(session, "WebSocket error occurred");
        }
    }

    private void sendError(Session session, String errorMessage) {
        if (session.isOpen()) {
            Map<String, Object> error = Map.of(
                    "type", "error",
                    "data", errorMessage
            );
            String json = jsonb.toJson(error);
            session.getAsyncRemote().sendText(json);
        }
    }

    private void handlePing(Session session) {
        if (session.isOpen()) {
            Map<String, Object> pong = Map.of("type", "pong");
            String json = jsonb.toJson(pong);
            session.getAsyncRemote().sendText(json);
        }
    }
}
