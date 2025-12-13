// GameFilterModal.tsx (refactored to use shared components)

// GameFilterModal.tsx (new file)
import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Col, Row } from 'react-bootstrap';
import { DeveloperDTO } from '../../types/DeveloperDTO';
import { PublisherDTO } from '../../types/PublisherDTO';
import { genreOptions, platformOptions } from '../../types/Enums';
import CompanySelector from './CompanySelector';
import SelectionTags from './SelectionTags';
import '../../styles/ModalStyles.css';

interface FilterProps {
    developerId: number | null;
    publisherId: number | null;
    genres: string[];
    platforms: string[];
}

interface Props {
    show: boolean;
    onClose: () => void;
    onApply: (filters: FilterProps) => void;
    currentFilters: FilterProps;
    developers: DeveloperDTO[];
    publishers: PublisherDTO[];
}

const GameFilterModal: React.FC<Props> = ({
    show,
    onClose,
    onApply,
    currentFilters,
    developers,
    publishers,
}) => {
    const [formData, setFormData] = useState({
        developerId: '',
        publisherId: '',
        selectedGenres: [] as string[],
        selectedPlatforms: [] as string[],
    });

    useEffect(() => {
        setFormData({
            developerId: currentFilters.developerId?.toString() || '',
            publisherId: currentFilters.publisherId?.toString() || '',
            selectedGenres: currentFilters.genres,
            selectedPlatforms: currentFilters.platforms,
        });
    }, [show, currentFilters]);

    const toggleSelection = (field: 'selectedGenres' | 'selectedPlatforms') => (item: string) => {
        setFormData((prev) => {
          const current = prev[field];
          const updated = current.includes(item)
            ? current.filter((i) => i !== item)
            : [...current, item];
          return { ...prev, [field]: updated };
        });
      };

    const handleApply = () => {
        onApply({
            developerId: formData.developerId ? parseInt(formData.developerId) : null,
            publisherId: formData.publisherId ? parseInt(formData.publisherId) : null,
            genres: formData.selectedGenres,
            platforms: formData.selectedPlatforms,
        });
        onClose();
    };

    const handleClear = () => {
        onApply({
            developerId: null,
            publisherId: null,
            genres: [],
            platforms: [],
        });
        onClose();
    };

    const selectDeveloper = (dev: DeveloperDTO | null) => {
        setFormData((prev) => ({ ...prev, developerId: dev?.id?.toString() || '' }));
    };

    const selectPublisher = (pub: PublisherDTO | null) => {
        setFormData((prev) => ({ ...prev, publisherId: pub?.id?.toString() || '' }));
    };

    return (
        <Modal show={show} onHide={onClose} size="lg">
            <Modal.Header closeButton className="bg-dark text-light">
                <Modal.Title>Filters</Modal.Title>
            </Modal.Header>
            <Modal.Body className="bg-dark text-light">
                <Form>
                    <Row>
                        <Col md={6}>
                          <CompanySelector
                            label="Developer"
                            companies={developers}
                            selectedId={formData.developerId}
                            onSelect={selectDeveloper}
                            includeAny={true}
                          />
                        </Col>
                        <Col md={6}>
                          <CompanySelector
                            label="Publisher"
                            companies={publishers}
                            selectedId={formData.publisherId}
                            onSelect={selectPublisher}
                            includeAny={true}
                          />
                        </Col>
                    </Row>
                    <Row>
                        <Col md={6}>
                            <SelectionTags
                              label="Genres"
                              options={genreOptions}
                              selected={formData.selectedGenres}
                              onToggle={toggleSelection('selectedGenres')}
                            />
                        </Col>
                        <Col md={6}>
                            <SelectionTags
                              label="Platforms"
                              options={platformOptions}
                              selected={formData.selectedPlatforms}
                              onToggle={toggleSelection('selectedPlatforms')}
                            />
                        </Col>
                    </Row>
                    <Button variant="outline-light" onClick={handleApply} className="me-2">
                        Apply
                    </Button>
                    <Button variant="outline-light" onClick={handleClear}>
                        Clear All
                    </Button>
                </Form>
            </Modal.Body>
        </Modal>
    );
};

export default GameFilterModal;