import React from 'react';
import { Button } from 'react-bootstrap';
import { NotificationDTO } from '../../types/NotificationDTO';

interface Props {
    notification: NotificationDTO;
    onMarkAsRead: (id: number) => void;
    onDelete: (id: number) => void;
}

const NotificationCard: React.FC<Props> = ({ notification, onMarkAsRead, onDelete }) => {
    const formatDate = (dateStr: string) => new Date(dateStr).toLocaleString('en-GB');

    return (
        <div 
            className="p-3 border-bottom border-secondary" 
            style={{ backgroundColor: '#212529' }}
        >
            <div className="d-flex justify-content-between align-items-start">
                <div className="flex-grow-1 me-2">
                    <p className="mb-1 text-light">{notification.content}</p>
                    <small className="text-secondary" style={{ fontSize: '0.75rem' }}>{formatDate(notification.createdAt)}</small>
                </div>
                <Button
                    variant="outline-danger"
                    size="sm"
                    className="ms-2"
                    style={{ 
                        minWidth: '2rem', 
                        height: '2rem',
                        padding: '0',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                    }}
                    onClick={(e) => {
                        e.stopPropagation();
                        onDelete(notification.id);
                    }}
                    title="Delete notification"
                >
                    {/* X Icon */}
                    <svg 
                        width="14" 
                        height="14" 
                        viewBox="0 0 24 24" 
                        fill="none" 
                        stroke="currentColor" 
                        strokeWidth="2" 
                        strokeLinecap="round" 
                        strokeLinejoin="round"
                    >
                        <path d="M18 6L6 18"></path>
                        <path d="M6 6l12 12"></path>
                    </svg>
                </Button>
            </div>
            {!notification.read && (
                <div className="text-end mt-2">
                    <small
                        className="text-light"
                        style={{ 
                            cursor: 'pointer',
                            textDecoration: 'underline',
                            fontSize: '0.75rem',
                            opacity: 0.7
                        }}
                        onClick={(e) => {
                            e.stopPropagation();
                            onMarkAsRead(notification.id);
                        }}
                    >
                        Mark as read
                    </small>
                </div>
            )}
        </div>
    );
};

export default NotificationCard;