import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Alert, Row, Col } from 'react-bootstrap';
import { useAuth } from '../../contexts/AuthContext';
import { updateUser } from '../../api/userApi';
import { UserDTO } from '../../types/UserDTO';
import { 
  validateTextField, 
  TextType, 
  getCharacterHint, 
  getCharacterCount, 
  TEXT_LIMITS 
} from '../../utils/formValidator';
import '../../styles/ModalStyles.css';

interface Props {
  show: boolean;
  onClose: () => void;
  onSave: () => void;
  user: UserDTO;
}

interface ValidationError {
  field: string;
  message: string;
}

const UserFormModal: React.FC<Props> = ({ show, onClose, onSave, user }) => {
  const { token } = useAuth();
  const [formData, setFormData] = useState<UserDTO>({
    id: '',
    displayName: '',
    bio: '',
    avatarUrl: '',
    userGameListEntries: [],
  });
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<ValidationError[]>([]);

  useEffect(() => {
    setFormData({
      id: user.id,
      displayName: user.displayName,
      bio: user.bio,
      avatarUrl: user.avatarUrl || '',
      userGameListEntries: user.userGameListEntries,
    });
    setValidationErrors([]);
  }, [user, show]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    // Clear validation errors for this field
    setValidationErrors(prev => prev.filter(error => 
      !error.field.toLowerCase().includes(name.toLowerCase())
    ));
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0] || null;
    setSelectedFile(file);
  };

  const validateForm = (): ValidationError[] => {
    const errors: ValidationError[] = [];
    // Validate bio - allow empty (isRequired = false)
    const bioError = validateTextField(formData.bio, 'Bio', TextType.MEDIUM, false);
    if (bioError) {
      errors.push(bioError);
    }
    return errors;
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
    const errors = validateForm();
    if (errors.length > 0) {
      setValidationErrors(errors);
      return;
    }

    setLoading(true);
    setError(null);
    setValidationErrors([]);

    const userDTO: UserDTO = {
      ...formData,
      avatarUrl: formData.avatarUrl || "null",
    };

    userDTO.userGameListEntries = [];

    const formDataToSend = new FormData();
    formDataToSend.append('data', JSON.stringify(userDTO));
    if (selectedFile) {
      formDataToSend.append('file', selectedFile);
    }

    try {
      await updateUser(token, formDataToSend);
      onSave();
      onClose();
    } catch (err: any) {
      setError(err.response?.data || 'Failed to update profile');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal show={show} onHide={onClose} centered size="lg">
      <Modal.Header closeButton className="bg-dark text-light">
        <Modal.Title>Update Profile</Modal.Title>
      </Modal.Header>
      <Modal.Body className="bg-dark text-light">
        <Form onSubmit={handleSubmit}>
          {/* Row 1: Bio */}
          <Row>
            <Col>
              <Form.Group controlId="bio" className="mb-3">
                <Form.Label>Bio {getCharacterHint(TextType.MEDIUM)} (optional)</Form.Label>
                <Form.Control
                  as="textarea"
                  rows={5}
                  name="bio"
                  value={formData.bio}
                  onChange={handleChange}
                  className={getFieldError('bio') ? 'field-error' : ''}
                  placeholder="Tell us about yourself..."
                  maxLength={TEXT_LIMITS[TextType.MEDIUM]}
                />
                <div className={`char-counter ${
                  formData.bio.length > TEXT_LIMITS[TextType.MEDIUM] * 0.9 
                    ? formData.bio.length >= TEXT_LIMITS[TextType.MEDIUM] 
                      ? 'at-limit' 
                      : 'near-limit'
                    : ''
                }`}>
                  {getCharacterCount(formData.bio, TEXT_LIMITS[TextType.MEDIUM])}
                </div>
                {getFieldError('bio') && (
                  <div className="text-danger mt-1">{getFieldError('bio')}</div>
                )}
              </Form.Group>
            </Col>
          </Row>

          {/* Row 2: Avatar Image */}
          <Row>
            <Col>
              <Form.Group controlId="avatarImage" className="mb-3">
                <Form.Label>Avatar Image 512:512 (optional)</Form.Label>
                <Form.Control
                  type="file"
                  accept="image/jpeg,image/png,image/gif"
                  onChange={handleFileChange}
                />
              </Form.Group>
            </Col>
          </Row>

          {error && <Alert variant="danger">{error}</Alert>}
          {validationErrors.length > 0 && (
            <Alert variant="danger">
              <strong>Please fix the following errors:</strong>
              <ul className="mb-0 mt-2">
                {validationErrors.map((err, index) => (
                  <li key={index}>{err.message}</li>
                ))}
              </ul>
            </Alert>
          )}
          <Button variant="outline-light" type="submit" disabled={loading}>
            {loading ? 'Saving...' : 'Update'}
          </Button>
        </Form>
      </Modal.Body>
    </Modal>
  );
};

export default UserFormModal;