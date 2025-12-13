import { PublisherDTO } from '../types/PublisherDTO';
import api from './apiClient';

export const getAllPublishers = async (token: string): Promise<PublisherDTO[]> => {
  const response = await api.get('/publishers/getAll', {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};

export const createPublisher = async (token: string, name: string): Promise<PublisherDTO> => {
  const response = await api.post('/publishers/create', { name }, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};