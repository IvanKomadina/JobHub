import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const useAuthStore = create(
    persist(
        (set) => ({
            user: null,
            isAuthenticated: false,

            setUser: (user) => set({
                user,
                isAuthenticated: !!user
            }),

            clearUser: () => set({
                user: null,
                isAuthenticated: false
            }),
        }),
        {
            name: 'auth-storage',
            // Only persist non-sensitive data
            // Tokens are in HttpOnly cookies, not here
        }
    )
);

export default useAuthStore;