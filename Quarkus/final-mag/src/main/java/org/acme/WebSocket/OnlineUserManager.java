package org.acme.WebSocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class OnlineUserManager {
    private final ConcurrentHashMap<String, Session> onlineUsers = new ConcurrentHashMap<>();

    public void addUser(String userId, Session session) {
        onlineUsers.put(userId, session);
    }

    public void removeUser(String userId) {
        onlineUsers.remove(userId);
    }

    public Session getSession(String userId) {
        return onlineUsers.get(userId);
    }
}
