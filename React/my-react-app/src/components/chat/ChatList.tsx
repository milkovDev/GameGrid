// ChatList.tsx (extracted chat list rendering)

import React from 'react';
import { Badge, Button } from 'react-bootstrap';
import { useMessages } from '../../contexts/MessageContext';
import { Chat } from '../../contexts/MessageContext'; // Assuming you export the Chat interface
import { imageApi } from '../../api/imageApi';

interface ChatListProps {
  activeChats: string[];
  chats: Map<string, Chat>;
  selectedChatUserId: string | null;
  onChatSelect: (userId: string) => void;
  currentUserId: string | null; // Added for completeness, though not directly used here
}

const ChatList: React.FC<ChatListProps> = ({ activeChats, chats, selectedChatUserId, onChatSelect }) => {
  const { closeChat } = useMessages();

  if (activeChats.length === 0) {
    return <div className="p-3 text-center no-messages">No active chats</div>;
  }

  return (
    <div>
      {activeChats.map((userId) => {
        const chat = chats.get(userId);
        const unreadCount = chat?.unreadCount || 0;
        const isSelected = selectedChatUserId === userId;
        const userInfo = chat?.userInfo;

        return (
          <div
            key={userId}
            className={`chat-list-item d-flex align-items-center p-2 border-bottom ${isSelected ? 'bg-primary text-white' : ''}`}
            style={{ cursor: 'pointer' }}
          >
            <div className="d-flex align-items-center flex-grow-1" onClick={() => onChatSelect(userId)}>
              <img
                src={imageApi.getAvatarImage(userInfo?.avatarUrl)}
                className="rounded-circle me-2"
                style={{ width: '2.5rem', height: '2.5rem', objectFit: 'cover' }}
              />
              <div className="flex-grow-1 min-width-0">
                <div className="fw-semibold chat-user-name">
                  {userInfo?.displayName || `User ${userId.substring(0, 8)}...`}
                </div>
                {unreadCount > 0 && (
                  <Badge bg="danger" pill className="mt-1">
                    {unreadCount}
                  </Badge>
                )}
              </div>
            </div>
            <Button
              variant="link"
              size="sm"
              className="chat-close-btn"
              onClick={(e) => {
                e.stopPropagation();
                closeChat(userId);
              }}
              title="Close chat"
            >
              ✕
            </Button>
          </div>
        );
      })}
    </div>
  );
};

export default ChatList;