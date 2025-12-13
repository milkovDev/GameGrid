import React, { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useUser } from '../contexts/UserContext';
import { useNotifications } from '../contexts/NotificationContext';
import { getAllGames } from '../api/gameApi';
import { getAllArticles } from '../api/articleApi';
import { GameDTO } from '../types/GameDTO';
import { ArticleDTO } from '../types/ArticleDTO';
import GameCard from '../components/games/GameCard';
import ArticleCard from '../components/articles/ArticleCard';
import GameFormModal from '../components/games/GameFormModal';
import GameDetailsModal from '../components/games/GameDetailsModal';
import ArticleFormModal from '../components/articles/ArticleFormModal';
import ArticleDetailsModal from '../components/articles/ArticleDetailsModal';
import { getAllDevelopers } from '../api/developerApi';
import { getAllPublishers } from '../api/publisherApi';
import { DeveloperDTO } from '../types/DeveloperDTO';
import { PublisherDTO } from '../types/PublisherDTO';
import { Button } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';

const Home: React.FC = () => {
    const { isAuthenticated, token, roles } = useAuth();
    const { fetchUserData } = useUser();
    const { fetchNotifications } = useNotifications();
    
    const [games, setGames] = useState<GameDTO[]>([]);
    const [articles, setArticles] = useState<ArticleDTO[]>([]);
    const [developers, setDevelopers] = useState<DeveloperDTO[]>([]);
    const [publishers, setPublishers] = useState<PublisherDTO[]>([]);
    const [loading, setLoading] = useState(true);
    
    // Modal states for games
    const [showGameModal, setShowGameModal] = useState(false);
    const [showGameDetailsModal, setShowGameDetailsModal] = useState(false);
    const [isCreateGame, setIsCreateGame] = useState(true);
    const [currentGame, setCurrentGame] = useState<GameDTO | undefined>(undefined);
    const [currentGameDetails, setCurrentGameDetails] = useState<GameDTO | undefined>(undefined);
    
    // Modal states for articles
    const [showArticleModal, setShowArticleModal] = useState(false);
    const [showArticleDetailsModal, setShowArticleDetailsModal] = useState(false);
    const [isCreateArticle, setIsCreateArticle] = useState(true);
    const [currentArticle, setCurrentArticle] = useState<ArticleDTO | undefined>(undefined);
    const [currentArticleDetails, setCurrentArticleDetails] = useState<ArticleDTO | undefined>(undefined);

    const isSuperuser = roles.includes('superuser');
    const navigate = useNavigate();

    const handleGamesClick = () => {
        navigate(`/games`);
    };

    const handleArticlesClick = () => {
        navigate(`/articles`);
    };

    useEffect(() => {
        if (isAuthenticated) {
            fetchUserData();
            fetchNotifications();
            fetchHomeData();
        }
    }, [isAuthenticated, fetchUserData]);

    const fetchHomeData = async () => {
        if (!token) return;
        
        try {
            const [gamesData, articlesData, devsData, pubsData] = await Promise.all([
                getAllGames(token),
                getAllArticles(token),
                getAllDevelopers(token),
                getAllPublishers(token),
            ]);
            
            // Get latest 3 games (by release date) and articles (by published date)
            const latestGames = gamesData
                .sort((a, b) => new Date(b.releaseDate).getTime() - new Date(a.releaseDate).getTime())
                .slice(0, 3);
            const latestArticles = articlesData
                .sort((a, b) => new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime())
                .slice(0, 3);
            
            setGames(latestGames);
            setArticles(latestArticles);
            setDevelopers(devsData);
            setPublishers(pubsData);
        } catch (error) {
            console.error('Failed to fetch home data:', error);
        } finally {
            setLoading(false);
        }
    };

    const refreshDevelopers = async () => {
        if (token) {
            const devs = await getAllDevelopers(token);
            setDevelopers(devs);
        }
    };

    const refreshPublishers = async () => {
        if (token) {
            const pubs = await getAllPublishers(token);
            setPublishers(pubs);
        }
    };

    const handleGameSave = (savedGame: GameDTO) => {
        if (isCreateGame) {
            setGames([savedGame, ...games.slice(0, 2)]); // Add to front, keep only 3
        } else {
            setGames(games.map((g) => (g.id === savedGame.id ? savedGame : g)));
        }
    };

    const handleArticleSave = (savedArticle: ArticleDTO) => {
        if (isCreateArticle) {
            setArticles([savedArticle, ...articles.slice(0, 2)]); // Add to front, keep only 3
        } else {
            setArticles(articles.map((a) => (a.id === savedArticle.id ? savedArticle : a)));
        }
        
        // Update current article states if needed
        if (currentArticle?.id === savedArticle.id) {
            setCurrentArticle(savedArticle);
        }
        if (currentArticleDetails?.id === savedArticle.id) {
            setCurrentArticleDetails(savedArticle);
        }
    };

    if (loading) {
        return <div className="text-center mt-5">Loading...</div>;
    }

    return (
        <div className="container mt-5">
            <div className="text-center mb-5">
                <h1>Welcome to GameGrid</h1>
                <p className="lead fw-normal">Explore the hottest games, latest articles, and connect with other game enthusiasts!</p>
            </div>

            {/* Latest Games Section */}
            <div className="mb-5">
                <div className="d-flex justify-content-between align-items-center mb-3">
                    <h2>Latest Games</h2>
                    <Button variant="outline-dark" size="lg" onClick={handleGamesClick}>View All Games</Button>
                </div>
                {games.length > 0 ? (
                    <div className="row">
                        {games.map(game => (
                            <div key={game.id} className="col-md-4">
                                <GameCard 
                                    game={game} 
                                    onEdit={isSuperuser ? () => { 
                                        setIsCreateGame(false); 
                                        setCurrentGame(game); 
                                        setShowGameModal(true); 
                                    } : undefined} 
                                    onDetails={() => { 
                                        setCurrentGameDetails(game); 
                                        setShowGameDetailsModal(true); 
                                    }}
                                />
                            </div>
                        ))}
                    </div>
                ) : (
                    <div className="text-center">
                        <p>No games available yet.</p>
                    </div>
                )}
            </div>

            {/* Latest Articles Section */}
            <div className="mb-5">
                <div className="d-flex justify-content-between align-items-center mb-3">
                    <h2>Latest Articles</h2>
                    <Button variant="outline-dark" size="lg" onClick={handleArticlesClick}>View All Articles</Button>
                </div>
                {articles.length > 0 ? (
                    <div className="row">
                        {articles.map(article => (
                            <div key={article.id} className="col-md-4">
                                <ArticleCard 
                                    article={article} 
                                    onEdit={isSuperuser ? () => { 
                                        setIsCreateArticle(false); 
                                        setCurrentArticle(article); 
                                        setShowArticleModal(true); 
                                    } : undefined} 
                                    onDetails={() => { 
                                        setCurrentArticleDetails(article); 
                                        setShowArticleDetailsModal(true); 
                                    }}
                                />
                            </div>
                        ))}
                    </div>
                ) : (
                    <div className="text-center">
                        <p>No articles available yet.</p>
                    </div>
                )}
            </div>

            {/* Game Modals */}
            <GameFormModal
                show={showGameModal}
                onClose={() => setShowGameModal(false)}
                onSave={handleGameSave}
                isCreate={isCreateGame}
                game={currentGame}
                developers={developers}
                publishers={publishers}
                refreshDevelopers={refreshDevelopers}
                refreshPublishers={refreshPublishers}
            />
            
            <GameDetailsModal
                show={showGameDetailsModal}
                onClose={() => setShowGameDetailsModal(false)}
                game={currentGameDetails}
            />

            {/* Article Modals */}
            <ArticleFormModal
                show={showArticleModal}
                onClose={() => setShowArticleModal(false)}
                onSave={handleArticleSave}
                isCreate={isCreateArticle}
                article={currentArticle}
            />
            
            <ArticleDetailsModal
                show={showArticleDetailsModal}
                onClose={() => setShowArticleDetailsModal(false)}
                article={currentArticleDetails}
            />
        </div>
    );
};

export default Home;