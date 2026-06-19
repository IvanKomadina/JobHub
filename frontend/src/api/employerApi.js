import api from './axios';

export const employerApi = {
    getProfile: () => api.get('/api/employer/profile'),
    updateProfile: (data) => api.put('/api/employer/profile', data),
    deleteAccount: () => api.delete('/api/employer/profile'),
    updateLogo: (formData) => api.patch('/api/employer/profile/logo', formData, {
                                    headers: { 'Content-Type': 'multipart/form-data' }
                                }),
};