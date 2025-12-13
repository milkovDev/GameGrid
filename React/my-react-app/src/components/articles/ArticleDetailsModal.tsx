import React from 'react';
import { Modal, Button, Row, Col, Card } from 'react-bootstrap';
import { ArticleDTO } from '../../types/ArticleDTO';
import { imageApi } from '../../api/imageApi';
import '../../styles/ModalStyles.css';

interface Props {
    show: boolean;
    onClose: () => void;
    article?: ArticleDTO;
}

const ArticleDetailsModal: React.FC<Props> = ({ show, onClose, article }) => {
    if (!article) return null;

    const imageUrl = article.featuredImageUrl ? imageApi.getArticleImage(article.featuredImageUrl) : '/images/bg.png';

    // Sort blocks by position to ensure correct order
    const sortedBlocks = [...article.articleBlocks].sort((a, b) => a.position - b.position);

    return (
        <Modal show={show} onHide={onClose} size="xl">
            <Modal.Header closeButton className="bg-dark text-light">
                <Modal.Title>{article.title}</Modal.Title>
            </Modal.Header>
            <Modal.Body className="bg-dark text-light">
                <Row>
                    <Col>
                        <Card.Img
                            src={imageUrl}
                            style={{ height: 'auto', objectFit: 'cover' }}
                        />
                    </Col>
                </Row>
                {sortedBlocks.map((block) => (
                    <Row className="mt-3" key={block.id || block.position}>
                        <Col>
                            {block.blockType === 'HEADING' ? (
                                <h3 className="article-heading">{block.content}</h3>
                            ) : (
                                <p className="article-paragraph">{block.content}</p>
                            )}
                        </Col>
                    </Row>
                ))}
                <Row>
                    <Col>
                        <p><strong>Author:</strong> {article.author}</p>
                        <p><strong>Published:</strong> {new Date(article.publishedAt).toLocaleDateString('en-GB')}</p>
                    </Col>
                </Row>
            </Modal.Body>
            <Modal.Footer className="bg-dark text-light">
                <Button variant="outline-light" onClick={onClose}>
                    Close
                </Button>
            </Modal.Footer>
        </Modal>
    );
};

export default ArticleDetailsModal;