// UserContext.tsx

import React, { createContext, useState, useContext, useCallback } from 'react';
import { useAuth } from './AuthContext'; // Adjust path if needed
import { CombinedUserDTO } from '../types/CombinedUserDTO';
import { getUser } from '../api/userApi';
import { UserGameListEntryDTO } from '../types/UserGameListEntryDTO';
import { AxiosError } from 'axios';

interface UserContextType {
    userData: CombinedUserDTO | null;
    fetchUserData: () => Promise<void>;
    updateUgle: (updatedUgle: UserGameListEntryDTO) => void;
    deleteUgle: (ugleId: number) => void;
    addUgle: (newUgle: UserGameListEntryDTO) => void;
    follow: (followedId: string) => void;
    unfollow: (followedId: string) => void;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

export const UserProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const auth = useAuth();
    const [userData, setUserData] = useState<CombinedUserDTO | null>(null);

    const fetchUserData = useCallback(async (): Promise<void> => {
        console.log('🔍 Starting fetchUserData...');

        if (!auth.token) {
            console.log('❌ No token available, user needs to login');
            auth.login();
            return;
        }

        console.log('🔑 Current token:', auth.token.substring(0, 50) + '...');
        console.log('👤 Current userId:', auth.userId);

        try {
            console.log('📡 Making API call to /user/me...');
            await auth.refreshToken(5);

            const response = await getUser(auth.token);

            console.log('✅ API call successful!', response);
            setUserData(response);
        } catch (error) {
            if (error instanceof AxiosError) {
                console.error('❌ Failed to fetch user data:', error);
                console.error('📄 Error response:', error.response?.data);
                console.error('📢 Status code:', error.response?.status);
                if (error.response?.status === 401) {
                    console.log('🚪 Unauthorized - triggering login...');
                    auth.login();
                }
            } else {
                console.error('💥 Unexpected error:', error);
            }
        }
    }, [auth]);

    const updateUgle = useCallback((updatedUgle: UserGameListEntryDTO) => {
        setUserData((prev) => {
            if (!prev || !prev.relationalData || !updatedUgle.id) return prev;
            const entries = prev.relationalData.userGameListEntries || [];
            const newEntries = entries.map((e) => (e.id === updatedUgle.id ? updatedUgle : e));
            return {
                ...prev,
                relationalData: {
                    ...prev.relationalData,
                    userGameListEntries: newEntries,
                },
            };
        });
    }, []);

    const deleteUgle = useCallback((ugleId: number) => {
        setUserData((prev) => {
            if (!prev || !prev.relationalData) return prev;
            const entries = prev.relationalData.userGameListEntries || [];
            const newEntries = entries.filter((e) => e.id !== ugleId);
            return {
                ...prev,
                relationalData: {
                    ...prev.relationalData,
                    userGameListEntries: newEntries,
                },
            };
        });
    }, []);

    const addUgle = useCallback((newUgle: UserGameListEntryDTO) => {
        setUserData((prev) => {
            if (!prev || !prev.relationalData) return prev;
            const entries = prev.relationalData.userGameListEntries || [];
            return {
                ...prev,
                relationalData: {
                    ...prev.relationalData,
                    userGameListEntries: [...entries, newUgle],
                },
            };
        });
    }, []);

    const follow = useCallback((followedId: string) => {
        setUserData((prev) => {
            if (!prev || !prev.graphData) return prev;
            const newFollowing = new Set(prev.graphData.following);
            newFollowing.add(followedId);
            return {
                ...prev,
                graphData: {
                    ...prev.graphData,
                    following: newFollowing,
                },
            };
        });
    }, []);

    const unfollow = useCallback((followedId: string) => {
        setUserData((prev) => {
            if (!prev || !prev.graphData) return prev;
            const newFollowing = new Set(prev.graphData.following);
            newFollowing.delete(followedId);
            return {
                ...prev,
                graphData: {
                    ...prev.graphData,
                    following: newFollowing,
                },
            };
        });
    }, []);

    return (
        <UserContext.Provider
            value={{ userData, fetchUserData, updateUgle, deleteUgle, addUgle, follow, unfollow }}
        >
            {children}
        </UserContext.Provider>
    );
};

export const useUser = () => {
    const context = useContext(UserContext);
    if (!context) throw new Error('useUser must be used within UserProvider');
    return context;
};