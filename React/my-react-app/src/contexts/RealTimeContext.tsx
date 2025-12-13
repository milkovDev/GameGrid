

import React, { createContext, useEffect, useRef, useContext, useCallback } from 'react';
import { useAuth } from './AuthContext';

interface SubscriberCallback {
    (data: any): void;
}

interface RealtimeContextType {
    subscribe: (type: string, callback: SubscriberCallback) => () => void;
    sendMessage: (recipientId: string, content: string) => void;
    editMessage: (messageId: number, content: string) => void;
    deleteMessage: (messageId: number) => void;
}

const RealtimeContext = createContext<RealtimeContextType | undefined>(undefined);

export const RealtimeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const auth = useAuth();
    const subscribers = useRef<Map<string, Set<SubscriberCallback>>>(new Map());
    const wsRef = useRef<WebSocket | null>(null);

    const subscribe = useCallback((type: string, callback: SubscriberCallback) => {
        if (!subscribers.current.has(type)) {
            subscribers.current.set(type, new Set());
        }
        subscribers.current.get(type)!.add(callback);
        return () => {
            subscribers.current.get(type)?.delete(callback);
            if (subscribers.current.get(type)?.size === 0) {
                subscribers.current.delete(type);
            }
        };
    }, []);

    const sendMessage = useCallback((recipientId: string, content: string) => {
        if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
            const message = {
                type: 'send_message',
                data: {
                    recipientId,
                    content
                }
            };
            wsRef.current.send(JSON.stringify(message));
        } else {
            console.error('WebSocket is not connected');
        }
    }, []);

    const editMessage = useCallback((messageId: number, content: string) => {
        if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
            const message = {
                type: 'edit_message',
                data: {
                    messageId,
                    content
                }
            };
            wsRef.current.send(JSON.stringify(message));
        } else {
            console.error('WebSocket is not connected');
        }
    }, []);

    const deleteMessage = useCallback((messageId: number) => {
        if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
            const message = {
                type: 'delete_message',
                data: {
                    messageId
                }
            };
            wsRef.current.send(JSON.stringify(message));
        } else {
            console.error('WebSocket is not connected');
        }
    }, []);

    useEffect(() => {
        let ws: WebSocket | null = null;
        let pingInterval: NodeJS.Timeout | null = null;
    
        const connectWebSocket = async () => {
            if (!auth.isAuthenticated || !auth.userId || !auth.token) return;
    
            try {
                await auth.refreshToken(30);
                const freshToken = auth.token!;
    
                //local
                //const wsUrl = `ws://localhost:8080/ws/realtime/${auth.userId}?token=${encodeURIComponent(freshToken)}`;
                //online
                const wsUrl = `wss://quarkus.gamegrid.buzz/ws/realtime/${auth.userId}?token=${encodeURIComponent(freshToken)}`;
                console.log('Trying to connect to WebSocket with URL:', wsUrl.substring(0, 100) + '...');
    
                ws = new WebSocket(wsUrl);
                wsRef.current = ws;
    
                ws.onopen = () => {
                    console.log('WebSocket connected');
                    
                    // Start ping interval - send ping every 30 seconds
                    pingInterval = setInterval(() => {
                        if (ws && ws.readyState === WebSocket.OPEN) {
                            ws.send(JSON.stringify({ type: 'ping' }));
                            console.log('Sent ping');
                        }
                    }, 60000); // 60 seconds
                };
    
                ws.onmessage = (event) => {
                    const msg = JSON.parse(event.data);
                    console.log('WebSocket message received:', msg);
    
                    // Ignore pong messages (just for keepalive)
                    if (msg.type === 'pong') {
                        console.log('Received pong');
                        return;
                    }
    
                    const type = msg.type;
                    const data = msg.data;
                    subscribers.current.get(type)?.forEach((cb) => cb(data));
                };
    
                ws.onclose = (event) => {
                    console.log('WebSocket closed. Code:', event.code, 'Reason:', event.reason);
                    wsRef.current = null;
                    
                    // Clear ping interval
                    if (pingInterval) {
                        clearInterval(pingInterval);
                        pingInterval = null;
                    }
                };
    
                ws.onerror = (event) => {
                    console.error('WebSocket error:', event);
                    wsRef.current = null;
                };
            } catch (error) {
                console.error('Failed to update token for WebSocket:', error);
            }
        };
    
        connectWebSocket();
    
        return () => {
            // Cleanup: clear interval and close connection
            if (pingInterval) {
                clearInterval(pingInterval);
            }
            if (ws) {
                ws.close();
                wsRef.current = null;
            }
        };
    }, [auth.isAuthenticated, auth.userId, auth.token, auth.refreshToken]);

    return (
        <RealtimeContext.Provider value={{ subscribe, sendMessage, editMessage, deleteMessage }}>
            {children}
        </RealtimeContext.Provider>
    );
};

export const useRealtime = () => {
    const context = useContext(RealtimeContext);
    if (!context) throw new Error('useRealtime must be used within RealtimeProvider');
    return context;
};