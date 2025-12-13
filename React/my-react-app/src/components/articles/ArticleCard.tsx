// Updated src/components/ArticleCard.tsx
import React from 'react';
import { ArticleDTO } from '../../types/ArticleDTO';
import { Button, Card } from 'react-bootstrap';
import { imageApi } from '../../api/imageApi';

interface Props {
    article: ArticleDTO;
    onEdit?: () => void;
    onDetails?: () => void;
}

const ArticleCard: React.FC<Props> = ({ article, onEdit, onDetails }) => {
    return (
      <Card className="mb-3 text-light bg-dark">
        {article.featuredImageUrl && (
          <div style={{ 
            width: '100%', 
            aspectRatio: '16/9', 
            overflow: 'hidden' 
          }}>
            <Card.Img
              variant="top"
              src={imageApi.getArticleImage(article.featuredImageUrl)}
              style={{ 
                width: '100%', 
                height: '100%', 
                objectFit: 'cover' 
              }}
            />
          </div>
        )}
        <Card.Body>
          <Card.Title>{article.title}</Card.Title>
          <Card.Text>By: {article.author}</Card.Text>
          <Card.Text>
            <small>
              Published: {new Date(article.publishedAt).toLocaleDateString('en-GB')}
            </small>
          </Card.Text>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
            <Button variant="outline-light" size="sm" onClick={onDetails}>
              Read
            </Button>
            {onEdit && (
              <Button variant="outline-light" size="sm" onClick={onEdit}>
                Edit
              </Button>
            )}
          </div>
        </Card.Body>
      </Card>
    );
  };

export default ArticleCard;