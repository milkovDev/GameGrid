

// ChatBox.tsx

import React, { useState, useRef } from 'react';
import { Badge, Spinner, Button, Row, Col } from 'react-bootstrap';
import { useMessages } from '../../contexts/MessageContext';
import { useAuth } from '../../contexts/AuthContext';
import ChatList from './ChatList';
import ChatMessages from './ChatMessages';
import MessageInput from './MessageInput';
import '../../styles/ChatBox.css';

interface ChatBoxProps {
  show: boolean;
  onHide: () => void;
}

const ChatBox: React.FC<ChatBoxProps> = ({ show, onHide }) => {
  const { chats, activeChats, selectedChatUserId, isLoading, selectChat, markChatAsRead } = useMessages();
  const { userId: currentUserId } = useAuth();
  const [messageInput, setMessageInput] = useState('');
  const messagesEndRef = useRef<HTMLDivElement | null>(null);
  const messagesContainerRef = useRef<HTMLDivElement | null>(null);

  const selectedChat = selectedChatUserId ? chats.get(selectedChatUserId) : null;

  const handleClose = async () => {
    if (selectedChatUserId) {
      await markChatAsRead(selectedChatUserId);
    }
    onHide();
  };

  const handleChatSelect = (userId: string) => {
    selectChat(userId);
  };

  if (!show) return null;

  return (
    <div className="chat-widget">
      <div className="chat-header">
        <h6 className="mb-0">Messages</h6>
        <Button
          variant="link"
          size="sm"
          onClick={handleClose}
          className="text-white p-0 ms-auto"
          style={{ fontSize: '1.125rem', textDecoration: 'none' }}
        >
          ✕
        </Button>
      </div>

      <div className="chat-body">
        <Row className="g-0 h-100">
          {/* Chat List */}
          <Col md={4} className="chat-sidebar border-end d-flex flex-column" style={{ height: '34.375rem' }}>
            <div className="p-3 border-bottom bg-light flex-shrink-0">
              <h6 className="mb-0">Active Chats</h6>
            </div>
            <div className="flex-grow-1 overflow-auto">
              <ChatList
                activeChats={activeChats}
                chats={chats}
                selectedChatUserId={selectedChatUserId}
                onChatSelect={handleChatSelect}
                currentUserId={currentUserId}
              />
            </div>
          </Col>

          {/* Chat Area */}
          <Col md={8} className="d-flex flex-column" style={{ height: '34.375rem' }}>
            {selectedChatUserId ? (
              <>
                {/* Messages Area */}
                <div ref={messagesContainerRef} className="flex-grow-1 p-3 overflow-auto chat-messages-area">
                  {isLoading ? (
                    <div className="d-flex justify-content-center align-items-center h-100">
                      <Spinner animation="border" className="loading-spinner" />
                    </div>
                  ) : !selectedChat || selectedChat.messages.length === 0 ? (
                    <div className="text-center h-100 d-flex align-items-center justify-content-center no-messages">
                      No messages yet. Start the conversation!
                    </div>
                  ) : (
                    <ChatMessages
                      messages={selectedChat.messages}
                      currentUserId={currentUserId}
                      messagesEndRef={messagesEndRef}
                      userId={selectedChatUserId!}
                    />
                  )}
                </div>

                {/* Message Input */}
                <MessageInput
                  messageInput={messageInput}
                  setMessageInput={setMessageInput}
                  selectedChatUserId={selectedChatUserId}
                  isLoading={isLoading}
                />
              </>
            ) : (
              <div className="d-flex align-items-center justify-content-center h-100 select-chat-message">
                <div className="text-center">
                  <h5>Select a chat to start messaging</h5>
                  <p>Choose a conversation from the list to view and send messages.</p>
                </div>
              </div>
            )}
          </Col>
        </Row>
      </div>
    </div>
  );
};

export default ChatBox;