import api from './axios';

export const adminApi = {
    getStats: () => api.get('/api/admin/stats'),
    getUsers: (params) => api.get('/api/admin/users', { params }),
    getUsersByRole: (role, params) =>
        api.get(`/api/admin/users/role/${role}`, { params }),
    deleteUser: (id) => api.delete(`/api/admin/users/${id}`),
    deactivateUser: (id) => api.patch(`/api/admin/users/${id}/deactivate`),
    activateUser: (id) => api.patch(`/api/admin/users/${id}/activate`),
    getPendingEmployers: (params) =>
        api.get('/api/admin/employers/pending', { params }),
    updateEmployerStatus: (id, data) =>
        api.patch(`/api/admin/employers/${id}/status`, data),
};