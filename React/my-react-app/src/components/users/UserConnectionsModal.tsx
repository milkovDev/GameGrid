import React, { useState, useEffect } from 'react';
import { Modal, Form } from 'react-bootstrap';
import { useAuth } from '../../contexts/AuthContext';
import UserCard from './UserCard';
import { UserDTO } from '../../types/UserDTO';
import { getUserFollowing, getUserFollowers } from '../../api/userApi';
import '../../styles/ModalStyles.css';

interface Props {
  show: boolean;
  onClose: () => void;
  userId: string;
  type: 'following' | 'followers';
  isOwnProfile: boolean;
}

const UserConnectionsModal: React.FC<Props> = ({ show, onClose, userId, type, isOwnProfile }) => {
  const { token, userId: currentUserId } = useAuth();
  const [users, setUsers] = useState<UserDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    if (!show || !token) return;

    const fetchUsers = async () => {
      setLoading(true);
      try {
        let data: UserDTO[];
        if (type === 'following') {
          data = await getUserFollowing(token, userId);
        } else {
          data = await getUserFollowers(token, userId);
        }
        setUsers(data);
      } catch (err) {
        console.error('Failed to load users', err);
      } finally {
        setLoading(false);
      }
    };

    fetchUsers();
  }, [show, token, userId, type]);

  const filteredUsers = users
    .filter(user => user.displayName.toLowerCase().includes(searchTerm.toLowerCase()))
    .sort((a, b) => a.displayName.localeCompare(b.displayName));

  return (
    <Modal show={show} onHide={onClose} centered>
      <Modal.Header closeButton className="bg-dark text-light">
        <Modal.Title>{type === 'following' ? `Following (${filteredUsers.length})` : `Followers (${filteredUsers.length})`}</Modal.Title>
      </Modal.Header>
      <Modal.Body className="bg-dark text-light" style={{ maxHeight: '70vh', overflowY: 'auto' }}>
        <Form.Control
          type="text"
          placeholder="Search users..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="mb-3"
        />
        {loading ? (
          <div className="text-center">Loading...</div>
        ) : (
          filteredUsers.map(user => (
            <UserCard
              key={user.id}
              user={user}
              theme="light"
            />
          ))
        )}
        {filteredUsers.length === 0 && !loading && (
          <div className="text-center">No users found</div>
        )}
      </Modal.Body>
    </Modal>
  );
};

export default UserConnectionsModal;