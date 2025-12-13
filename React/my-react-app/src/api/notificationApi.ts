import api from './apiClient';
import { NotificationDTO } from '../types/NotificationDTO';

export const getNotifications = async (targetId: string): Promise<NotificationDTO[]> => {
    const response = await api.get<NotificationDTO[]>(`/notifications/getForUser/${targetId}`);
    return response.data;
};
    
export const markAsRead = async (id: number): Promise<void> => {
    await api.put(`/notifications/read/${id}`);
};

export const deleteNotification = async (id: number): Promise<void> => {
    await api.delete(`/notifications/delete/${id}`);
};