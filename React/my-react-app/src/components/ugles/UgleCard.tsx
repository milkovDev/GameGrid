

import React, { useState } from 'react';
import { UserGameListEntryDTO } from '../../types/UserGameListEntryDTO';
import { imageApi } from '../../api/imageApi';
import { Button, Row, Col } from 'react-bootstrap';
import GameDetailsModal from '../games/GameDetailsModal';
import UgleFormModal from './UgleFormModal';
import ReviewModal from './ReviewModal';
import { useAuth } from '../../contexts/AuthContext';
import { deleteUGLE } from '../../api/ugleApi';

interface Props {
  ugle: UserGameListEntryDTO;
  onUpdate?: (updatedUgle: UserGameListEntryDTO) => void;
  onDelete?: (ugleId: number) => void;
  isOwn?: boolean;
}

const UgleCard: React.FC<Props> = ({ ugle, onUpdate, onDelete, isOwn = true }) => {
  const [currentUgle, setCurrentUgle] = useState<UserGameListEntryDTO>(ugle);
  const [showDetails, setShowDetails] = useState(false);
  const [showUpdateModal, setShowUpdateModal] = useState(false);
  const [showReviewModal, setShowReviewModal] = useState(false);
  const { token } = useAuth();

  const imageUrl = imageApi.getGameImage(currentUgle.game.coverUrl);

  const favoriteLabel = currentUgle.isFavorite ? 'FAVORITE' : '';
  const ratingLabel = currentUgle.rating != null ? `${currentUgle.rating} / 10` : 'N/A';

  const handleDelete = async () => {
    if (!token || !currentUgle.id) return;
    if (window.confirm('Are you sure you want to delete this entry?')) {
      try {
        await deleteUGLE(token, currentUgle);
        onDelete?.(currentUgle.id);
      } catch (err) {
        console.error('Failed to delete entry', err);
      }
    }
  };

  const handleUgleUpdate = (updatedUgle?: UserGameListEntryDTO) => {
    if (updatedUgle) {
      // Optimistic update - update local state immediately
      setCurrentUgle(updatedUgle);
      onUpdate?.(updatedUgle);
    }
  };

  const handleReviewUpdate = (updatedUgle: UserGameListEntryDTO) => {
    // Optimistic update - update local state immediately
    setCurrentUgle(updatedUgle);
    onUpdate?.(updatedUgle);
  };

  return (
    <Row 
      className="mb-3 bg-dark text-light p-2 mx-0 align-items-center" 
      style={{ borderRadius: '0.5rem' }}
    >
      <Col xs={2}>
        <div style={{ 
          width: '100%', 
          aspectRatio: '16/9', 
          overflow: 'hidden',
          borderRadius: '0.25rem'
        }}>
          <img 
            src={imageUrl}
            style={{ 
              width: '100%', 
              height: '100%', 
              objectFit: 'cover'
            }}
          />
        </div>
      </Col>
      <Col xs={4}>
        <span style={{ fontWeight: 'bold' }}>{currentUgle.game.title}</span>
      </Col>
      <Col xs={2} className="text-center">
        <span 
          className="d-inline-block px-2 py-1 bg-white text-dark" 
          style={{ 
            fontWeight: 'bold', 
            borderRadius: '0.25rem',
            minWidth: '5rem',
            textAlign: 'center'
          }}
        >
          {currentUgle.status}
        </span>
      </Col>
      <Col xs={1} className="text-center">
        <span 
          className="d-inline-block px-2 py-1 bg-white text-dark" 
          style={{ 
            fontWeight: 'bold', 
            borderRadius: '0.25rem',
            minWidth: '5rem',
            textAlign: 'center'
          }}
        >
          {favoriteLabel}
        </span>
      </Col>
      <Col xs={1} className="text-center">
        <span 
          className="d-inline-block px-2 py-1 bg-white text-dark" 
          style={{ 
            fontWeight: 'bold', 
            borderRadius: '0.25rem',
            minWidth: '3.75rem',
            textAlign: 'center'
          }}
        >
          {ratingLabel}
        </span>
      </Col>
      <Col xs={2} className="d-flex flex-column align-items-end gap-1">
        <Button variant="outline-light" size="sm" onClick={() => setShowDetails(true)} className="w-100">
          Details
        </Button>
        <Button variant="outline-light" size="sm" onClick={() => setShowReviewModal(true)} className="w-100">
          Review
        </Button>
        {isOwn && (
          <>
            <Button variant="outline-light" size="sm" onClick={() => setShowUpdateModal(true)} className="w-100">
              Update
            </Button>
            <Button variant="outline-danger" size="sm" onClick={handleDelete} className="w-100">
              Delete
            </Button>
          </>
        )}
      </Col>
      <GameDetailsModal 
        show={showDetails} 
        onClose={() => setShowDetails(false)} 
        game={currentUgle.game} 
      />
      {isOwn && (
        <UgleFormModal
          show={showUpdateModal}
          onClose={() => setShowUpdateModal(false)}
          onSave={handleUgleUpdate}
          isCreate={false}
          ugle={currentUgle}
        />
      )}
      <ReviewModal
        show={showReviewModal}
        onClose={() => setShowReviewModal(false)}
        ugle={currentUgle}
        onUpdate={isOwn ? handleReviewUpdate : undefined}
        isOwn={isOwn}
      />
    </Row>
  );
};

export default UgleCard;