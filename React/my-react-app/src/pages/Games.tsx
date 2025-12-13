import React, { useState, useEffect } from 'react';
import { getAllGames } from '../api/gameApi';
import { getAllDevelopers } from '../api/developerApi';
import { getAllPublishers } from '../api/publisherApi';
import { useAuth } from '../contexts/AuthContext';
import { useUser } from '../contexts/UserContext';
import GameCard from '../components/games/GameCard';
import GameFormModal from '../components/games/GameFormModal';
import GameFilterModal from '../components/games/GameFilterModal';
import GameDetailsModal from '../components/games/GameDetailsModal';
import { GameDTO } from '../types/GameDTO';
import { DeveloperDTO } from '../types/DeveloperDTO';
import { PublisherDTO } from '../types/PublisherDTO';
import { Button, Form } from 'react-bootstrap';

const Games: React.FC = () => {
  const { token, roles } = useAuth();
  const isSuperuser = roles.includes('superuser');
  const [games, setGames] = useState<GameDTO[]>([]);
  const [developers, setDevelopers] = useState<DeveloperDTO[]>([]);
  const [publishers, setPublishers] = useState<PublisherDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [showFilterModal, setShowFilterModal] = useState(false);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [isCreate, setIsCreate] = useState(true);
  const [currentGame, setCurrentGame] = useState<GameDTO | undefined>(undefined);
  const [currentGameDetails, setCurrentGameDetails] = useState<GameDTO | undefined>(undefined);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterDeveloperId, setFilterDeveloperId] = useState<number | null>(null);
  const [filterPublisherId, setFilterPublisherId] = useState<number | null>(null);
  const [filterGenres, setFilterGenres] = useState<string[]>([]);
  const [filterPlatforms, setFilterPlatforms] = useState<string[]>([]);

  useEffect(() => {
    if (!token) return;
    const fetchData = async () => {
      try {
        const [gamesData, devsData, pubsData] = await Promise.all([
          getAllGames(token),
          getAllDevelopers(token),
          getAllPublishers(token),
        ]);
        setGames(gamesData);
        setDevelopers(devsData);
        setPublishers(pubsData);
      } catch (err) {
        setError('Failed to load data');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [token]);

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

  const handleSave = (savedGame: GameDTO) => {
    if (isCreate) {
      setGames([...games, savedGame]);
    } else {
      setGames(games.map((g) => (g.id === savedGame.id ? savedGame : g)));
    }
  };

  // Filter and sort games by release date (newest first)
  const filteredAndSortedGames = games.filter((game) => {
    const titleMatch = game.title.toLowerCase().includes(searchTerm.toLowerCase());
    const devMatch = filterDeveloperId ? game.developer.id === filterDeveloperId : true;
    const pubMatch = filterPublisherId ? game.publisher.id === filterPublisherId : true;
    const genresMatch = filterGenres.length > 0
      ? filterGenres.every((genre) => game.gameGenres.some((g) => g.genre.name === genre))
      : true;
    const platformsMatch = filterPlatforms.length > 0
      ? filterPlatforms.every((platform) => game.gamePlatforms.some((p) => p.platform.name === platform))
      : true;
    return titleMatch && devMatch && pubMatch && genresMatch && platformsMatch;
  }).sort((a, b) => {
    // Sort by release date, newest first
    const dateA = new Date(a.releaseDate).getTime();
    const dateB = new Date(b.releaseDate).getTime();
    return dateB - dateA; // Descending order (newest first)
  });

  if (loading) return <div className="text-center mt-5">Loading...</div>;
  if (error) return <div className="text-center mt-5 text-danger">{error}</div>;

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div className="d-flex align-items-center gap-2">
          <Button variant="outline-dark" onClick={() => setShowFilterModal(true)}>
            Filters
          </Button>
          <Form.Control
            type="text"
            placeholder="Search by title..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ 
              width: '21.3rem', 
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
          <Button variant="outline-dark" size="lg" onClick={() => { setIsCreate(true); setCurrentGame(undefined); setShowModal(true); }}>
            + Add Game
          </Button>
        )}
      </div>
      {filteredAndSortedGames.length > 0 ? (
        <div className="row">
          {filteredAndSortedGames.map(game => (
            <div key={game.id} className="col-md-4">
              <GameCard 
                game={game} 
                onEdit={isSuperuser ? () => { setIsCreate(false); setCurrentGame(game); setShowModal(true); } : undefined} 
                onDetails={() => { setCurrentGameDetails(game); setShowDetailsModal(true); }}
              />
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center mt-5">
          <h1>No games found</h1>
        </div>
      )}
      <GameFormModal
        show={showModal}
        onClose={() => setShowModal(false)}
        onSave={handleSave}
        isCreate={isCreate}
        game={currentGame}
        developers={developers}
        publishers={publishers}
        refreshDevelopers={refreshDevelopers}
        refreshPublishers={refreshPublishers}
      />
      <GameFilterModal
        show={showFilterModal}
        onClose={() => setShowFilterModal(false)}
        onApply={(filters) => {
          setFilterDeveloperId(filters.developerId);
          setFilterPublisherId(filters.publisherId);
          setFilterGenres(filters.genres);
          setFilterPlatforms(filters.platforms);
        }}
        currentFilters={{
          developerId: filterDeveloperId,
          publisherId: filterPublisherId,
          genres: filterGenres,
          platforms: filterPlatforms,
        }}
        developers={developers}
        publishers={publishers}
      />
      <GameDetailsModal
        show={showDetailsModal}
        onClose={() => setShowDetailsModal(false)}
        game={currentGameDetails}
      />
    </div>
  );
};

export default Games;