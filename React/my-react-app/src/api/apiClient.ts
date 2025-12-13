import axios from 'axios';

const api = axios.create({
    //local
    //baseURL: 'http://localhost:8080/api',
    //online
    baseURL: 'https://quarkus.gamegrid.buzz/api',
});

// Interceptor to add token (call setAuthInterceptor in AuthProvider after login)
export const setAuthInterceptor = (getToken: () => string | null) => {
    api.interceptors.request.use((config) => {
        const token = getToken();
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    });
};

export default api;