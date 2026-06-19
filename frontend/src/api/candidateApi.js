import api from './axios';

export const candidateApi = {
    getProfile: () => api.get('/api/candidate/profile'),
    updateProfile: (data) => api.put('/api/candidate/profile', data),
    deleteAccount: () => api.delete('/api/candidate/profile'),
};