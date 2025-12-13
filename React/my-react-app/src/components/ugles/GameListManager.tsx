import React, { useState, useMemo } from 'react';
import { Row, Col, Form } from 'react-bootstrap';
import { UserGameListEntryDTO } from '../../types/UserGameListEntryDTO';
import UgleCard from '../ugles/UgleCard';

type SortField = 'title' | 'status' | 'favorite' | 'rating';
type SortDirection = 'asc' | 'desc';

interface Props {
  ugles: UserGameListEntryDTO[];
  userName: string;
  isOwn: boolean;
  isUpdating?: boolean;
  onUpdate?: (updatedUgle: UserGameListEntryDTO) => void;
  onDelete?: (ugleId: number) => void;
}

const GameListManager: React.FC<Props> = ({
  ugles,
  userName,
  isOwn,
  isUpdating = false,
  onUpdate,
  onDelete,
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [sortField, setSortField] = useState<SortField>('title');
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc');

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  const getSortIcon = (field: SortField) => {
    if (sortField !== field) return '⮁'; 
    return sortDirection === 'asc' ? '🠕' : '🠗';
  };

  const filteredAndSortedUgles = useMemo(() => {
    return ugles
      .filter(ugle => 
        ugle.game.title.toLowerCase().includes(searchTerm.toLowerCase())
      )
      .sort((a, b) => {
        let comparison = 0;
        
        switch (sortField) {
          case 'title':
            comparison = a.game.title.toLowerCase().localeCompare(b.game.title.toLowerCase());
            break;
          case 'status':
            comparison = a.status.localeCompare(b.status);
            break;
          case 'favorite':
            comparison = a.isFavorite === b.isFavorite ? 0 : a.isFavorite ? -1 : 1;
            break;
          case 'rating':
            if (a.rating === null && b.rating === null) comparison = 0;
            else if (a.rating === null) comparison = 1;
            else if (b.rating === null) comparison = -1;
            else comparison = a.rating - b.rating;
            break;
        }
        
        return sortDirection === 'asc' ? comparison : -comparison;
      });
  }, [ugles, searchTerm, sortField, sortDirection]);

  const SortableHeader: React.FC<{ field: SortField; children: React.ReactNode; className?: string }> = ({ 
    field, 
    children, 
    className = "" 
  }) => (
    <span 
      className={`sortable-header ${sortField === field ? 'active' : ''} ${className}`}
      onClick={() => !isUpdating && handleSort(field)}
      style={{ 
        padding: '0.25rem',
        cursor: isUpdating ? 'not-allowed' : 'pointer'
      }}
    >
      {children} <span>{getSortIcon(field)}</span>
    </span>
  );

  const entryCount = ugles.length;
  const displayName = isOwn ? 'My' : `${userName}'s`;

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3 mt-4">
        <h2 className="mb-0">{displayName} Games ({entryCount})</h2>
        <Form.Control
          type="text"
          placeholder="Search by title..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          disabled={isUpdating}
          style={{ 
            width: '21.3rem', 
            borderColor: '#000000',
            borderWidth: '0.05rem',
            opacity: isUpdating ? 0.8 : 1 
          } as React.CSSProperties}
        />
      </div>

      {entryCount > 0 ? (
        <>
          {filteredAndSortedUgles.length > 0 ? (
            <div className={isUpdating ? 'updating-overlay' : ''}>
              <Row 
                className="mb-2 bg-dark text-light p-2 mx-0 align-items-center" 
                style={{ borderRadius: '0.5rem', fontWeight: 'bold' }}
              >
                <Col xs={2}></Col>
                <Col xs={4}>
                  <SortableHeader field="title">Title</SortableHeader>
                </Col>
                <Col xs={2} className="text-center">
                  <SortableHeader field="status">Status</SortableHeader>
                </Col>
                <Col xs={1} className="text-center">
                  <SortableHeader field="favorite">Favorite</SortableHeader>
                </Col>
                <Col xs={1} className="text-center">
                  <SortableHeader field="rating">Rating</SortableHeader>
                </Col>
                <Col xs={2}></Col>
              </Row>
              
              {filteredAndSortedUgles.map((ugle) => (
                <UgleCard 
                  key={`ugle-${ugle.id}-${ugle.rating}-${ugle.isFavorite}-${ugle.status}`}
                  ugle={ugle} 
                  onUpdate={onUpdate}
                  onDelete={onDelete}
                  isOwn={isOwn}
                />
              ))}
            </div>
          ) : (
            <div className="text-center mt-5">
              <h3>No games found matching your search.</h3>
            </div>
          )}
        </>
      ) : (
        <div className="text-center mt-5">
          <h3>{isOwn ? 'No games in your list yet.' : 'No games in this user\'s list.'}</h3>
        </div>
      )}

      {isUpdating && (
        <style>
        {`
          .updating-overlay {
            position: relative;
            pointer-events: none;
            opacity: 0.8;
            transition: opacity 0.2s ease;
          }
        `}
        </style>
      )}
    </>
  );
};

export default GameListManager;