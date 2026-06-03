import { useQuery } from '@tanstack/react-query';
import {
    Users, Briefcase, FileText,
    Building2, ArrowRight, Clock
} from 'lucide-react';
import { Link } from 'react-router-dom';
import DashboardLayout from '../../components/layout/DashboardLayout';
import Card from '../../components/ui/Card';
import Spinner from '../../components/ui/Spinner';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import { adminApi } from '../../api/adminApi';

const navItems = [
    { to: '/admin/dashboard', icon: <Briefcase size={18} />, label: 'Dashboard' },
    { to: '/admin/users', icon: <Users size={18} />, label: 'Users' },
    { to: '/admin/posts', icon: <FileText size={18} />, label: 'Job Posts' },
];

export default function AdminDashboard() {
    const { data: statsData, isLoading: loadingStats } = useQuery({
        queryKey: ['admin-stats'],
        queryFn: adminApi.getStats,
    });

    const { data: pendingData, isLoading: loadingPending } = useQuery({
        queryKey: ['pending-employers'],
        queryFn: () => adminApi.getPendingEmployers({ page: 0, size: 5 }),
    });

    const stats = statsData?.data;
    const pendingEmployers = pendingData?.data?.content || [];

    return (
        <DashboardLayout navItems={navItems}>
            <div className="mb-8">
                <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
                <p className="text-gray-500 mt-1">
                    Platform overview and management.
                </p>
            </div>

            {/* Stats */}
            {loadingStats ? (
                <div className="flex justify-center py-8">
                    <Spinner />
                </div>
            ) : (
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
                    <Card className="p-5">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-gray-500">Candidates</p>
                                <p className="text-2xl font-bold text-gray-900 mt-1">
                                    {stats?.totalCandidates || 0}
                                </p>
                            </div>
                            <div className="bg-blue-100 p-3 rounded-lg">
                                <Users className="text-blue-600" size={22} />
                            </div>
                        </div>
                    </Card>

                    <Card className="p-5">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-gray-500">Employers</p>
                                <p className="text-2xl font-bold text-gray-900 mt-1">
                                    {stats?.totalEmployers || 0}
                                </p>
                            </div>
                            <div className="bg-purple-100 p-3 rounded-lg">
                                <Building2 className="text-purple-600" size={22} />
                            </div>
                        </div>
                    </Card>

                    <Card className="p-5">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-gray-500">Active Posts</p>
                                <p className="text-2xl font-bold text-gray-900 mt-1">
                                    {stats?.activePosts || 0}
                                </p>
                            </div>
                            <div className="bg-green-100 p-3 rounded-lg">
                                <Briefcase className="text-green-600" size={22} />
                            </div>
                        </div>
                    </Card>

                    <Card className="p-5">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-gray-500">Applications</p>
                                <p className="text-2xl font-bold text-gray-900 mt-1">
                                    {stats?.totalApplications || 0}
                                </p>
                            </div>
                            <div className="bg-amber-100 p-3 rounded-lg">
                                <FileText className="text-amber-600" size={22} />
                            </div>
                        </div>
                    </Card>
                </div>
            )}

            {/* Pending Employers */}
            <Card className="p-6">
                <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center gap-2">
                        <h2 className="text-base font-semibold text-gray-900">
                            Pending Employer Approvals
                        </h2>
                        {pendingEmployers.length > 0 && (
                            <Badge variant="warning">
                                {pendingEmployers.length}
                            </Badge>
                        )}
                    </div>
                    <Link
                        to="/admin/users"
                        className="flex items-center gap-1 text-sm text-primary-600 hover:text-primary-700"
                    >
                        Manage all <ArrowRight size={14} />
                    </Link>
                </div>

                {loadingPending ? (
                    <div className="flex justify-center py-6">
                        <Spinner />
                    </div>
                ) : pendingEmployers.length === 0 ? (
                    <p className="text-sm text-gray-500 text-center py-6">
                        No pending approvals. 🎉
                    </p>
                ) : (
                    <div className="space-y-3">
                        {pendingEmployers.map((user) => (
                            <div
                                key={user.id}
                                className="flex items-center justify-between py-3 border-b border-gray-100 last:border-0"
                            >
                                <div>
                                    <p className="text-sm font-medium text-gray-900">
                                        {user.companyName}
                                    </p>
                                    <p className="text-xs text-gray-500 mt-0.5">
                                        {user.email}
                                    </p>
                                </div>
                                <div className="flex items-center gap-2">
                                    <Clock size={14} className="text-amber-500" />
                                    <Badge variant="warning">Pending</Badge>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </Card>
        </DashboardLayout>
    );
}