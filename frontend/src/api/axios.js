import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080',
    withCredentials: true, // sends cookies with every request
    headers: {
        'Content-Type': 'application/json',
    },
});

// Response interceptor - handle token refresh
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // If 401 and we haven't already tried to refresh
        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                await axios.post(
                    'http://localhost:8080/api/auth/refresh',
                    {},
                    { withCredentials: true }
                );
                // Retry the original request
                return api(originalRequest);
            } catch (refreshError) {
                // Refresh failed - clear auth state and redirect to login
                window.dispatchEvent(new CustomEvent('auth:logout'));
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

export default api;