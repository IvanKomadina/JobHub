import api from './axios';

export const jobPostApi = {
    // Public
    getAll: (params) => api.get('/api/posts', { params }),
    getById: (id) => api.get(`/api/posts/${id}`),

    // Employer
    create: (data) => api.post('/api/employer/posts', data),
    update: (id, data) => api.put(`/api/employer/posts/${id}`, data),
    delete: (id) => api.delete(`/api/employer/posts/${id}`),
    close: (id) => api.patch(`/api/employer/posts/${id}/close`),
    getMyPosts: () => api.get('/api/employer/posts'),

    // Admin
    adminGetAll: (params) => api.get('/api/admin/posts', { params }),
    adminDelete: (id) => api.delete(`/api/admin/posts/${id}`),
};