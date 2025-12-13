import React from 'react';
import { Form, Button, Card, Dropdown } from 'react-bootstrap';
import { ArticleBlockDTO } from '../../types/ArticleBlockDTO';
import { ValidationError, TextType, getCharacterHint, getCharacterCount, TEXT_LIMITS } from '../../utils/formValidator';

interface Props {
  blocks: ArticleBlockDTO[];
  onBlocksChange: (blocks: ArticleBlockDTO[]) => void;
  validationErrors: ValidationError[];
  onClearValidationError: (fieldPattern: string) => void;
}

const ArticleBlockManager: React.FC<Props> = ({
  blocks,
  onBlocksChange,
  validationErrors,
  onClearValidationError,
}) => {
  const getBlockError = (index: number): string | null => {
    const error = validationErrors.find(error => 
      error.field.toLowerCase().includes(`block ${index + 1}`)
    );
    return error ? error.message : null;
  };

  const getGeneralBlockError = (): string | null => {
    const error = validationErrors.find(error => 
      error.field.toLowerCase().includes('article block') && !error.field.includes('Block ')
    );
    return error ? error.message : null;
  };

  const addBlock = () => {
    if (blocks.length >= 9) {
      return; // Let parent handle validation
    }
    const newBlock: ArticleBlockDTO = {
      blockType: 'PARAGRAPH',
      content: '',
      position: blocks.length + 1,
    };
    onBlocksChange([...blocks, newBlock]);
    onClearValidationError('article block');
  };

  const updateBlock = (index: number, field: keyof ArticleBlockDTO, value: any) => {
    const updatedBlocks = [...blocks];
    updatedBlocks[index] = { ...updatedBlocks[index], [field]: value };
    onBlocksChange(updatedBlocks);
    onClearValidationError(`block ${index + 1}`);
  };

  const deleteBlock = (index: number) => {
    const updatedBlocks = blocks
      .filter((_, i) => i !== index)
      .map((block, i) => ({ ...block, position: i + 1 }));
    onBlocksChange(updatedBlocks);
    onClearValidationError('block');
  };

  const moveBlock = (index: number, direction: 'up' | 'down') => {
    const updatedBlocks = [...blocks];
    if (direction === 'up' && index > 0) {
      [updatedBlocks[index - 1], updatedBlocks[index]] = [updatedBlocks[index], updatedBlocks[index - 1]];
    } else if (direction === 'down' && index < updatedBlocks.length - 1) {
      [updatedBlocks[index], updatedBlocks[index + 1]] = [updatedBlocks[index + 1], updatedBlocks[index]];
    }
    const repositioned = updatedBlocks.map((block, i) => ({ ...block, position: i + 1 }));
    onBlocksChange(repositioned);
    onClearValidationError('block');
  };

  return (
    <Form.Group className="mb-3">
      <Form.Label>Article Blocks (max 9)</Form.Label>
      <div className="mb-3">
        <Button 
          variant="outline-light" 
          onClick={addBlock}
          disabled={blocks.length >= 9}
        >
          Add Block
        </Button>
      </div>
      
      {getGeneralBlockError() && blocks.length === 0 && (
        <div className="text-danger mb-2">{getGeneralBlockError()}</div>
      )}
      {blocks.length >= 9 && (
        <div className="text-warning mb-2">Maximum of 9 blocks reached</div>
      )}
      
      {blocks.map((block, index) => (
        <Card 
          key={index} 
          className={`mb-2 bg-dark ${getBlockError(index) ? 'border-danger' : ''}`} 
          style={{ border: getBlockError(index) ? '1px solid #dc3545' : '1px solid #6c757d' }}
        >
          <Card.Body>
            <div className="d-flex align-items-center gap-2">
              <div style={{ minWidth: '8.75rem', flexShrink: 0 }}>
                <Dropdown>
                  <Dropdown.Toggle
                    variant="outline-light"
                    id={`block-type-dropdown-${index}`}
                    className="w-100 text-start"
                  >
                    {block.blockType}
                  </Dropdown.Toggle>
                  <Dropdown.Menu className="dropdown-menu-dark w-100">
                    {['PARAGRAPH', 'HEADING'].map((type) => (
                      <Dropdown.Item
                        key={type}
                        onClick={() => updateBlock(index, 'blockType', type)}
                        className="dropdown-item-dark"
                      >
                        {type}
                      </Dropdown.Item>
                    ))}
                  </Dropdown.Menu>
                </Dropdown>
              </div>
              
              <div className="flex-grow-1">
                {block.blockType === 'PARAGRAPH' ? (
                  <>
                    <Form.Control
                      as="textarea"
                      rows={3}
                      placeholder={`Paragraph content ${getCharacterHint(TextType.LONG)}`}
                      value={block.content}
                      onChange={(e) => updateBlock(index, 'content', e.target.value)}
                      className={getBlockError(index) ? 'field-error' : ''}
                      maxLength={TEXT_LIMITS[TextType.LONG]}
                    />
                    <div className={`char-counter ${
                      block.content.length > TEXT_LIMITS[TextType.LONG] * 0.9 
                        ? block.content.length >= TEXT_LIMITS[TextType.LONG] 
                          ? 'at-limit' 
                          : 'near-limit'
                        : ''
                    }`}>
                      {getCharacterCount(block.content, TEXT_LIMITS[TextType.LONG])}
                    </div>
                  </>
                ) : (
                  <Form.Control
                    type="text"
                    placeholder={`Heading content ${getCharacterHint(TextType.SHORT)}`}
                    value={block.content}
                    onChange={(e) => updateBlock(index, 'content', e.target.value)}
                    className={getBlockError(index) ? 'field-error' : ''}
                    maxLength={TEXT_LIMITS[TextType.SHORT]}
                  />
                )}
                {getBlockError(index) && (
                  <div className="text-danger mt-1 small">{getBlockError(index)}</div>
                )}
              </div>
              
              <div className="d-flex gap-1" style={{ flexShrink: 0 }}>
                <Button
                  variant="outline-light"
                  size="sm"
                  onClick={() => moveBlock(index, 'up')}
                  disabled={index === 0}
                >
                  Up
                </Button>
                <Button
                  variant="outline-light"
                  size="sm"
                  onClick={() => moveBlock(index, 'down')}
                  disabled={index === blocks.length - 1}
                >
                  Down
                </Button>
                <Button
                  variant="outline-danger"
                  size="sm"
                  onClick={() => deleteBlock(index)}
                >
                  Delete
                </Button>
              </div>
            </div>
          </Card.Body>
        </Card>
      ))}
    </Form.Group>
  );
};

export default ArticleBlockManager;