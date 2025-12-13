import React from 'react';
import { useAuth } from '../contexts/AuthContext';
import { Navigate } from 'react-router-dom';
import { Container, Row, Col, Card, Button } from 'react-bootstrap';

const Login: React.FC = () => {
    const { login, isAuthenticated } = useAuth();

    // If already authenticated, redirect to home
    if (isAuthenticated) {
        return <Navigate to="/" replace />;
    }

    return (
        <Container className="mt-5">
          <Row className="justify-content-center">
            <Col md={6}>
              <Card bg="dark" text="light" className="border-0">
                <Card.Body className="text-center">
                  <Card.Title as="h1" className="mb-4">Welcome</Card.Title>
                  <Card.Text className="mb-4">
                    Please login to continue to GameGrid.
                  </Card.Text>
                  <Button
                    variant="outline-light"
                    size="lg"
                    onClick={login}
                  >
                    Login with Keycloak
                  </Button>
                </Card.Body>
              </Card>
            </Col>
          </Row>
        </Container>
      );
};

export default Login;