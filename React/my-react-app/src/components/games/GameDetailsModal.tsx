import React, { useState } from 'react';
import { Modal, Button, Row, Col, Card } from 'react-bootstrap';
import { GameDTO } from '../../types/GameDTO';
import { imageApi } from '../../api/imageApi';
import { useAuth } from '../../contexts/AuthContext';
import { useUser } from '../../contexts/UserContext';
import { deleteUGLE } from '../../api/ugleApi';
import UgleFormModal from '../ugles/UgleFormModal';
import '../../styles/ModalStyles.css';


interface Props {
  show: boolean;
  onClose: () => void;
  game?: GameDTO;
}

const GameDetailsModal: React.FC<Props> = ({ show, onClose, game }) => {
  const { token } = useAuth();
  const { userData, fetchUserData } = useUser();
  const [showAddModal, setShowAddModal] = useState(false);

  if (!game) return null;

  const imageUrl = imageApi.getGameImage(game.coverUrl);

  const userEntries = userData?.relationalData.userGameListEntries || [];
  const existingEntry = userEntries.find((entry) => entry.game.id === game.id);
  const isInList = !!existingEntry;

  const handleAddOrDelete = async () => {
    if (!token || !userData) return;

    try {
      if (isInList && existingEntry) {
        if (window.confirm('Are you sure you want to delete this from your list?')) {
          await deleteUGLE(token, existingEntry);
          await fetchUserData();
        }
      } else {
        setShowAddModal(true);
      }
    } catch (err) {
      console.error('Failed to delete entry', err);
    }
  };

  // Format release date to European format
  const formatReleaseDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-GB');
  };

  return (
    <Modal show={show} onHide={onClose} size="xl">
      <Modal.Header closeButton className="bg-dark text-light">
        <Modal.Title>{game.title}</Modal.Title>
      </Modal.Header>
      <Modal.Body className="bg-dark text-light">
        <Row>
          <Col md={4}>
            <Card.Img
              src={imageUrl}
              style={{ height: 'auto', objectFit: 'cover' }}
            />
          </Col>
          <Col md={8}>
            <p><strong>Release Date:</strong> {formatReleaseDate(game.releaseDate)}</p>
            <p><strong>Developer:</strong> {game.developer.name}</p>
            <p><strong>Publisher:</strong> {game.publisher.name}</p>
            <p><strong>Genres:</strong> {game.gameGenres.map(g => g.genre.name).join(', ')}</p>
            <p><strong>Platforms:</strong> {game.gamePlatforms.map(p => p.platform.name).join(', ')}</p>
          </Col>
        </Row>
        <Row className="mt-3">
          <Col>
            <p className="game-description"><strong>Description:</strong>{'\n' + game.description}</p>
          </Col>
        </Row>
      </Modal.Body>
      <Modal.Footer className="bg-dark text-light">
        <Button 
          variant={isInList ? 'outline-danger' : 'outline-light'} 
          onClick={handleAddOrDelete}
        >
          {isInList ? 'Delete from List' : 'Add to List'}
        </Button>
      </Modal.Footer>
      <UgleFormModal
        show={showAddModal}
        onClose={() => setShowAddModal(false)}
        onSave={fetchUserData}
        isCreate={true}
        game={game}
      />
    </Modal>
  );
};

export default GameDetailsModal;