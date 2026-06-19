import { useEffect } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import AppRoutes from './routes/AppRoutes';
import useAuthStore from './store/authStore';

function App() {
    const clearUser = useAuthStore((state) => state.clearUser);
    const queryClient = useQueryClient();

    // Listen for auth:logout event from axios interceptor
    useEffect(() => {
        const handleLogout = () => {
            clearUser();
            queryClient.clear(); // clears ALL cached queries
        };
        window.addEventListener('auth:logout', handleLogout);
        return () => window.removeEventListener('auth:logout', handleLogout);
    }, [clearUser, queryClient]);

    return <AppRoutes />;
}

export default App;