
import React, { useState } from 'react';
import { Modal, Form, Button, Alert } from 'react-bootstrap';
import { useAuth } from '../../contexts/AuthContext';
import { useUser } from '../../contexts/UserContext';
import { updateUGLE } from '../../api/ugleApi';
import { UserGameListEntryDTO } from '../../types/UserGameListEntryDTO';
import { 
  validateTextField, 
  TextType, 
  getCharacterCount, 
  ValidationError,
  TEXT_LIMITS 
} from '../../utils/formValidator';
import '../../styles/ModalStyles.css';

interface Props {
  show: boolean;
  onClose: () => void;
  ugle: UserGameListEntryDTO;
  onUpdate?: (updatedUgle: UserGameListEntryDTO) => void;
  isOwn?: boolean;
}

const ReviewModal: React.FC<Props> = ({ show, onClose, ugle, onUpdate, isOwn = true }) => {
  const { token } = useAuth();
  const { fetchUserData } = useUser();
  const [isEditing, setIsEditing] = useState(false);
  const [reviewText, setReviewText] = useState(ugle.reviewText || '');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [validationErrors, setValidationErrors] = useState<ValidationError[]>([]);

  const handleEdit = () => {
    setIsEditing(true);
    setError(null);
    setValidationErrors([]);
  };

  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setReviewText(e.target.value);
    // Clear validation errors when user starts typing
    setValidationErrors([]);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validate the review text
    const validationError = validateTextField(reviewText, 'Review', TextType.EXTRA_LONG, false);
    if (validationError) {
      setValidationErrors([validationError]);
      return;
    }

    setLoading(true);
    setError(null);
    setValidationErrors([]);

    try {
      if (token) {
        const updatedUgle: UserGameListEntryDTO = {
          ...ugle,
          reviewText: reviewText.trim() || null,
        };
        await updateUGLE(token, updatedUgle);
        
        // Use optimistic update if callback provided, otherwise fall back to refresh
        if (onUpdate) {
          onUpdate(updatedUgle);
        } else {
          await fetchUserData();
        }
        
        setIsEditing(false);
      }
    } catch (err) {
      setError('Failed to update review');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    setIsEditing(false);
    setReviewText(ugle.reviewText || '');
    setError(null);
    setValidationErrors([]);
  };

  const maxLength = TEXT_LIMITS[TextType.EXTRA_LONG];

  return (
    <Modal show={show} onHide={onClose} size="xl">
      <Modal.Header closeButton className="bg-dark text-light">
        <Modal.Title>Review for {ugle.game.title}</Modal.Title>
      </Modal.Header>
      <Modal.Body className="bg-dark text-light">
        {isOwn && isEditing ? (
          <Form onSubmit={handleSubmit}>
            <Form.Group className="mb-3">
              <Form.Label>Review (max {maxLength} characters)</Form.Label>
              <Form.Control
                as="textarea"
                name="reviewText"
                value={reviewText}
                onChange={handleChange}
                rows={10}
                className={`${validationErrors.length > 0 ? 'field-error' : ''}`}
                maxLength={TEXT_LIMITS[TextType.EXTRA_LONG]}
              />
              <div className={`char-counter ${
                reviewText.length > TEXT_LIMITS[TextType.EXTRA_LONG] * 0.9 
                  ? reviewText.length >= TEXT_LIMITS[TextType.EXTRA_LONG] 
                    ? 'at-limit' 
                    : 'near-limit'
                  : ''
              }`}>
                {getCharacterCount(reviewText, TEXT_LIMITS[TextType.EXTRA_LONG])}
              </div>
              {validationErrors.length > 0 && (
                <div className="text-danger mt-1">{validationErrors[0].message}</div>
              )}
            </Form.Group>
            
            {error && <Alert variant="danger">{error}</Alert>}
            {validationErrors.length > 0 && (
              <Alert variant="danger">
                <strong>Please fix the following errors:</strong>
                <ul className="mb-0 mt-2">
                  {validationErrors.map((error, index) => (
                    <li key={index}>{error.message}</li>
                  ))}
                </ul>
              </Alert>
            )}
            
            <Button variant="outline-light" type="submit" disabled={loading}>
              {loading ? 'Saving...' : 'Update'}
            </Button>
            <Button variant="outline-light" onClick={handleCancel} className="ms-2">
              Cancel
            </Button>
          </Form>
        ) : (
          <>
            <div className="mb-3" style={{ 
              wordWrap: 'break-word', 
              whiteSpace: 'pre-wrap',
              maxHeight: '25rem',
              overflowY: 'auto',
              padding: '1rem',
              backgroundColor: '#212529',
              border: '1px solid #495057',
              borderRadius: '0.375rem'
            }}>
              {ugle.reviewText || 'No review'}
            </div>
            {isOwn && (
              <Button variant="outline-light" onClick={handleEdit}>
                Edit
              </Button>
            )}
          </>
        )}
      </Modal.Body>
    </Modal>
  );
};

export default ReviewModal;