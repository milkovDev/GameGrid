import { CombinedUserDTO } from '../types/CombinedUserDTO';
import { UserDTO } from '../types/UserDTO';
import api from './apiClient';

export const getUser = async (token: string): Promise<CombinedUserDTO> => {
    const response = await api.get(`/users/me`, {
        headers: { Authorization: `Bearer ${token}` }
    });
    // Convert following and followers to Set objects
    const data = response.data as CombinedUserDTO;
    return {
        ...data,
        graphData: {
            ...data.graphData,
            following: new Set(data.graphData.following),
            followers: new Set(data.graphData.followers)
        }
    };
};

export const updateUser = async (token: string, formData: FormData): Promise<UserDTO> => {
  const response = await api.put('/users/update', formData, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};

export const getAllUsers = async (token: string): Promise<UserDTO[]> => {
    const response = await api.get(`/users/getAll`, {
        headers: { Authorization: `Bearer ${token}` }
    });
    return response.data;
};

export const getById = async (token: string, userId: string): Promise<UserDTO> => {
    const response = await api.get(`/users/${userId}`, {
        headers: { Authorization: `Bearer ${token}` }
    });
    return response.data;
};

export const followUser = async (token: string, followedId: string): Promise<void> => {
    await api.post(`/users/follow/${followedId}`, {}, {
        headers: { Authorization: `Bearer ${token}` }
    });
};

export const unfollowUser = async (token: string, followedId: string): Promise<void> => {
    await api.delete(`/users/unfollow/${followedId}`, {
        headers: { Authorization: `Bearer ${token}` }
    });
};

export const getUserFollowing = async (token: string, userId: string): Promise<UserDTO[]> => {
  const response = await api.get(`/users/following/${userId}`, {
      headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};

export const getUserFollowers = async (token: string, userId: string): Promise<UserDTO[]> => {
  const response = await api.get(`/users/followers/${userId}`, {
      headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};