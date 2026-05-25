import api from './axios';

export const authApi = {
    register: (data) => api.post('/api/auth/register', data),
    login: (data) => api.post('/api/auth/login', data),
    refresh: () => api.post('/api/auth/refresh'),
    logout: () => api.post('/api/auth/logout'),
};