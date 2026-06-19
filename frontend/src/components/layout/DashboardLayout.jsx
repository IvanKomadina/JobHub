import { Link, NavLink, useNavigate } from 'react-router-dom';
import { LogOut, Briefcase } from 'lucide-react';
import useAuthStore from '../../store/authStore';
import { authApi } from '../../api/authApi';
import toast from 'react-hot-toast';
import { useQueryClient } from '@tanstack/react-query';

export default function DashboardLayout({ children, navItems }) {
    const { user, clearUser } = useAuthStore();
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const handleLogout = async () => {
        try {
            await authApi.logout();
        } catch (e) {
            // ignore
        } finally {
            queryClient.clear();
            clearUser();
            navigate('/');
            toast.success('Logged out successfully');
        }
    };

    return (
        <div className="min-h-screen flex bg-gray-50">
            {/* Sidebar */}
            <aside className="w-64 bg-white border-r border-gray-200 flex flex-col fixed h-full">
                {/* Logo */}
                <div className="flex items-center gap-2 px-6 py-5 border-b border-gray-200">
                    <Briefcase className="text-primary-600" size={22} />
                    <span className="text-lg font-bold text-gray-900">JobHub</span>
                </div>

                {/* User info */}
                <div className="px-6 py-4 border-b border-gray-200">
                    <p className="text-xs text-gray-500 uppercase font-medium tracking-wide mb-1">
                        {user?.role}
                    </p>
                    <p className="text-sm font-medium text-gray-900 truncate">
                        {user?.email}
                    </p>
                </div>

                {/* Nav items */}
                <nav className="flex-1 px-3 py-4 space-y-1">
                    {navItems.map((item) => (
                        <NavLink
                            key={item.to}
                            to={item.to}
                            className={({ isActive }) => `
                                flex items-center gap-3 px-3 py-2 rounded-lg text-sm
                                transition-colors font-medium
                                ${isActive
                                    ? 'bg-primary-50 text-primary-700'
                                    : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                                }
                            `}
                        >
                            {item.icon}
                            {item.label}
                        </NavLink>
                    ))}
                </nav>

                {/* Logout */}
                <div className="px-3 py-4 border-t border-gray-200">
                    <button
                        onClick={handleLogout}
                        className="flex items-center gap-3 px-3 py-2 rounded-lg text-sm text-gray-600 hover:bg-gray-50 hover:text-gray-900 transition-colors w-full font-medium"
                    >
                        <LogOut size={18} />
                        Log out
                    </button>
                </div>
            </aside>

            {/* Main content */}
            <div className="flex-1 ml-64">
                <div className="max-w-6xl mx-auto px-8 py-8">
                    {children}
                </div>
            </div>
        </div>
    );
}