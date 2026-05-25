import api from './axios';

export const applicationApi = {
    // Candidate
    createDraft: (jobPostId, data) =>
        api.post(`/api/candidate/posts/${jobPostId}/draft`, data),
    uploadDocument: (applicationId, formData) =>
        api.post(`/api/candidate/applications/${applicationId}/documents`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        }),
    deleteDocument: (applicationId, documentId) =>
        api.delete(`/api/candidate/applications/${applicationId}/documents/${documentId}`),
    getDocuments: (applicationId) =>
        api.get(`/api/candidate/applications/${applicationId}/documents`),
    submit: (applicationId) =>
        api.post(`/api/candidate/applications/${applicationId}/submit`),
    withdraw: (applicationId) =>
        api.patch(`/api/candidate/applications/${applicationId}/withdraw`),
    getMyApplications: () =>
        api.get('/api/candidate/applications'),

    // Employer
    getForPost: (jobPostId) =>
        api.get(`/api/employer/posts/${jobPostId}/applications`),
    updateStatus: (applicationId, data) =>
        api.patch(`/api/employer/applications/${applicationId}/status`, data),
    getDocumentsEmployer: (applicationId) =>
        api.get(`/api/employer/applications/${applicationId}/documents`),
    generateAssessment: (applicationId) =>
        api.post(`/api/employer/applications/${applicationId}/assessment/generate`),
    getAssessment: (applicationId) =>
        api.get(`/api/employer/applications/${applicationId}/assessment`),
    updateAssessmentNotes: (applicationId, data) =>
        api.patch(`/api/employer/applications/${applicationId}/assessment/notes`, data),
};