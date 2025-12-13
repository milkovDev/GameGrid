import React from 'react';
import { Navbar, Nav, Button, Container, Badge } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useMessages } from '../../contexts/MessageContext';
import NotificationsDropdown from '../notifications/NotificationsDropdown';
import ChatBox from '../chat/Chatbox';

const Header: React.FC = () => {
    const { isAuthenticated, logout } = useAuth();
    const { totalUnreadCount, showChatBox, openChatBox, closeChatBox } = useMessages();
    
    if (!isAuthenticated) {
        return null; // Don't show navigation for unauthenticated users
    }

    return (
        <>
            <Navbar bg="dark" variant="dark" expand="lg" className="mb-4" fixed="top">
                <Container fluid>
                    <Navbar.Brand as={Link} to="/" className="d-flex align-items-center">
                        <img
                            src="images/logo.png"
                            alt="GameGrid Logo"
                            height="32"
                            className="d-inline-block align-top me-2"
                        />
                        GameGrid
                    </Navbar.Brand>
                    <Navbar.Toggle aria-controls="basic-navbar-nav" />
                    <Navbar.Collapse id="basic-navbar-nav">
                        <Nav className="me-auto">
                            <Nav.Link as={Link} to="/games">Games</Nav.Link>
                            <Nav.Link as={Link} to="/articles">Articles</Nav.Link>
                            <Nav.Link as={Link} to="/social">Social</Nav.Link>
                            <Nav.Link as={Link} to="/profile">Profile</Nav.Link>
                        </Nav>
                        <Nav className="ms-auto d-flex flex-row align-items-center">
                            <Button 
                                variant="outline-light" 
                                size="sm" 
                                className="me-2 position-relative"
                                onClick={() => openChatBox()}
                            >
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
                                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                                </svg>
                                {totalUnreadCount > 0 && (
                                    <Badge 
                                        bg="danger" 
                                        pill 
                                        className="position-absolute top-0 start-100 translate-middle"
                                        style={{ fontSize: '0.6rem' }}
                                    >
                                        {totalUnreadCount > 99 ? '99+' : totalUnreadCount}
                                    </Badge>
                                )}
                            </Button>
                            <NotificationsDropdown />
                            <Nav.Item className="ms-2">
                                <Button variant="outline-light" size="sm" onClick={logout}>
                                    Logout
                                </Button>
                            </Nav.Item>
                        </Nav>
                    </Navbar.Collapse>
                </Container>
            </Navbar>
            
            <ChatBox 
                show={showChatBox} 
                onHide={closeChatBox} 
            />
        </>
    );
};

export default Header;