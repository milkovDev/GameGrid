import { DeveloperDTO } from '../types/DeveloperDTO';
import api from './apiClient';

export const getAllDevelopers = async (token: string): Promise<DeveloperDTO[]> => {
  const response = await api.get('/developers/getAll', {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};

export const createDeveloper = async (token: string, name: string): Promise<DeveloperDTO> => {
  const response = await api.post('/developers/create', { name }, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};