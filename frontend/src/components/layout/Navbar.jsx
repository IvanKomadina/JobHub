import { Link, useNavigate } from 'react-router-dom';
import { LogOut, User, Briefcase, LayoutDashboard } from 'lucide-react';
import useAuthStore from '../../store/authStore';
import { authApi } from '../../api/authApi';
import toast from 'react-hot-toast';
import Button from '../ui/Button';

export default function Navbar() {
    const { user, isAuthenticated, clearUser } = useAuthStore();
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            await authApi.logout();
        } catch (e) {
            // ignore
        } finally {
            clearUser();
            navigate('/');
            toast.success('Logged out successfully');
        }
    };

    const getDashboardLink = () => {
        if (!user) return '/';
        const map = {
            CANDIDATE: '/candidate/dashboard',
            EMPLOYER: '/employer/dashboard',
            ADMINISTRATOR: '/admin/dashboard',
        };
        return map[user.role] || '/';
    };

    return (
        <nav className="bg-white border-b border-gray-200 sticky top-0 z-40">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center justify-between h-16">

                    {/* Logo */}
                    <Link to="/" className="flex items-center gap-2">
                        <Briefcase className="text-primary-600" size={24} />
                        <span className="text-xl font-bold text-gray-900">JobHub</span>
                    </Link>

                    {/* Nav links */}
                    <div className="hidden md:flex items-center gap-6">
                        <Link
                            to="/jobs"
                            className="text-sm text-gray-600 hover:text-gray-900 transition-colors"
                        >
                            Browse Jobs
                        </Link>
                    </div>

                    {/* Auth */}
                    <div className="flex items-center gap-3">
                        {isAuthenticated ? (
                            <>
                                <Link
                                    to={getDashboardLink()}
                                    className="flex items-center gap-1.5 text-sm text-gray-600 hover:text-gray-900"
                                >
                                    <LayoutDashboard size={16} />
                                    Dashboard
                                </Link>
                                <div className="flex items-center gap-2 text-sm text-gray-600">
                                    <User size={16} />
                                    {user?.email}
                                </div>
                                <Button
                                    variant="ghost"
                                    size="sm"
                                    onClick={handleLogout}
                                >
                                    <LogOut size={16} className="mr-1" />
                                    Logout
                                </Button>
                            </>
                        ) : (
                            <>
                                <Link to="/login">
                                    <Button variant="secondary" size="sm">
                                        Log in
                                    </Button>
                                </Link>
                                <Link to="/register">
                                    <Button size="sm">
                                        Sign up
                                    </Button>
                                </Link>
                            </>
                        )}
                    </div>
                </div>
            </div>
        </nav>
    );
}