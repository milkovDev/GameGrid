export interface NotificationDTO {
    id: number;
    content: string;
    createdAt: string;
    read: boolean;
    targetId: string;
    triggererId: string;
}