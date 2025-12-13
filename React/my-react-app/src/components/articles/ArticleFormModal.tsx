import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Alert } from 'react-bootstrap';
import { useAuth } from '../../contexts/AuthContext';
import { useUser } from '../../contexts/UserContext';
import { createArticle, updateArticle } from '../../api/articleApi';
import { ArticleDTO } from '../../types/ArticleDTO';
import { 
  validateArticleForm, 
  ValidationError, 
  TextType, 
  getCharacterHint, 
  TEXT_LIMITS 
} from '../../utils/formValidator';
import ArticleBlockManager from './ArticleBlockManager';
import '../../styles/ModalStyles.css';

interface Props {
  show: boolean;
  onClose: () => void;
  onSave: (savedArticle: ArticleDTO) => void;
  isCreate: boolean;
  article?: ArticleDTO;
}

const ArticleFormModal: React.FC<Props> = ({
  show,
  onClose,
  onSave,
  isCreate,
  article,
}) => {
  const { token } = useAuth();
  const { userData } = useUser();
  
  const [formData, setFormData] = useState<ArticleDTO>({
    title: '',
    author: userData?.relationalData?.displayName || '',
    authorId: userData?.relationalData?.id || '',
    publishedAt: new Date().toISOString().slice(0, 19),
    featuredImageUrl: null,
    articleBlocks: [],
  });
  const [featuredFile, setFeaturedFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<ValidationError[]>([]);

  // Reset form when modal opens/closes or article changes
  useEffect(() => {
    const resetFormData = {
      title: '',
      author: userData?.relationalData?.displayName || '',
      authorId: userData?.relationalData?.id || '',
      publishedAt: new Date().toISOString().slice(0, 19),
      featuredImageUrl: null,
      articleBlocks: [],
    };

    if (article) {
      setFormData({
        id: article.id,
        title: article.title,
        author: article.author,
        authorId: article.authorId,
        publishedAt: new Date().toISOString().slice(0, 19),
        featuredImageUrl: article.featuredImageUrl || null,
        articleBlocks: [...article.articleBlocks].sort((a, b) => a.position - b.position),
      });
    } else {
      setFormData(resetFormData);
    }
    
    setFeaturedFile(null);
    setValidationErrors([]);
  }, [article, show, userData]);

  const clearValidationError = (fieldPattern: string) => {
    setValidationErrors(prev => prev.filter(error => 
      !error.field.toLowerCase().includes(fieldPattern.toLowerCase())
    ));
  };

  const getFieldError = (fieldName: string): string | null => {
    const error = validationErrors.find(error => 
      error.field.toLowerCase().includes(fieldName.toLowerCase())
    );
    return error ? error.message : null;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    clearValidationError(name);
  };

  const handleBlocksChange = (articleBlocks: ArticleDTO['articleBlocks']) => {
    setFormData(prev => ({ ...prev, articleBlocks }));
    
    // Add validation for max blocks
    if (articleBlocks.length >= 9) {
      setValidationErrors(prev => {
        const filtered = prev.filter(error => 
          !error.field.toLowerCase().includes('article block')
        );
        return [...filtered, { field: 'Article Blocks', message: 'Maximum of 9 blocks allowed' }];
      });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) return;

    // Validate form
    const validation = validateArticleForm(formData);
    // Filter out character limit errors since maxLength prevents them
    const filteredErrors = validation.errors.filter(error => 
      !error.message.includes('must be') || !error.message.includes('characters or less')
    );
    
    if (filteredErrors.length > 0) {
      setValidationErrors(filteredErrors);
      return;
    }

    setLoading(true);
    setError(null);
    setValidationErrors([]);
    
    try {
      const formDataToSend = new FormData();
      formDataToSend.append('data', new Blob([JSON.stringify(formData)], { type: 'application/json' }));
      if (featuredFile) {
        formDataToSend.append('file', featuredFile);
      }

      const saved = isCreate 
        ? await createArticle(token, formDataToSend)
        : await updateArticle(token, formDataToSend);
        
      onSave(saved);
      onClose();
    } catch (err) {
      setError('Failed to save article');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal show={show} onHide={onClose} size="xl" centered>
      <Modal.Header closeButton className="bg-dark text-light">
        <Modal.Title>{isCreate ? 'Create Article' : 'Update Article'}</Modal.Title>
      </Modal.Header>
      
      <Modal.Body className="bg-dark text-light">
        <Form onSubmit={handleSubmit}>
          <Form.Group className="mb-3">
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

          <Form.Group className="mb-3">
            <Form.Label>Featured Image 1920:1080 (optional)</Form.Label>
            <Form.Control
              type="file"
              accept="image/*"
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setFeaturedFile(e.target.files?.[0] || null)}
            />
            {featuredFile && <small className="text-muted">{featuredFile.name}</small>}
            {formData.featuredImageUrl && !featuredFile && (
              <small className="text-muted">{formData.featuredImageUrl.split('/').pop()}</small>
            )}
          </Form.Group>

          <ArticleBlockManager
            blocks={formData.articleBlocks}
            onBlocksChange={handleBlocksChange}
            validationErrors={validationErrors}
            onClearValidationError={clearValidationError}
          />

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
  );
};

export default ArticleFormModal;