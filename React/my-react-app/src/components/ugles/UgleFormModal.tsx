import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Dropdown } from 'react-bootstrap';
import { useAuth } from '../../contexts/AuthContext';
import { useUser } from '../../contexts/UserContext';
import { createUGLE, updateUGLE } from '../../api/ugleApi';
import { UserGameListEntryDTO } from '../../types/UserGameListEntryDTO';
import { GameDTO } from '../../types/GameDTO';
import '../../styles/ModalStyles.css';

interface Props {
  show: boolean;
  onClose: () => void;
  onSave: (updatedUgle?: UserGameListEntryDTO) => void; // Pass the updated entry
  isCreate: boolean;
  game?: GameDTO;
  ugle?: UserGameListEntryDTO;
}

const UgleFormModal: React.FC<Props> = ({ show, onClose, onSave, isCreate, game, ugle }) => {
  const { token } = useAuth();
  const { userData, fetchUserData } = useUser();
  const [formData, setFormData] = useState<UserGameListEntryDTO>({
    userId: userData?.relationalData.id || '',
    game: game || ugle?.game || { id: 0 } as GameDTO,
    status: 'PLAYING',
    isFavorite: false,
    rating: 1,
    reviewText: null,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (ugle && !isCreate) {
      setFormData(ugle);
    } else if (game && isCreate) {
      setFormData({
        userId: userData?.relationalData.id || '',
        game,
        status: 'PLAYING',
        isFavorite: false,
        rating: 1,
        reviewText: null,
      });
    }
  }, [ugle, game, isCreate, show]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value, type } = e.target;
    const checked = (e.target as HTMLInputElement).checked;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : name === 'rating' ? parseInt(value) || null : value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token || !userData) return;
    setLoading(true);
    setError(null);

    try {
      let savedUgle;
      if (isCreate) {
        savedUgle = await createUGLE(token, formData);
        // For creation, we still need to refresh to get the complete data
        await fetchUserData();
        onSave();
      } else {
        savedUgle = await updateUGLE(token, formData);
        // For updates, pass the updated entry to avoid full refresh
        onSave(savedUgle);
      }
      onClose();
    } catch (err) {
      setError('Failed to save entry');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const statusOptions = [
    { value: 'PLAYING', label: 'Playing' },
    { value: 'FINISHED', label: 'Finished' },
    { value: 'WISHLIST', label: 'Wishlist' }
  ];

  const ratingOptions = Array.from({ length: 10 }, (_, i) => ({
    value: i + 1,
    label: `${i + 1}`
  }));

  const selectStatus = (status: string) => {
    setFormData((prev) => ({ ...prev, status }));
  };

  const selectRating = (rating: number) => {
    setFormData((prev) => ({ ...prev, rating }));
  };

  const toggleFavorite = () => {
    setFormData((prev) => ({ ...prev, isFavorite: !prev.isFavorite }));
  };

  return (
    <Modal show={show} onHide={onClose} size="lg">
      <Modal.Header closeButton className="bg-dark text-light">
        <Modal.Title>{isCreate ? 'Add to List' : 'Update Entry'}</Modal.Title>
      </Modal.Header>
      <Modal.Body className="bg-dark text-light">
        <Form onSubmit={handleSubmit}>
          <Form.Group controlId="status" className="mb-3">
            <Form.Label>Status</Form.Label>
            <Dropdown>
              <Dropdown.Toggle
                variant="outline-light"
                id="statusDropdown"
                className="w-100 text-start"
              >
                {statusOptions.find(option => option.value === formData.status)?.label || 'Select Status'}
              </Dropdown.Toggle>
              <Dropdown.Menu className="dropdown-menu-dark w-100">
                {statusOptions.map((option) => (
                  <Dropdown.Item
                    key={option.value}
                    onClick={() => selectStatus(option.value)}
                    className="dropdown-item-dark"
                  >
                    {option.label}
                  </Dropdown.Item>
                ))}
              </Dropdown.Menu>
            </Dropdown>
          </Form.Group>

          <Form.Group controlId="rating" className="mb-3">
            <Form.Label>Rating</Form.Label>
            <Dropdown>
              <Dropdown.Toggle
                variant="outline-light"
                id="ratingDropdown"
                className="w-100 text-start"
              >
                {formData.rating ? `${formData.rating}` : 'Select Rating'}
              </Dropdown.Toggle>
              <Dropdown.Menu className="dropdown-menu-dark w-100" style={{ maxHeight: '12.5rem', overflowY: 'auto' }}>
                {ratingOptions.map((option) => (
                  <Dropdown.Item
                    key={option.value}
                    onClick={() => selectRating(option.value)}
                    className="dropdown-item-dark"
                  >
                    {option.label}
                  </Dropdown.Item>
                ))}
              </Dropdown.Menu>
            </Dropdown>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Favorite</Form.Label>
            <div className="d-block">
              <Button
                variant="outline-light"
                className={`favorite-toggle ${formData.isFavorite ? 'active' : ''}`}
                onClick={toggleFavorite}
                type="button"
              >
                {formData.isFavorite ? 'Favorite' : 'Add to Favorites'}
              </Button>
            </div>
          </Form.Group>

          {error && <div className="text-danger mb-3">{error}</div>}
          <Button variant="outline-light" type="submit" disabled={loading}>
            {loading ? 'Saving...' : isCreate ? 'Add' : 'Update'}
          </Button>
        </Form>
      </Modal.Body>
    </Modal>
  );
};

export default UgleFormModal;