// ugleApi.ts
import api from './apiClient';
import { UserGameListEntryDTO } from '../types/UserGameListEntryDTO';

export const createUGLE = async (token: string, dto: UserGameListEntryDTO): Promise<UserGameListEntryDTO> => {
  const response = await api.post(`/usergamelistentries/create`, dto, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};

export const updateUGLE = async (token: string, dto: UserGameListEntryDTO): Promise<UserGameListEntryDTO> => {
  const response = await api.put(`/usergamelistentries/update`, dto, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};

export const deleteUGLE = async (token: string, dto: UserGameListEntryDTO): Promise<void> => {
  await api.delete(`/usergamelistentries/delete`, {
    headers: { Authorization: `Bearer ${token}` },
    data: dto
  });
};