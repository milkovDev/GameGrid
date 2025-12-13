// GameFormModal.tsx (refactored to use shared components)

import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Col, Row, Alert } from 'react-bootstrap';
import { GameDTO } from '../../types/GameDTO';
import { DeveloperDTO } from '../../types/DeveloperDTO';
import { PublisherDTO } from '../../types/PublisherDTO';
import { genreOptions, platformOptions } from '../../types/Enums';
import { useAuth } from '../../contexts/AuthContext';
import { createGame, updateGame } from '../../api/gameApi';
import CreateCompanyModal from './CreateCompanyModal';
import { 
  validateGameForm, 
  ValidationError, 
  TextType, 
  getCharacterHint, 
  getCharacterCount, 
  TEXT_LIMITS 
} from '../../utils/formValidator';
import CompanySelector from './CompanySelector';
import SelectionTags from './SelectionTags';
import '../../styles/ModalStyles.css';

interface Props {
  show: boolean;
  onClose: () => void;
  onSave: (savedGame: GameDTO) => void;
  isCreate: boolean;
  game?: GameDTO;
  developers: DeveloperDTO[];
  publishers: PublisherDTO[];
  refreshDevelopers: () => void;
  refreshPublishers: () => void;
}

const GameFormModal: React.FC<Props> = ({
  show,
  onClose,
  onSave,
  isCreate,
  game,
  developers,
  publishers,
  refreshDevelopers,
  refreshPublishers,
}) => {
  const { token } = useAuth();
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    releaseDate: '',
    coverUrl: '',
    developerId: '',
    publisherId: '',
    selectedGenres: [] as string[],
    selectedPlatforms: [] as string[],
  });
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showDevModal, setShowDevModal] = useState(false);
  const [showPubModal, setShowPubModal] = useState(false);
  const [validationErrors, setValidationErrors] = useState<ValidationError[]>([]);

  useEffect(() => {
    if (game) {
      setFormData({
        title: game.title,
        description: game.description,
        releaseDate: game.releaseDate.slice(0, 10),
        coverUrl: game.coverUrl || '',
        developerId: game.developer.id?.toString() || '',
        publisherId: game.publisher.id?.toString() || '',
        selectedGenres: game.gameGenres.map((g) => g.genre.name),
        selectedPlatforms: game.gamePlatforms.map((p) => p.platform.name),
      });
      setSelectedFile(null);
    } else {
      setFormData({
        title: '',
        description: '',
        releaseDate: '',
        coverUrl: '',
        developerId: '',
        publisherId: '',
        selectedGenres: [],
        selectedPlatforms: [],
      });
    }
    setValidationErrors([]);
  }, [game, show]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setValidationErrors(prev => prev.filter(error => 
      !error.field.toLowerCase().includes(name.toLowerCase())
    ));
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0] || null;
    setSelectedFile(file);
  };

  const toggleSelection = (field: 'selectedGenres' | 'selectedPlatforms') => (item: string) => {
    setFormData((prev) => {
      const current = prev[field];
      const updated = current.includes(item)
        ? current.filter((i) => i !== item)
        : [...current, item];
      return { ...prev, [field]: updated };
    });
    setValidationErrors(prev => prev.filter(error => 
      !error.field.toLowerCase().includes(field.replace('selected', '').slice(0, -1).toLowerCase())
    ));
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

    const validation = validateGameForm(formData, isCreate);
    if (!validation.isValid) {
      setValidationErrors(validation.errors);
      return;
    }

    setLoading(true);
    setError(null);
    setValidationErrors([]);

    const gameDTO: GameDTO = {
      id: isCreate ? undefined : game!.id,
      title: formData.title,
      description: formData.description,
      releaseDate: formData.releaseDate,
      coverUrl: formData.coverUrl || null,
      developer: isCreate
        ? {
            id: parseInt(formData.developerId),
            name: developers.find((d) => d.id === parseInt(formData.developerId))?.name || '',
          }
        : game!.developer,
      publisher: isCreate
        ? {
            id: parseInt(formData.publisherId),
            name: publishers.find((p) => p.id === parseInt(formData.publisherId))?.name || '',
          }
        : game!.publisher,
      gameGenres: formData.selectedGenres.map((g) => ({
        id: undefined,
        genre: { id: undefined, name: g },
      })),
      gamePlatforms: formData.selectedPlatforms.map((p) => ({
        id: undefined,
        platform: { id: undefined, name: p },
      })),
    };

    const formDataToSend = new FormData();
    formDataToSend.append('data', JSON.stringify(gameDTO));
    if (selectedFile) {
      formDataToSend.append('file', selectedFile);
    }

    try {
      let saved;
      if (isCreate) {
        saved = await createGame(token, formDataToSend);
      } else {
        saved = await updateGame(token, formDataToSend);
      }
      onSave(saved);
      onClose();
    } catch (err: any) {
      setError(err.response?.data || 'Failed to save game');
    } finally {
      setLoading(false);
    }
  };

  const selectDeveloper = (dev: DeveloperDTO | null) => {
    setFormData((prev) => ({ ...prev, developerId: dev?.id?.toString() || '' }));
    setValidationErrors(prev => prev.filter(error => 
      !error.field.toLowerCase().includes('developer')
    ));
  };

  const selectPublisher = (pub: PublisherDTO | null) => {
    setFormData((prev) => ({ ...prev, publisherId: pub?.id?.toString() || '' }));
    setValidationErrors(prev => prev.filter(error => 
      !error.field.toLowerCase().includes('publisher')
    ));
  };

  return (
    <>
      <Modal show={show} onHide={onClose} size="lg">
        <Modal.Header closeButton className="bg-dark text-light">
          <Modal.Title>{isCreate ? 'Create Game' : 'Update Game'}</Modal.Title>
        </Modal.Header>
        <Modal.Body className="bg-dark text-light">
          <Form onSubmit={handleSubmit}>
            <Row>
              <Col md={6}>
                <Form.Group controlId="title" className="mb-3">
                  <Form.Label>Title {getCharacterHint(TextType.SHORT)}</Form.Label>
                  <Form.Control
                    type="text"
                    name="title"
                    value={formData.title}
                    onChange={handleChange}
                    className={getFieldError('title') ? 'field-error' : ''}
                    maxLength={TEXT_LIMITS[TextType.SHORT]}
                    required
                  />
                  {getFieldError('title') && (
                    <div className="text-danger mt-1">{getFieldError('title')}</div>
                  )}
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group controlId="releaseDate" className="mb-3">
                  <Form.Label>Release Date</Form.Label>
                  <Form.Control
                    type="date"
                    name="releaseDate"
                    value={formData.releaseDate}
                    onChange={handleChange}
                    className={getFieldError('release date') ? 'field-error' : ''}
                    required
                  />
                  {getFieldError('release date') && (
                    <div className="text-danger mt-1">{getFieldError('release date')}</div>
                  )}
                </Form.Group>
              </Col>
            </Row>

            <Row>
              <Col>
                <Form.Group controlId="description" className="mb-3">
                  <Form.Label>Description {getCharacterHint(TextType.MEDIUM)}</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={4}
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                    className={getFieldError('description') ? 'field-error' : ''}
                    maxLength={TEXT_LIMITS[TextType.MEDIUM]}
                    required
                  />
                  <div className={`char-counter ${
                    formData.description.length > TEXT_LIMITS[TextType.MEDIUM] * 0.9 
                      ? formData.description.length >= TEXT_LIMITS[TextType.MEDIUM] 
                        ? 'at-limit' 
                        : 'near-limit'
                      : ''
                  }`}>
                    {getCharacterCount(formData.description, TEXT_LIMITS[TextType.MEDIUM])}
                  </div>
                  {getFieldError('description') && (
                    <div className="text-danger mt-1">{getFieldError('description')}</div>
                  )}
                </Form.Group>
              </Col>
            </Row>

            {isCreate && (
              <Row>
                <Col md={6}>
                  <CompanySelector
                    label="Developer"
                    companies={developers}
                    selectedId={formData.developerId}
                    onSelect={selectDeveloper}
                    showAddNew={true}
                    onAddNew={() => setShowDevModal(true)}
                    error={getFieldError('developer')}
                  />
                </Col>
                <Col md={6}>
                  <CompanySelector
                    label="Publisher"
                    companies={publishers}
                    selectedId={formData.publisherId}
                    onSelect={selectPublisher}
                    showAddNew={true}
                    onAddNew={() => setShowPubModal(true)}
                    error={getFieldError('publisher')}
                  />
                </Col>
              </Row>
            )}

            <Row>
              <Col>
                <Form.Group controlId="coverImage" className="mb-3">
                  <Form.Label>Cover Image 1920:1080 (optional)</Form.Label>
                  <Form.Control
                    type="file"
                    accept="image/jpeg,image/png,image/gif"
                    onChange={handleFileChange}
                  />
                </Form.Group>
              </Col>
            </Row>

            <Row>
              <Col md={6}>
                <SelectionTags
                  label="Genres"
                  options={genreOptions}
                  selected={formData.selectedGenres}
                  onToggle={toggleSelection('selectedGenres')}
                  error={getFieldError('genre')}
                />
              </Col>
              <Col md={6}>
                <SelectionTags
                  label="Platforms"
                  options={platformOptions}
                  selected={formData.selectedPlatforms}
                  onToggle={toggleSelection('selectedPlatforms')}
                  error={getFieldError('platform')}
                />
              </Col>
            </Row>

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
              {loading ? 'Saving...' : isCreate ? 'Create' : 'Update'}
            </Button>
          </Form>
        </Modal.Body>
      </Modal>

      <CreateCompanyModal
        show={showDevModal}
        onClose={() => setShowDevModal(false)}
        onSave={(newDev: DeveloperDTO) => {
          refreshDevelopers();
          setFormData((prev) => ({ ...prev, developerId: newDev.id!.toString() }));
        }}
        type="developer"
      />

      <CreateCompanyModal
        show={showPubModal}
        onClose={() => setShowPubModal(false)}
        onSave={(newPub: PublisherDTO) => {
          refreshPublishers();
          setFormData((prev) => ({ ...prev, publisherId: newPub.id!.toString() }));
        }}
        type="publisher"
      />
    </>
  );
};

export default GameFormModal;