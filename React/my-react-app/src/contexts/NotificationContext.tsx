// NotificationContext.tsx

import React, { createContext, useState, useEffect, useContext, useCallback, useMemo } from 'react';
import { useAuth } from './AuthContext'; // Adjust path if needed
import { useRealtime } from './RealTimeContext'; // Adjust path if needed
import { NotificationDTO } from '../types/NotificationDTO';
import { getNotifications } from '../api/notificationApi';
import * as notificationApi from '../api/notificationApi';
import { audioNotificationManager } from '../utils/audioNotifications';

interface NotificationContextType {
    notifications: NotificationDTO[];
    unreadCount: number;
    fetchNotifications: () => Promise<void>;
    markNotificationAsRead: (id: number) => Promise<void>;
    deleteNotification: (id: number) => Promise<void>;
    addNotification: (notif: NotificationDTO, isRealTime?: boolean) => void;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const NotificationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const auth = useAuth();
    const realtime = useRealtime();
    const [notifications, setNotifications] = useState<NotificationDTO[]>([]);

    const unreadCount = useMemo(() => notifications.filter((n) => !n.read).length, [notifications]);

    const fetchNotifications = useCallback(async () => {
        if (!auth.userId) return;
        try {
            const data = await getNotifications(auth.userId);
            setNotifications(data.sort((a, b) => Date.parse(b.createdAt) - Date.parse(a.createdAt)));
        } catch (error) {
            console.error('Failed to fetch notifications:', error);
        }
    }, [auth.userId]);

    const addNotification = useCallback((notif: NotificationDTO, isRealTime: boolean = false) => {
        setNotifications((prev) => {
            if (prev.some((p) => p.id === notif.id)) return prev;
            
            // Only play notification sound for real-time new unread notifications
            if (isRealTime && !notif.read) {
                audioNotificationManager.playNotificationSound();
            }
            
            return [notif, ...prev];
        });
    }, []);

    const markNotificationAsRead = useCallback(async (id: number) => {
        try {
            await notificationApi.markAsRead(id);
            setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)));
        } catch (error) {
            console.error('Failed to mark notification as read:', error);
        }
    }, []);

    const deleteNotification = useCallback(async (id: number) => {
        try {
            await notificationApi.deleteNotification(id);
            setNotifications((prev) => prev.filter((n) => n.id !== id));
        } catch (error) {
            console.error('Failed to delete notification:', error);
        }
    }, []);

    useEffect(() => {
        const unsubNew = realtime.subscribe('new_notification', (data) => {
            addNotification(data as NotificationDTO, true); // true = isRealTime
        });

        const unsubUnread = realtime.subscribe('unread_notifications', (data) => {
            const unread = data as NotificationDTO[];
            setNotifications((prev) => {
                const existingIds = new Set(prev.map((p) => p.id));
                const newUnread = unread.filter((u) => !existingIds.has(u.id));
                
                // Don't play sound for unread notifications on login
                if (newUnread.length > 0) {
                    return [...newUnread, ...prev].sort((a, b) => Date.parse(b.createdAt) - Date.parse(a.createdAt));
                }
                return prev;
            });
        });

        return () => {
            unsubNew();
            unsubUnread();
        };
    }, [realtime, addNotification]);

    return (
        <NotificationContext.Provider
            value={{
                notifications,
                unreadCount,
                fetchNotifications,
                markNotificationAsRead,
                deleteNotification,
                addNotification,
            }}
        >
            {children}
        </NotificationContext.Provider>
    );
};

export const useNotifications = () => {
    const context = useContext(NotificationContext);
    if (!context) throw new Error('useNotifications must be used within NotificationProvider');
    return context;
};