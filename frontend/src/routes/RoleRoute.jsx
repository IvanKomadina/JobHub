import { Navigate, Outlet } from 'react-router-dom';
import useAuthStore from '../store/authStore';

export default function RoleRoute({ role }) {
    const user = useAuthStore((state) => state.user);
    return user?.role === role ? <Outlet /> : <Navigate to="/" replace />;
}