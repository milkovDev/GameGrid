export interface MessageDTO {
    id: number;
    content: string;
    createdAt: string;
    read: boolean;
    senderId: string;
    recipientId: string;
}