package org.acme.N4JDB.Repositories;

import jakarta.persistence.EntityNotFoundException;
import org.acme.N4JDB.Nodes.UserNode;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.cypher.Filter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;

@ApplicationScoped
public class UserNodeRepository {
    @Inject
    SessionFactory sessionFactory;

    public UserNode findByGraphId(Long id) {
        Session session = sessionFactory.openSession();

        UserNode userNode = session.load(UserNode.class, id);
        session.clear();

        return userNode;
    }

    public UserNode findById(String userId) {
        Session session = sessionFactory.openSession();

        // Use Filter to search by the userId field (not the primary key)
        Filter filter = new Filter("userId", ComparisonOperator.EQUALS, userId);
        Collection<UserNode> users = session.loadAll(UserNode.class, filter);
        session.clear();

        return users.isEmpty() ? null : users.iterator().next();
    }

    public void save(UserNode user) {
        Session session = sessionFactory.openSession();
        var tx = session.beginTransaction();
        try {
            session.save(user);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            session.clear();
        }
    }

    public Set<UserNode> getFollowers(String userId) {
        Session session = sessionFactory.openSession();
        String cypher = "MATCH (u:UserNode {userId: $userId})<-[:FOLLOWS]-(follower:UserNode) RETURN follower";
        Map<String, Object> params = Map.of("userId", userId);
        Iterable<UserNode> results = session.query(UserNode.class, cypher, params);
        Set<UserNode> followers = new HashSet<>();
        results.forEach(followers::add);
        session.clear();
        return followers;
    }

    public boolean deleteFollowsRelationship(String followerId, String followedId) {
        Session session = sessionFactory.openSession();
        var tx = session.beginTransaction();
        try {
            String cypher = "MATCH (follower:UserNode {userId: $followerId})-[r:FOLLOWS]->(followed:UserNode {userId: $followedId}) " +
                    "DELETE r " +
                    "RETURN count(r) AS deletedCount";
            Map<String, Object> params = Map.of("followerId", followerId, "followedId", followedId);
            Long deletedCount = session.queryForObject(Long.class, cypher, params);
            tx.commit();
            return deletedCount > 0;
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            session.clear();
        }
    }
}
