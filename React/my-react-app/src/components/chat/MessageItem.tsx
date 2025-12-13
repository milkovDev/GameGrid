// MessageItem.tsx (moved function definitions up)

import React, { useState } from 'react';
import { Button } from 'react-bootstrap';
import { useMessages } from '../../contexts/MessageContext';
import { MessageDTO } from '../../types/MessageDTO';

interface MessageItemProps {
  message: MessageDTO;
  index: number;
  messages: MessageDTO[];
  currentUserId: string | null;
}

const MessageItem: React.FC<MessageItemProps> = ({ message, index, messages, currentUserId }) => {
  const { editMessage, deleteMessage } = useMessages();
  const [editingMessageId, setEditingMessageId] = useState<number | null>(null);
  const [editingContent, setEditingContent] = useState('');
  const isOwn = message.senderId === currentUserId;
  const isEditing = editingMessageId === message.id;
  const previousMessage = index > 0 ? messages[index - 1] : undefined;

  const formatMessageTime = (createdAt: string) => new Date(createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  const formatMessageDate = (createdAt: string) => new Date(createdAt).toLocaleDateString([], { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
  const shouldShowDateSeparator = (current: MessageDTO, previous?: MessageDTO) => !previous || new Date(current.createdAt).toDateString() !== new Date(previous.createdAt).toDateString();

  const showDateSeparator = shouldShowDateSeparator(message, previousMessage);

  const handleEditStart = (messageId: number, currentContent: string) => {
    setEditingMessageId(messageId);
    setEditingContent(currentContent);
  };

  const handleEditSave = (messageId: number) => {
    if (editingContent.trim()) {
      editMessage(messageId, editingContent.trim());
    }
    setEditingMessageId(null);
    setEditingContent('');
  };

  const handleEditCancel = () => {
    setEditingMessageId(null);
    setEditingContent('');
  };

  const handleDeleteMessage = (messageId: number) => {
    if (window.confirm('Are you sure you want to delete this message?')) {
      deleteMessage(messageId);
    }
  };

  return (
    <React.Fragment key={message.id}>
      {showDateSeparator && <div className="date-separator">{formatMessageDate(message.createdAt)}</div>}
      <div className={`d-flex mb-2 ${isOwn ? 'justify-content-end' : 'justify-content-start'}`}>
        <div className={`message-container position-relative ${isOwn ? 'own-message' : 'other-message'}`} style={{ maxWidth: '70%' }}>
          <div
            className={`p-2 rounded ${isOwn ? 'bg-primary text-white' : 'bg-light text-dark border'}`}
            style={{ wordWrap: 'break-word', wordBreak: 'break-word', overflowWrap: 'break-word' }}
          >
            {isEditing ? (
              <div>
                <textarea
                  className="form-control"
                  value={editingContent}
                  onChange={(e) => setEditingContent(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' && !e.shiftKey) {
                      e.preventDefault();
                      handleEditSave(message.id!);
                    } else if (e.key === 'Escape') {
                      handleEditCancel();
                    }
                  }}
                  autoFocus
                  style={{ resize: 'none', minHeight: '3.75rem', fontSize: '0.875rem' }}
                />
                <div className="mt-2">
                  <Button size="sm" variant="outline-dark" onClick={() => handleEditSave(message.id!)} className="me-1">
                    Save
                  </Button>
                  <Button size="sm" variant="outline-danger" onClick={handleEditCancel}>
                    Cancel
                  </Button>
                </div>
              </div>
            ) : (
              <div className="message-content" style={{ whiteSpace: 'pre-wrap' }}>
                {message.content}
              </div>
            )}
            <small className={`d-block mt-1 ${isOwn ? 'text-white-50' : 'text-muted'}`}>{formatMessageTime(message.createdAt)}</small>
          </div>

          {isOwn && !isEditing && (
            <div className="message-actions position-absolute top-0 end-0 translate-middle-y">
              <Button
                variant="link"
                size="sm"
                className="message-action-btn"
                onClick={() => handleEditStart(message.id!, message.content)}
                title="Edit message"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
              </Button>
              <Button
                variant="link"
                size="sm"
                className="message-action-btn"
                onClick={() => handleDeleteMessage(message.id!)}
                title="Delete message"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <polyline points="3,6 5,6 21,6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </Button>
            </div>
          )}
        </div>
      </div>
    </React.Fragment>
  );
};

export default MessageItem;