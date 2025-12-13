import React from 'react';
import { Dropdown, Badge, Nav, Button } from 'react-bootstrap';
import NotificationCard from './NotificationCard';
import { useNotifications } from '../../contexts/NotificationContext';

const NotificationsDropdown: React.FC = () => {
    const { notifications, unreadCount, markNotificationAsRead, deleteNotification } = useNotifications();

    return (
        <Dropdown as={Nav.Item} align="end">
            <Dropdown.Toggle 
                as={Button}
                variant="outline-light" 
                size="sm"
                id="notifications-dropdown"
                className="position-relative"
                style={{ padding: '0.25rem 0.5rem' }}
                bsPrefix="btn"
            >
                {/* Bell Icon */}
                <svg 
                    width="16" 
                    height="16" 
                    viewBox="0 0 24 24" 
                    fill="none" 
                    stroke="currentColor" 
                    strokeWidth="2" 
                    strokeLinecap="round" 
                    strokeLinejoin="round"
                >
                    <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"></path>
                    <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
                </svg>
                {unreadCount > 0 && (
                    <Badge 
                        pill 
                        bg="danger" 
                        className="position-absolute top-0 start-100 translate-middle"
                        style={{ fontSize: '0.65rem', minWidth: '1.2rem', height: '1.2rem' }}
                    >
                        {unreadCount}
                    </Badge>
                )}
            </Dropdown.Toggle>
            <Dropdown.Menu 
                className="bg-dark border-secondary" 
                style={{ 
                    maxHeight: '25rem', 
                    overflowY: 'auto', 
                    width: '18.75rem',
                    '--bs-dropdown-link-color': '#f8f9fa',
                    '--bs-dropdown-link-hover-color': '#ffffff',
                    '--bs-dropdown-link-hover-bg': '#343a40'
                } as React.CSSProperties}
            >
                {notifications.length === 0 ? (
                    <div className="p-3 text-center text-light">No notifications</div>
                ) : (
                    notifications.map((notif) => (
                        <NotificationCard
                            key={notif.id}
                            notification={notif}
                            onMarkAsRead={markNotificationAsRead}
                            onDelete={deleteNotification}
                        />
                    ))
                )}
            </Dropdown.Menu>
        </Dropdown>
    );
};

export default NotificationsDropdown;