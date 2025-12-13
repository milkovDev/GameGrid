import React, { useEffect, useRef, useState } from 'react';
import { Spinner } from 'react-bootstrap';
import { MessageDTO } from '../../types/MessageDTO';
import { useMessages } from '../../contexts/MessageContext';
import MessageItem from './MessageItem';

interface ChatMessagesProps {
  messages: MessageDTO[];
  currentUserId: string | null;
  messagesEndRef: React.RefObject<HTMLDivElement | null>;
  userId: string;
}

const ChatMessages: React.FC<ChatMessagesProps> = ({ messages, currentUserId, messagesEndRef, userId }) => {
  const { loadMoreMessages, chats } = useMessages();
  const scrollContainerRef = useRef<HTMLDivElement | null>(null);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const prevScrollHeightRef = useRef<number>(0);
  const prevMessageCountRef = useRef<number>(0);

  const chat = chats.get(userId);
  const hasMoreMessages = chat?.hasMoreMessages ?? false;

  useEffect(() => {
    const scrollContainer = scrollContainerRef.current?.parentElement;
    if (!scrollContainer) return;

    const handleScroll = async () => {
      // Check if user scrolled near the top (within 100px threshold)
      if (scrollContainer.scrollTop < 100 && hasMoreMessages && !isLoadingMore) {
        setIsLoadingMore(true);
        
        // Save current scroll position
        prevScrollHeightRef.current = scrollContainer.scrollHeight;
        
        try {
          await loadMoreMessages(userId);
        } finally {
          setIsLoadingMore(false);
        }
      }
    };

    scrollContainer.addEventListener('scroll', handleScroll);
    return () => scrollContainer.removeEventListener('scroll', handleScroll);
  }, [userId, loadMoreMessages, hasMoreMessages, isLoadingMore]);

  // Restore scroll position after loading more messages (when scrolling up)
  useEffect(() => {
    const scrollContainer = scrollContainerRef.current?.parentElement;
    if (!scrollContainer || prevScrollHeightRef.current === 0) return;

    const newScrollHeight = scrollContainer.scrollHeight;
    const heightDifference = newScrollHeight - prevScrollHeightRef.current;
    
    if (heightDifference > 0) {
      // Maintain scroll position by adjusting for new content height
      scrollContainer.scrollTop = heightDifference;
      prevScrollHeightRef.current = 0;
    }
  }, [messages.length]);

  // Only scroll to bottom for NEW messages (not when loading older ones)
  useEffect(() => {
    const scrollContainer = scrollContainerRef.current?.parentElement;
    if (!scrollContainer || !messagesEndRef.current) return;

    const currentCount = messages.length;
    const prevCount = prevMessageCountRef.current;

    // Skip scroll logic if we just loaded more messages (indicated by prevScrollHeightRef being set)
    if (prevScrollHeightRef.current > 0) {
      // Don't update prevMessageCountRef yet - wait for scroll position restoration
      return;
    }

    // Only scroll to bottom if:
    // 1. This is the first load (prevCount === 0)
    // 2. OR new message was added at the END (user scrolled down or received new message)
    if (prevCount > 0 && currentCount > prevCount) {
      // Check if we're near the bottom already (within 200px)
      const isNearBottom = 
        scrollContainer.scrollHeight - scrollContainer.scrollTop - scrollContainer.clientHeight < 200;
      
      if (isNearBottom) {
        messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
      }
    } else if (prevCount === 0 && currentCount > 0) {
      // Initial load - scroll to bottom immediately
      messagesEndRef.current.scrollIntoView({ behavior: 'auto' });
    }

    prevMessageCountRef.current = currentCount;
  }, [messages, messagesEndRef]);

  return (
    <div ref={scrollContainerRef}>
      {isLoadingMore && (
        <div className="d-flex justify-content-center py-3">
          <Spinner animation="border" size="sm" />
          <span className="ms-2 small text-muted">Loading older messages...</span>
        </div>
      )}
      {!hasMoreMessages && messages.length > 0 && (
        <div className="text-center text-muted py-2 small">
          Beginning of conversation
        </div>
      )}
      {messages.map((message, index) => (
        <MessageItem
          key={message.id}
          message={message}
          index={index}
          messages={messages}
          currentUserId={currentUserId}
        />
      ))}
      <div ref={messagesEndRef} />
    </div>
  );
};

export default ChatMessages;