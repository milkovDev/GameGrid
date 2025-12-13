import React, { useState, useEffect } from 'react';
import { getAllArticles } from '../api/articleApi';
import { useAuth } from '../contexts/AuthContext';
import ArticleCard from '../components/articles/ArticleCard';
import ArticleFormModal from '../components/articles/ArticleFormModal';
import ArticleDetailsModal from '../components/articles/ArticleDetailsModal'; // New import
import { ArticleDTO } from '../types/ArticleDTO';
import { Button, Form } from 'react-bootstrap';

const Articles: React.FC = () => {
    const { token, roles } = useAuth();
    const [articles, setArticles] = useState<ArticleDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [showModal, setShowModal] = useState(false);
    const [isCreate, setIsCreate] = useState(true);
    const [currentArticle, setCurrentArticle] = useState<ArticleDTO | undefined>(undefined);
    const [searchTerm, setSearchTerm] = useState('');
    const [showDetailsModal, setShowDetailsModal] = useState(false);
    const [currentArticleDetails, setCurrentArticleDetails] = useState<ArticleDTO | undefined>(undefined);

    const isSuperuser = roles.includes('superuser');

    useEffect(() => {
        if (!token) return;
        const fetchArticles = async () => {
            try {
                const data = await getAllArticles(token);
                setArticles(data);
            } catch (err) {
                setError('Failed to load articles');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };
        fetchArticles();
    }, [token]);

    const handleSave = (savedArticle: ArticleDTO) => {
        if (isCreate) {
          setArticles([...articles, savedArticle]);
        } else {
          setArticles(articles.map((a) => (a.id === savedArticle.id ? savedArticle : a)));
        }
      
        // ensure current editing article is fresh
        if (currentArticle?.id === savedArticle.id) {
          setCurrentArticle(savedArticle);
        }
      
        // ensure details modal shows fresh blocks
        if (currentArticleDetails?.id === savedArticle.id) {
          setCurrentArticleDetails(savedArticle);
        }
    };
      
    // Filter and sort articles by newest first
    const filteredAndSortedArticles = articles
        .filter(article =>
            article.title.toLowerCase().includes(searchTerm.toLowerCase())
        )
        .sort((a, b) => {
            // Sort by publishedAt date, newest first
            const dateA = new Date(a.publishedAt).getTime();
            const dateB = new Date(b.publishedAt).getTime();
            return dateB - dateA; // Descending order (newest first)
        });

    if (loading) return <div className="text-center mt-5">Loading...</div>;
    if (error) return <div className="text-center mt-5 text-danger">{error}</div>;

    return (
        <div className="container mt-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div className="d-flex align-items-center" style={{ flex: 1 }}>
                    <Form.Control
                        type="text"
                        placeholder="Search articles..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="me-2"
                        style={{ 
                            width: '26rem', 
                            borderColor: '#000000',
                            borderWidth: '0.05rem'
                        } as React.CSSProperties}
                    />
                    <style>
                    {`
                        .form-control::placeholder {
                            color: #6c757d;
                            opacity: 1;
                        }
                    `}
                    </style>
                </div>
                {isSuperuser && (
                    <Button variant="outline-dark" size="lg" onClick={() => { setIsCreate(true); setCurrentArticle(undefined); setShowModal(true); }}>
                        + Write Article
                    </Button>
                )}
            </div>
            {filteredAndSortedArticles.length > 0 ? (
                <div className="row">
                    {filteredAndSortedArticles.map(article => (
                        <div key={article.id} className="col-md-4">
                            <ArticleCard 
                                article={article} 
                                onEdit={isSuperuser ? () => { setIsCreate(false); setCurrentArticle(article); setShowModal(true); } : undefined} 
                                onDetails={() => { setCurrentArticleDetails(article); setShowDetailsModal(true); }}
                            />
                        </div>
                    ))}
                </div>
            ) : (
                <div className="text-center mt-5">
                    <h1>No articles found</h1>
                </div>
            )}
            <ArticleFormModal
                show={showModal}
                onClose={() => setShowModal(false)}
                onSave={handleSave}
                isCreate={isCreate}
                article={currentArticle}
            />
            <ArticleDetailsModal
                show={showDetailsModal}
                onClose={() => setShowDetailsModal(false)}
                article={currentArticleDetails}
            />
        </div>
    );
};

export default Articles;