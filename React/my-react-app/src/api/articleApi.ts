// import { ArticleDTO } from '../types/ArticleDTO';
// import api from './apiClient';

// export const getAllArticles = async (token: string): Promise<ArticleDTO[]> => {
//     const response = await api.get('/articles/getAll', {
//         headers: { Authorization: `Bearer ${token}` }
//     });
//     return response.data;
// };

// export const createArticle = async (token: string, article: Partial<ArticleDTO>): Promise<ArticleDTO> => {
//   const response = await api.post('/articles/create', article, {
//     headers: { Authorization: `Bearer ${token}` }
//   });
//   return response.data;
// };

// export const updateArticle = async (token: string, article: ArticleDTO): Promise<ArticleDTO> => {
//   const response = await api.put('/articles/update', article, {
//     headers: { Authorization: `Bearer ${token}` }
//   });
//   return response.data;
// };

// src/api/articleApi.ts
import { ArticleDTO } from '../types/ArticleDTO';
import api from './apiClient';

export const getAllArticles = async (token: string): Promise<ArticleDTO[]> => {
    const response = await api.get('/articles/getAll', {
        headers: { Authorization: `Bearer ${token}` }
    });
    return response.data;
};

export const createArticle = async (token: string, formData: FormData): Promise<ArticleDTO> => {
  const response = await api.post('/articles/create', formData, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};

export const updateArticle = async (token: string, formData: FormData): Promise<ArticleDTO> => {
  const response = await api.put('/articles/update', formData, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};