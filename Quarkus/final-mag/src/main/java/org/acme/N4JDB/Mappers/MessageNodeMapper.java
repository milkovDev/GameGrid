package org.acme.N4JDB.Mappers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.N4JDB.DTOs.MessageNodeDTO;
import org.acme.N4JDB.Nodes.MessageNode;
import org.acme.N4JDB.Nodes.UserNode;
import org.acme.N4JDB.Repositories.UserNodeRepository;

@ApplicationScoped
public class MessageNodeMapper {

    @Inject
    UserNodeRepository userNodeRepository;

    public MessageNodeDTO toDTO(MessageNode messageNode) {
        if (messageNode == null) {
            return null;
        }

        MessageNodeDTO dto = new MessageNodeDTO();
        dto.setId(messageNode.getId());
        dto.setContent(messageNode.getContent());
        dto.setCreatedAt(messageNode.getCreatedAt());
        dto.setRead(messageNode.getRead());

        // Extract sender and recipient IDs from relationships
        dto.setSenderId(messageNode.getSender() != null ? messageNode.getSender().getUserId() : null);
        dto.setRecipientId(messageNode.getRecipient() != null ? messageNode.getRecipient().getUserId() : null);

        return dto;
    }

    public MessageNode toEntity(MessageNodeDTO dto) {
        if (dto == null) {
            return null;
        }

        MessageNode messageNode = new MessageNode();
        messageNode.setContent(dto.getContent());
        messageNode.setCreatedAt(dto.getCreatedAt());
        messageNode.setRead(dto.getRead());

        // Set relationships if IDs are provided
        if (dto.getSenderId() != null) {
            UserNode sender = userNodeRepository.findById(dto.getSenderId());
            if (sender == null) {
                throw new IllegalArgumentException("Sender with ID " + dto.getSenderId() + " not found");
            }
            messageNode.setSender(sender);
        }

        if (dto.getRecipientId() != null) {
            UserNode recipient = userNodeRepository.findById(dto.getRecipientId());
            if (recipient == null) {
                throw new IllegalArgumentException("Recipient with ID " + dto.getRecipientId() + " not found");
            }
            messageNode.setRecipient(recipient);
        }

        return messageNode;
    }
}
