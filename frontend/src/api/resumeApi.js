import api from './axios';

export const resumeApi = {
    create: (data) => api.post('/api/candidate/resume', data),
    get: () => api.get('/api/candidate/resume'),
    updateSummary: (data) => api.patch('/api/candidate/resume/summary', data),
    downloadPdf: () => api.get('/api/candidate/resume/pdf', { responseType: 'blob' }),

    // Education
    addEducation: (data) => api.post('/api/candidate/resume/education', data),
    updateEducation: (id, data) => api.put(`/api/candidate/resume/education/${id}`, data),
    deleteEducation: (id) => api.delete(`/api/candidate/resume/education/${id}`),

    // Experience
    addExperience: (data) => api.post('/api/candidate/resume/experience', data),
    updateExperience: (id, data) => api.put(`/api/candidate/resume/experience/${id}`, data),
    deleteExperience: (id) => api.delete(`/api/candidate/resume/experience/${id}`),

    // Skills
    addSkill: (data) => api.post('/api/candidate/resume/skills', data),
    updateSkill: (id, data) => api.put(`/api/candidate/resume/skills/${id}`, data),
    deleteSkill: (id) => api.delete(`/api/candidate/resume/skills/${id}`),

    // Languages
    addLanguage: (data) => api.post('/api/candidate/resume/languages', data),
    updateLanguage: (id, data) => api.put(`/api/candidate/resume/languages/${id}`, data),
    deleteLanguage: (id) => api.delete(`/api/candidate/resume/languages/${id}`),
};