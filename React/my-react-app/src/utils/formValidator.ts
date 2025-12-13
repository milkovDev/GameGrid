// Updated formValidator.ts
export interface ValidationError {
  field: string;
  message: string;
}

export interface ValidationResult {
  isValid: boolean;
  errors: ValidationError[];
}

export enum TextType {
  SHORT = 'short',
  MEDIUM = 'medium',
  LONG = 'long',
  EXTRA_LONG = 'extra_long'
}

export const TEXT_LIMITS = {
  [TextType.SHORT]: 100,
  [TextType.MEDIUM]: 1000,
  [TextType.LONG]: 2000,
  [TextType.EXTRA_LONG]: 4000
};

export const getCharacterHint = (type: TextType): string => {
  return `(max ${TEXT_LIMITS[type]} characters)`;
};

export const validateTextField = (
  value: string,
  fieldName: string,
  type: TextType,
  isRequired: boolean = true
): ValidationError | null => {
  const trimmedValue = value?.trim() || '';
  
  if (isRequired && !trimmedValue) {
      return {
          field: fieldName,
          message: `${fieldName} is required`
      };
  }
  
  if (trimmedValue.length > TEXT_LIMITS[type]) {
      return {
          field: fieldName,
          message: `${fieldName} must be ${TEXT_LIMITS[type]} characters or less`
      };
  }
  
  return null;
};

export const validateRequiredField = (
  value: any,
  fieldName: string
): ValidationError | null => {
  if (!value || (typeof value === 'string' && !value.trim())) {
      return {
          field: fieldName,
          message: `${fieldName} is required`
      };
  }
  return null;
};

export const validateArrayNotEmpty = (
  array: any[],
  fieldName: string
): ValidationError | null => {
  if (!array || array.length === 0) {
      return {
          field: fieldName,
          message: `At least one ${fieldName.toLowerCase()} must be selected`
      };
  }
  return null;
};

// Game validation
export const validateGameForm = (formData: {
  title: string;
  description: string;
  releaseDate: string;
  developerId: string;
  publisherId: string;
  selectedGenres: string[];
  selectedPlatforms: string[];
}, isCreate: boolean): ValidationResult => {
  const errors: ValidationError[] = [];
  
  // Validate title
  const titleError = validateTextField(formData.title, 'Title', TextType.SHORT);
  if (titleError) errors.push(titleError);
  
  // Validate description
  const descriptionError = validateTextField(formData.description, 'Description', TextType.MEDIUM);
  if (descriptionError) errors.push(descriptionError);
  
  // Validate release date
  const releaseDateError = validateRequiredField(formData.releaseDate, 'Release Date');
  if (releaseDateError) errors.push(releaseDateError);
  
  // Validate developer and publisher (only for creation)
  if (isCreate) {
      const developerError = validateRequiredField(formData.developerId, 'Developer');
      if (developerError) errors.push(developerError);
      
      const publisherError = validateRequiredField(formData.publisherId, 'Publisher');
      if (publisherError) errors.push(publisherError);
  }
  
  // Validate genres
  const genresError = validateArrayNotEmpty(formData.selectedGenres, 'Genre');
  if (genresError) errors.push(genresError);
  
  // Validate platforms
  const platformsError = validateArrayNotEmpty(formData.selectedPlatforms, 'Platform');
  if (platformsError) errors.push(platformsError);
  
  return {
      isValid: errors.length === 0,
      errors
  };
};

// Article validation
export const validateArticleForm = (formData: {
  title: string;
  articleBlocks: { blockType: string; content: string }[];
}): ValidationResult => {
  const errors: ValidationError[] = [];
  
  // Validate title
  const titleError = validateTextField(formData.title, 'Title', TextType.SHORT);
  if (titleError) errors.push(titleError);
  
  // Validate article blocks
  const blocksError = validateArrayNotEmpty(formData.articleBlocks, 'Article block');
  if (blocksError) errors.push(blocksError);
  
  // Validate each block's content
  formData.articleBlocks.forEach((block, index) => {
      const blockType = block.blockType === 'HEADING' ? TextType.SHORT : TextType.MEDIUM;
      const blockError = validateTextField(
          block.content,
          `Block ${index + 1} content`,
          blockType
      );
      if (blockError) errors.push(blockError);
  });
  
  return {
      isValid: errors.length === 0,
      errors
  };
};

// Entity validation (Developer/Publisher)
export const validateEntityForm = (name: string, type: string): ValidationResult => {
  const errors: ValidationError[] = [];
  
  const nameError = validateTextField(name, `${type} name`, TextType.SHORT);
  if (nameError) errors.push(nameError);
  
  return {
      isValid: errors.length === 0,
      errors
  };
};

export const getCharacterCount = (text: string, maxLength: number): string => {
  const currentLength = text?.length || 0;
  return `${currentLength}/${maxLength} characters`;
};