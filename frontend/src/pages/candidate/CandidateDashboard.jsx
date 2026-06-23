import { useQuery } from '@tanstack/react-query';
import { FileText, Briefcase, Heart, ArrowRight, User, Search, FileBox } from 'lucide-react';
import { Link } from 'react-router-dom';
import DashboardLayout from '../../components/layout/DashboardLayout';
import Card from '../../components/ui/Card';
import Spinner from '../../components/ui/Spinner';
import Badge from '../../components/ui/Badge';
import { applicationApi } from '../../api/applicationApi';
import { resumeApi } from '../../api/resumeApi';
import { favoriteApi } from '../../api/favoriteApi';
import { formatDate, formatApplicationStatus } from '../../utils/formatters';
import useAuthStore from '../../store/authStore';

const navItems = [
    { to: '/jobs', icon: <Search size={18} />, label: 'Browse Jobs' },
    { to: '/candidate/dashboard', icon: <Briefcase size={18} />, label: 'Dashboard' },
    { to: '/candidate/applications', icon: <FileBox size={18} />, label: 'My Applications' },
    { to: '/candidate/resume', icon: <FileText size={18} />, label: 'My Resume' },
    { to: '/candidate/favorites', icon: <Heart size={18} />, label: 'Saved Jobs' },
    { to: '/candidate/profile', icon: <User size={18} />, label: 'My Profile' },
];

const statusVariant = {
    DRAFT: 'default',
    PENDING: 'warning',
    ACCEPTED: 'success',
    REJECTED: 'danger',
    WITHDRAWN: 'default',
};

export default function CandidateDashboard() {
    const { user } = useAuthStore();

    const { data: applicationsData, isLoading: loadingApps } = useQuery({
        queryKey: ['my-applications'],
        queryFn: applicationApi.getMyApplications,
    });

    const { data: resumeData } = useQuery({
        queryKey: ['my-resume'],
        queryFn: resumeApi.get,
        retry: false,
    });

    const { data: favoritesData } = useQuery({
        queryKey: ['my-favorites'],
        queryFn: favoriteApi.getAll,
    });

    const applications = applicationsData?.data || [];
    const resume = resumeData?.data;
    const favorites = favoritesData?.data || [];

    const recentApplications = applications.slice(0, 5);

    return (
        <DashboardLayout navItems={navItems}>
            <div className="mb-8">
                <h1 className="text-2xl font-bold text-gray-900">
                    Dashboard Overview
                </h1>
                <p className="text-gray-500 mt-1">
                    Here's an overview of your job search activity.
                </p>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
                <Card className="p-5">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm text-gray-500">Total Applications</p>
                            <p className="text-2xl font-bold text-gray-900 mt-1">
                                {applications.length}
                            </p>
                        </div>
                        <div className="bg-primary-100 p-3 rounded-lg">
                            <Briefcase className="text-primary-600" size={22} />
                        </div>
                    </div>
                </Card>

                <Card className="p-5">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm text-gray-500">Saved Jobs</p>
                            <p className="text-2xl font-bold text-gray-900 mt-1">
                                {favorites.length}
                            </p>
                        </div>
                        <div className="bg-red-100 p-3 rounded-lg">
                            <Heart className="text-red-500" size={22} />
                        </div>
                    </div>
                </Card>

                <Card className="p-5">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm text-gray-500">Resume Status</p>
                            <p className="text-sm font-semibold text-gray-900 mt-1">
                                {resume ? 'Complete' : 'Not created'}
                            </p>
                        </div>
                        <div className="bg-green-100 p-3 rounded-lg">
                            <FileText className="text-green-600" size={22} />
                        </div>
                    </div>
                </Card>
            </div>

            {/* Recent Applications */}
            <Card className="p-6 mb-6">
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-base font-semibold text-gray-900">
                        Recent Applications
                    </h2>
                    <Link
                        to="/candidate/applications"
                        className="flex items-center gap-1 text-sm text-primary-600 hover:text-primary-700"
                    >
                        View all <ArrowRight size={14} />
                    </Link>
                </div>

                {loadingApps ? (
                    <div className="flex justify-center py-6">
                        <Spinner />
                    </div>
                ) : recentApplications.length === 0 ? (
                    <div className="text-center py-8">
                        <p className="text-sm text-gray-500 mb-3">
                            You haven't applied to any jobs yet.
                        </p>
                        <Link
                            to="/jobs"
                            className="text-sm text-primary-600 hover:text-primary-700 font-medium"
                        >
                            Browse available jobs →
                        </Link>
                    </div>
                ) : (
                    <div className="space-y-3">
                        {recentApplications.map((app) => (
                            <div
                                key={app.id}
                                className="flex items-center justify-between py-3 border-b border-gray-100 last:border-0"
                            >
                                <div>
                                    <p className="text-sm font-medium text-gray-900">
                                        {app.jobPostTitle}
                                    </p>
                                    <p className="text-xs text-gray-500 mt-0.5">
                                        {app.companyName} · {formatDate(app.appliedAt)}
                                    </p>
                                </div>
                                <Badge variant={statusVariant[app.status]}>
                                    {formatApplicationStatus(app.status)}
                                </Badge>
                            </div>
                        ))}
                    </div>
                )}
            </Card>

            {/* Resume prompt */}
            {!resume && (
                <Card className="p-6 bg-primary-50 border-primary-200">
                    <div className="flex items-start justify-between">
                        <div>
                            <h3 className="font-semibold text-gray-900 mb-1">
                                Complete your resume
                            </h3>
                            <p className="text-sm text-gray-600">
                                A complete resume increases your chances of getting hired.
                            </p>
                        </div>
                        <Link to="/candidate/resume">
                            <button className="text-sm font-medium text-primary-600 hover:text-primary-700 whitespace-nowrap ml-4">
                                Create Resume →
                            </button>
                        </Link>
                    </div>
                </Card>
            )}
        </DashboardLayout>
    );
}