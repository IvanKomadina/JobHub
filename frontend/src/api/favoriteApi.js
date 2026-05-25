import api from './axios';

export const favoriteApi = {
    add: (jobPostId) => api.post(`/api/candidate/favorites/${jobPostId}`),
    remove: (favoriteId) => api.delete(`/api/candidate/favorites/${favoriteId}`),
    getAll: () => api.get('/api/candidate/favorites'),
    checkIsFavorite: (jobPostId) => api.get(`/api/candidate/favorites/check/${jobPostId}`),
};