import api from './axios';

export const favoriteApi = {
    add: (jobPostId) => api.post(`/api/candidate/favorites/${jobPostId}`),
    remove: (jobPostId) => api.delete(`/api/candidate/favorites/${jobPostId}`),
    getAll: () => api.get('/api/candidate/favorites'),
    checkIsFavorite: (jobPostId) => api.get(`/api/candidate/favorites/check/${jobPostId}`),
};