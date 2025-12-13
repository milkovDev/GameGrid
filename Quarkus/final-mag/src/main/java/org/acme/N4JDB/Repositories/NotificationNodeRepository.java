package org.acme.N4JDB.Repositories;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import org.acme.N4JDB.Nodes.NotificationNode;
import org.acme.N4JDB.Nodes.UserNode;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class NotificationNodeRepository {
    @Inject
    SessionFactory sessionFactory;

    @Inject
    UserNodeRepository userNodeRepository;

    public Collection<NotificationNode> findByTargetId(String targetId) {
        Session session = sessionFactory.openSession();
        String cypher = "MATCH (target:UserNode {userId: $targetId})<-[:FOR]-(notif:NotificationNode) RETURN notif";
        Map<String, Object> params = Map.of("targetId", targetId);
        Iterable<NotificationNode> results = session.query(NotificationNode.class, cypher, params);
        Collection<NotificationNode> notifications = new ArrayList<>();
        results.forEach(notifications::add);

        session.clear();
        return notifications;
    }

    public Collection<NotificationNode> findUnreadForUser(String userId) {
        Session session = sessionFactory.openSession();
        String cypher = "MATCH (u:UserNode {userId: $userId})<-[:FOR]-(n:NotificationNode {read: false}) " +
                "RETURN n ORDER BY n.createdAt ASC";
        Map<String, Object> params = Map.of("userId", userId);
        Iterable<NotificationNode> results = session.query(NotificationNode.class, cypher, params);
        Collection<NotificationNode> notifications = new ArrayList<>();
        results.forEach(notifications::add);

        session.clear();
        return notifications;
    }

    public NotificationNode findById(Long id) {
        Session session = sessionFactory.openSession();
        NotificationNode result = session.load(NotificationNode.class, id);

        session.clear();
        return result;
    }

    public void save(NotificationNode notification) {
        Session session = sessionFactory.openSession();
        var tx = session.beginTransaction();
        try {
            // Update target's receivedNotifications
            if (notification.getTarget() != null) {
                UserNode target = notification.getTarget();
                if (!target.getReceivedNotifications().contains(notification)) {
                    target.getReceivedNotifications().add(notification);
                    userNodeRepository.save(target);
                }
            }
            session.save(notification);
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
            NotificationNode notification = session.load(NotificationNode.class, id);
            if (notification == null) {
                throw new EntityNotFoundException("NotificationNode with ID " + id + " not found");
            }
            // Remove notification from target's receivedNotifications
            if (notification.getTarget() != null) {
                UserNode target = notification.getTarget();
                target.getReceivedNotifications().remove(notification);
                userNodeRepository.save(target);
            }
            session.delete(notification);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
        session.clear();
    }
}
