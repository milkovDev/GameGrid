// MessageInput.tsx (extracted input area)

import React from 'react';
import { Form, Button } from 'react-bootstrap';
import { useMessages } from '../../contexts/MessageContext';

interface MessageInputProps {
  messageInput: string;
  setMessageInput: (value: string) => void;
  selectedChatUserId: string | null;
  isLoading: boolean;
}

const MessageInput: React.FC<MessageInputProps> = ({ messageInput, setMessageInput, selectedChatUserId, isLoading }) => {
  const { sendMessage } = useMessages();

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (messageInput.trim() && selectedChatUserId) {
      sendMessage(selectedChatUserId, messageInput.trim());
      setMessageInput('');
    }
  };

  return (
    <div className="p-3 border-top flex-shrink-0 message-input-area">
      <Form onSubmit={handleSendMessage}>
        <div className="input-group">
          <Form.Control
            as="textarea"
            rows={1}
            placeholder="Type your message..."
            value={messageInput}
            onChange={(e) => setMessageInput(e.target.value)}
            disabled={isLoading}
            style={{ resize: 'none', minHeight: '2.375rem', maxHeight: '7.5rem', overflowY: 'auto' }}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleSendMessage(e);
              }
            }}
          />
          <Button type="submit" variant="outline-light" disabled={!messageInput.trim() || isLoading}>
            Send
          </Button>
        </div>
      </Form>
    </div>
  );
};

export default MessageInput;