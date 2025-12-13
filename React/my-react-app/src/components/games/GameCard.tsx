import { GameDTO } from '../../types/GameDTO';
import { Button, Card } from 'react-bootstrap';
import '../../App.css';
import { imageApi } from '../../api/imageApi';
import { useAuth } from '../../contexts/AuthContext';
import { useUser } from '../../contexts/UserContext';
import { deleteUGLE } from '../../api/ugleApi';
import { useState } from 'react';
import UgleFormModal from '../ugles/UgleFormModal';

interface Props {
  game: GameDTO;
  onEdit?: () => void;
  onDetails?: () => void;
}

const GameCard: React.FC<Props> = ({ game, onEdit, onDetails }) => {
  const { token } = useAuth();
  const { userData, fetchUserData } = useUser();
  const [showAddModal, setShowAddModal] = useState(false);
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
    <Card className="mb-3 text-light bg-dark">
       <div style={{ 
        width: '100%', 
        aspectRatio: '16/9', 
        overflow: 'hidden' 
      }}>
        <Card.Img
          src={imageUrl}
          style={{ 
            width: '100%', 
            height: '100%', 
            objectFit: 'cover' 
          }}
        />
      </div>
      <Card.Body>
        <Card.Title>{game.title}</Card.Title>
        <Card.Text>Developer: {game.developer.name}</Card.Text>
        <Card.Text>Publisher: {game.publisher.name}</Card.Text>
        <Card.Text>Release Date: {formatReleaseDate(game.releaseDate)}</Card.Text>
        <div className="card-text">
          <div className="game-card-tags-container">
            <span>
              <small>Genres: </small>
              {game.gameGenres.map((g, index) => (
                <span key={index} className="game-card-tags">
                  {g.genre.name}
                </span>
              ))}
            </span>
          </div>
        </div>
        <div className="card-text">
          <div className="game-card-tags-container">
            <span>
              <small>Platforms: </small>
              {game.gamePlatforms.map((p, index) => (
                <span key={index} className="game-card-tags">
                  {p.platform.name}
                </span>
              ))}
            </span>
          </div>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <Button variant="outline-light" size="sm" onClick={onDetails}>
              Details
            </Button>
            <Button 
              variant={isInList ? 'outline-danger' : 'outline-light'} 
              size="sm" 
              onClick={handleAddOrDelete}
            >
              {isInList ? 'Delete from List' : 'Add to List'}
            </Button>
          </div>
          {onEdit && (
            <Button variant="outline-light" size="sm" onClick={onEdit}>
              Edit
            </Button>
          )}
        </div>
      </Card.Body>
      <UgleFormModal
        show={showAddModal}
        onClose={() => setShowAddModal(false)}
        onSave={fetchUserData}
        isCreate={true}
        game={game}
      />
    </Card>
  );
};

export default GameCard;