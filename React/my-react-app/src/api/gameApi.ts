import { GameDTO } from '../types/GameDTO';
import api from './apiClient';

export const getAllGames = async (token: string): Promise<GameDTO[]> => {
    const response = await api.get('/games/getAll', {
        headers: { Authorization: `Bearer ${token}` }
    });
    return response.data;
};

export const createGame = async (token: string, formData: FormData): Promise<GameDTO> => {
  const response = await api.post('/games/create', formData, {
    headers: { Authorization: `Bearer ${token}` }  // Do NOT set Content-Type; let browser handle for FormData
  });
  return response.data;
};

export const updateGame = async (token: string, formData: FormData): Promise<GameDTO> => {
  const response = await api.put('/games/update', formData, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};