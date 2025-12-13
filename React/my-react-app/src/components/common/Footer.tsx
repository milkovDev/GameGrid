import React from 'react';
import { Container } from 'react-bootstrap';

const Footer: React.FC = () => (
    <footer className="bg-dark text-white text-center py-3 mt-5">
        <Container fluid>
            <p>&copy; 2025 My App</p>
        </Container>
    </footer>
);

export default Footer;