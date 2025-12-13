
import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Alert } from 'react-bootstrap';
import { createDeveloper } from '../../api/developerApi';
import { createPublisher } from '../../api/publisherApi';
import { useAuth } from '../../contexts/AuthContext';
import { 
  validateEntityForm, 
  ValidationError, 
  TextType, 
  getCharacterHint 
} from '../../utils/formValidator';
import '../../styles/ModalStyles.css';

interface Props {
  show: boolean;
  onClose: () => void;
  onSave: (newEntity: any) => void;
  type: 'developer' | 'publisher';
}

const CreateCompanyModal: React.FC<Props> = ({ show, onClose, onSave, type }) => {
  const { token } = useAuth();
  const [name, setName] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<ValidationError[]>([]);

  useEffect(() => {
    if (show) {
      setName('');
      setValidationErrors([]);
      setError(null);
    }
  }, [show]);

  const handleNameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setName(e.target.value);
    // Clear validation errors when user starts typing
    setValidationErrors([]);
  };

  const getFieldError = (fieldName: string): string | null => {
    const error = validationErrors.find(error => 
      error.field.toLowerCase().includes(fieldName.toLowerCase())
    );
    return error ? error.message : null;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) return;

    // Validate form
    const validation = validateEntityForm(name, type);
    if (!validation.isValid) {
      setValidationErrors(validation.errors);
      return;
    }

    setLoading(true);
    setError(null);
    setValidationErrors([]);
    
    try {
      let newEntity;
      if (type === 'developer') {
        newEntity = await createDeveloper(token, name);
      } else {
        newEntity = await createPublisher(token, name);
      }
      onSave(newEntity);
      onClose();
    } catch (err: any) {
      setError(err.response?.data || 'Failed to create');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal show={show} onHide={onClose}>
      <Modal.Header closeButton className="bg-dark text-light">
        <Modal.Title>Create New {type.charAt(0).toUpperCase() + type.slice(1)}</Modal.Title>
      </Modal.Header>
      <Modal.Body className="bg-dark text-light">
        <Form onSubmit={handleSubmit}>
          <Form.Group controlId="name" className="mb-3">
            <Form.Label>Name {getCharacterHint(TextType.SHORT)}</Form.Label>
            <Form.Control
              type="text"
              value={name}
              onChange={handleNameChange}
              className={getFieldError('name') ? 'field-error' : ''}
              required
            />
            {getFieldError('name') && (
              <div className="text-danger mt-1">{getFieldError('name')}</div>
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
            {loading ? 'Creating...' : 'Create'}
          </Button>
        </Form>
      </Modal.Body>
    </Modal>
  );
};

export default CreateCompanyModal;