// SelectionTags.tsx (new shared component)

import React from 'react';
import { Form } from 'react-bootstrap';

interface SelectionTagsProps {
  label: string;
  options: string[];
  selected: string[];
  onToggle: (item: string) => void;
  error?: string | null;
}

const SelectionTags: React.FC<SelectionTagsProps> = ({
  label,
  options,
  selected,
  onToggle,
  error,
}) => {
  return (
    <Form.Group className="mb-3">
      <Form.Label>{label}</Form.Label>
      <div className="d-flex flex-wrap">
        {options.map((item) => (
          <div
            key={item}
            className={`selection-tag ${selected.includes(item) ? 'selected' : ''}`}
            onClick={() => onToggle(item)}
          >
            {item}
          </div>
        ))}
      </div>
      {error && <div className="text-danger mt-1">{error}</div>}
    </Form.Group>
  );
};

export default SelectionTags;