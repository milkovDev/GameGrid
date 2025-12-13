package org.acme.Services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.acme.N4JDB.DTOs.MessageNodeDTO;
import org.acme.N4JDB.Mappers.MessageNodeMapper;
import org.acme.N4JDB.Nodes.MessageNode;
import org.acme.N4JDB.Repositories.MessageNodeRepository;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class MessageNodeService {

    @Inject
    MessageNodeMapper messageNodeMapper;

    @Inject
    MessageNodeRepository messageNodeRepository;

    public MessageNodeDTO create(MessageNodeDTO dto) {
        MessageNode entity = messageNodeMapper.toEntity(dto);
        if (entity.getSender() == null || entity.getRecipient() == null) {
            throw new IllegalArgumentException("Sender and recipient IDs are required");
        }
        messageNodeRepository.save(entity);
        return messageNodeMapper.toDTO(entity);
    }

    public MessageNodeDTO update(MessageNodeDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("ID is required for update");
        }
        MessageNode entity = messageNodeRepository.findById(dto.getId());
        if (entity == null) {
            throw new NotFoundException("Message not found");
        }
        // Update editable fields
        if (dto.getContent() != null) {
            entity.setContent(dto.getContent());
        }

        messageNodeRepository.save(entity);
        return messageNodeMapper.toDTO(entity);
    }

    public void markAsRead(Long id) {
        MessageNode entity = messageNodeRepository.findById(id);
        if (entity == null) {
            throw new NotFoundException("Message not found");
        }
        entity.setRead(true);
        messageNodeRepository.save(entity);
    }

    public void delete(Long id) {
        messageNodeRepository.delete(id);
    }

    public List<MessageNodeDTO> getUnreadForUser(String userId) {
        return messageNodeRepository.findUnreadForUser(userId).stream()
                .map(messageNodeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void markAsReadUpTo(Long lastMessageId, String recipientId) {
        MessageNode lastMessage = messageNodeRepository.findById(lastMessageId);
        if (lastMessage == null) {
            throw new NotFoundException("Message not found");
        }

        // Check if the current user is part of this conversation
        boolean isRecipient = lastMessage.getRecipient().getUserId().equals(recipientId);
        boolean isSender = lastMessage.getSender().getUserId().equals(recipientId);

        if (!isRecipient && !isSender) {
            throw new ForbiddenException("You can only mark messages from your own conversations as read");
        }

        // Determine who the other person in the conversation is
        String otherUserId = isRecipient
                ? lastMessage.getSender().getUserId()
                : lastMessage.getRecipient().getUserId();

        Instant upToTime = lastMessage.getCreatedAt();

        // Mark all messages from the other user to the current user as read
        messageNodeRepository.markAsReadUpTo(otherUserId, recipientId, upToTime);
    }

    public List<MessageNodeDTO> getMessagesBetweenPaginated(
            String user1Id,
            String user2Id,
            int limit,
            int skip
    ) {
        // Get messages in DESC order (newest first) from DB
        List<MessageNodeDTO> messages = messageNodeRepository
                .findMessagesBetweenPaginated(user1Id, user2Id, limit, skip)
                .stream()
                .map(messageNodeMapper::toDTO)
                .collect(Collectors.toList());

        // Reverse to return in ASC order (oldest first) to frontend
        java.util.Collections.reverse(messages);
        return messages;
    }
}
