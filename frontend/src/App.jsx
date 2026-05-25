import { useEffect } from 'react';
import AppRoutes from './routes/AppRoutes';
import useAuthStore from './store/authStore';

function App() {
    const clearUser = useAuthStore((state) => state.clearUser);

    // Listen for auth:logout event from axios interceptor
    useEffect(() => {
        const handleLogout = () => clearUser();
        window.addEventListener('auth:logout', handleLogout);
        return () => window.removeEventListener('auth:logout', handleLogout);
    }, [clearUser]);

    return <AppRoutes />;
}

export default App;