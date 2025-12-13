// AuthContext.tsx

import React, { createContext, useState, useEffect, useContext, useCallback, useRef } from 'react';
import keycloak from '../auth/keycloak';
import { setAuthInterceptor } from '../api/apiClient';

interface AuthContextType {
    isAuthenticated: boolean;
    userId: string | null;
    token: string | null;
    roles: string[];
    isLoading: boolean;
    login: () => void;
    logout: () => void;
    refreshToken: (minValidity: number) => Promise<boolean>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

let isKeycloakInitialized = false;

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [userId, setUserId] = useState<string | null>(null);
    const [token, setToken] = useState<string | null>(null);
    const [roles, setRoles] = useState<string[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    const tokenRef = useRef(token);
    useEffect(() => {
        tokenRef.current = token;
    }, [token]);

    const updateAuthState = useCallback(() => {
        setToken(keycloak.token || null);
        setUserId(keycloak.tokenParsed?.sub || null);
        setRoles(keycloak.tokenParsed?.realm_access?.roles || []);
    }, []);

    const refreshToken = useCallback(async (minValidity: number) => {
        const refreshed = await keycloak.updateToken(minValidity);
        if (refreshed) {
            updateAuthState();
        }
        return refreshed;
    }, [updateAuthState]);

    useEffect(() => {
        if (!isKeycloakInitialized) {
            isKeycloakInitialized = true;
            keycloak
                .init({
                    onLoad: 'check-sso',
                    checkLoginIframe: false,
                    silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
                })
                .then((authenticated) => {
                    setIsAuthenticated(authenticated);
                    if (authenticated) {
                        updateAuthState();
                    }
                    setIsLoading(false);
                })
                .catch((error) => {
                    console.error('Keycloak init failed:', error);
                    isKeycloakInitialized = false;
                    setIsLoading(false);
                });
        }
    }, [updateAuthState]);

    useEffect(() => {
        let refreshInterval: NodeJS.Timeout | null = null;
        if (isAuthenticated) {
            refreshInterval = setInterval(async () => {
                try {
                    await refreshToken(30);
                } catch {
                    setIsAuthenticated(false);
                    setToken(null);
                    setUserId(null);
                    setRoles([]);
                }
            }, 60000);

            // Set the auth interceptor once authenticated
            setAuthInterceptor(() => tokenRef.current || '');
        }
        return () => {
            if (refreshInterval) {
                clearInterval(refreshInterval);
            }
        };
    }, [isAuthenticated, refreshToken]);

    const login = useCallback(() => {
        keycloak.login().then(() => {
            setIsAuthenticated(true);
            updateAuthState();
        });
    }, [updateAuthState]);

    const logout = useCallback(() => {
        keycloak.logout().then(() => {
            setIsAuthenticated(false);
            setToken(null);
            setUserId(null);
            setRoles([]);
        });
    }, []);

    if (isLoading) {
        return (
            <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
                <div className="spinner-border" role="status">
                    <span className="visually-hidden">Loading...</span>
                </div>
            </div>
        );
    }

    return (
        <AuthContext.Provider
            value={{
                isAuthenticated,
                userId,
                token,
                roles,
                isLoading,
                login,
                logout,
                refreshToken,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within AuthProvider');
    return context;
};