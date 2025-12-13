

import React, { useState } from 'react';
import { useUser } from '../../contexts/UserContext';
import { Row, Col, Button, Card } from 'react-bootstrap';
import { imageApi } from '../../api/imageApi';
import UserFormModal from '../users/UserFormModal';
import { UserDTO } from '../../types/UserDTO';

interface Props {
  user: UserDTO;
  isOwn?: boolean;
}

const ProfileInfo: React.FC<Props> = ({ user, isOwn = true }) => {
  const { fetchUserData } = useUser();
  const [showModal, setShowModal] = useState(false);

  const avatarUrl = imageApi.getAvatarImage(user.avatarUrl);

  return (
    <>
      <Card className="mb-3 text-light bg-dark mx-0">
        <Card.Body>
          <Row>
            <Col md={3}>
              <img
                src={avatarUrl}
                className="img-fluid rounded-circle"
                style={{ width: '20rem', height: '20rem', objectFit: 'cover' }}
              />
            </Col>
            <Col md={9}>
              <h2>
                {user.displayName}
                {isOwn && (
                  <Button variant="outline-light" size="sm" className="ms-2" onClick={() => setShowModal(true)}>
                    Update Profile
                  </Button>
                )}
              </h2>
              <p>{user.bio || 'No bio available'}</p>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {isOwn && (
        <UserFormModal
          show={showModal}
          onClose={() => setShowModal(false)}
          onSave={fetchUserData}
          user={user}
        />
      )}
    </>
  );
};

export default ProfileInfo;