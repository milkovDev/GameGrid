// CompanySelector.tsx (new shared component)

import React, { useState } from 'react';
import { Form, Dropdown, Button } from 'react-bootstrap';
import { DeveloperDTO } from '../../types/DeveloperDTO';
import { PublisherDTO } from '../../types/PublisherDTO';

type Company = DeveloperDTO | PublisherDTO;

interface CompanySelectorProps {
  label: string;
  companies: Company[];
  selectedId: string;
  onSelect: (company: Company | null) => void;
  showAddNew?: boolean;
  onAddNew?: () => void;
  includeAny?: boolean;
  error?: string | null;
}

const CompanySelector: React.FC<CompanySelectorProps> = ({
  label,
  companies,
  selectedId,
  onSelect,
  showAddNew = false,
  onAddNew,
  includeAny = false,
  error,
}) => {
  const [filter, setFilter] = useState('');

  const filteredCompanies = companies.filter((company) =>
    company.name.toLowerCase().includes(filter.toLowerCase())
  );

  const getDisplayName = () => {
    if (!selectedId) return includeAny ? 'Any' : `Select ${label}`;
    const selected = companies.find((c) => c.id === parseInt(selectedId));
    return selected?.name || `Select ${label}`;
  };

  return (
    <>
      <Form.Group className="mb-3">
        <Form.Label>Filter {label}s</Form.Label>
        <Form.Control
          type="text"
          placeholder={`Filter ${label.toLowerCase()}s...`}
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
        />
      </Form.Group>
      <Form.Group className="mb-3">
        <Form.Label>{label}</Form.Label>
        <div className="d-flex">
          <Dropdown className="flex-grow-1">
            <Dropdown.Toggle
              variant={error ? 'outline-danger' : 'outline-light'}
              id={`${label.toLowerCase()}Dropdown`}
              className="w-100 text-start"
            >
              {getDisplayName()}
            </Dropdown.Toggle>
            <Dropdown.Menu className="dropdown-menu-dark w-100" style={{ maxHeight: '12.5rem', overflowY: 'auto' }}>
              {includeAny && (
                <>
                  <Dropdown.Item
                    onClick={() => onSelect(null)}
                    className="dropdown-item-dark"
                  >
                    Any {label}
                  </Dropdown.Item>
                  <Dropdown.Divider />
                </>
              )}
              {filteredCompanies.length > 0 ? (
                filteredCompanies.map((company) => (
                  <Dropdown.Item
                    key={company.id}
                    onClick={() => onSelect(company)}
                    className="dropdown-item-dark"
                  >
                    {company.name}
                  </Dropdown.Item>
                ))
              ) : (
                <Dropdown.Item disabled className="dropdown-item-dark">
                  No results found
                </Dropdown.Item>
              )}
            </Dropdown.Menu>
          </Dropdown>
          {showAddNew && onAddNew && (
            <Button
              variant="outline-light"
              className="ms-2"
              onClick={onAddNew}
            >
              Add New
            </Button>
          )}
        </div>
        {error && <div className="text-danger mt-1">{error}</div>}
      </Form.Group>
    </>
  );
};

export default CompanySelector;