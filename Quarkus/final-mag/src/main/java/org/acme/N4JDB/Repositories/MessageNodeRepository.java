package org.acme.N4JDB.Repositories;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import org.acme.N4JDB.Nodes.MessageNode;
import org.acme.N4JDB.Nodes.UserNode;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class MessageNodeRepository {
    @Inject
    SessionFactory sessionFactory;

    @Inject
    UserNodeRepository userNodeRepository;

    public Collection<MessageNode> findUnreadForUser(String userId) {
        Session session = sessionFactory.openSession();
        String cypher = """
    MATCH (sender)-[:SENT]->(m:MessageNode)-[:TO]->(u:UserNode {userId: $userId})
    WHERE m.read = false
    RETURN m ORDER BY m.createdAt ASC
    """;
        Map<String, Object> params = Map.of("userId", userId);

        Iterable<MessageNode> results = session.query(MessageNode.class, cypher, params);
        Collection<MessageNode> messages = new ArrayList<>();

        // Load each message with its relationships
        for (MessageNode msg : results) {
            MessageNode fullMessage = session.load(MessageNode.class, msg.getId(), 1);
            messages.add(fullMessage);
        }

        session.clear();
        return messages;
    }

    public Collection<MessageNode> findMessagesBetweenPaginated(
            String user1Id,
            String user2Id,
            int limit,
            int skip
    ) {
        Session session = sessionFactory.openSession();
        String cypher = """
    MATCH (u1:UserNode {userId: $user1Id}), (u2:UserNode {userId: $user2Id})
    MATCH (sender)-[:SENT]->(msg:MessageNode)-[:TO]->(recipient)
    WHERE (sender = u1 AND recipient = u2) OR (sender = u2 AND recipient = u1)
    RETURN msg ORDER BY msg.createdAt DESC
    SKIP $skip
    LIMIT $limit
    """;
        Map<String, Object> params = Map.of(
                "user1Id", user1Id,
                "user2Id", user2Id,
                "skip", skip,
                "limit", limit
        );

        Iterable<MessageNode> results = session.query(MessageNode.class, cypher, params);
        Collection<MessageNode> messages = new ArrayList<>();

        // Load each message with its relationships
        for (MessageNode msg : results) {
            MessageNode fullMessage = session.load(MessageNode.class, msg.getId(), 1);
            messages.add(fullMessage);
        }

        session.clear();
        return messages;
    }

    public MessageNode findById(Long id) {
        Session session = sessionFactory.openSession();
        // Load with depth 1 to include relationships
        MessageNode result = session.load(MessageNode.class, id, 1);

        session.clear();
        return result;
    }

    public void save(MessageNode message) {
        Session session = sessionFactory.openSession();
        var tx = session.beginTransaction();
        try {
            // Update recipient's receivedMessages
            if (message.getRecipient() != null) {
                UserNode recipient = message.getRecipient();
                if (!recipient.getReceivedMessages().contains(message)) {
                    recipient.getReceivedMessages().add(message);
                    userNodeRepository.save(recipient);
                }
            }
            session.save(message);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
        session.clear();
    }

    public void markAsReadUpTo(String senderId, String recipientId, Instant upToTime) {
        Session session = sessionFactory.openSession();
        var tx = session.beginTransaction();
        try {
            String cypher = """
            MATCH (s:UserNode {userId: $senderId})-[:SENT]->(m:MessageNode)-[:TO]->(r:UserNode {userId: $recipientId})
            WHERE m.createdAt <= $upToTime AND m.read = false
            SET m.read = true
            """;
            Map<String, Object> params = Map.of(
                    "senderId", senderId,
                    "recipientId", recipientId,
                    "upToTime", upToTime.toString()  // Convert Instant to String for Neo4j OGM compatibility
            );
            session.query(cypher, params);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
        session.clear();
    }

    public void delete(Long id) {
        Session session = sessionFactory.openSession();
        var tx = session.beginTransaction();
        try {
            MessageNode message = session.load(MessageNode.class, id);
            if (message == null) {
                throw new EntityNotFoundException("MessageNode with ID " + id + " not found");
            }
            // Remove message from recipient's receivedMessages
            if (message.getRecipient() != null) {
                UserNode recipient = message.getRecipient();
                recipient.getReceivedMessages().remove(message);
                userNodeRepository.save(recipient);
            }
            session.delete(message);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
        session.clear();
    }
}
