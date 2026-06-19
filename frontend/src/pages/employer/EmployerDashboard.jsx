import { useQuery } from '@tanstack/react-query';
import { Briefcase, FileText, Users, Plus, ArrowRight, User } from 'lucide-react';
import { Link } from 'react-router-dom';
import DashboardLayout from '../../components/layout/DashboardLayout';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import { jobPostApi } from '../../api/jobPostApi';
import { employerApi } from '../../api/employerApi';
import { formatDate } from '../../utils/formatters';
import useAuthStore from '../../store/authStore';

const navItems = [
    { to: '/employer/dashboard', icon: <Briefcase size={18} />, label: 'Dashboard' },
    { to: '/employer/posts', icon: <FileText size={18} />, label: 'My Posts' },
    { to: '/employer/profile', icon: <User size={18} />, label: 'Company Profile' },
];

const statusVariant = {
    ACTIVE: 'success',
    CLOSED: 'default',
    DELETED: 'danger',
};

export default function EmployerDashboard() {
    const { user } = useAuthStore();

    const { data, isLoading } = useQuery({
        queryKey: ['my-posts'],
        queryFn: jobPostApi.getMyPosts,
    });

    const { data: profileData } = useQuery({
        queryKey: ['employer-profile'],
        queryFn: employerApi.getProfile,
    });

    const posts = data?.data || [];
    const profile = profileData?.data;
    const activePosts = posts.filter(p => p.status === 'ACTIVE');
    const recentPosts = posts.slice(0, 5);

    return (
        <DashboardLayout navItems={navItems}>
            <div className="mb-8">
                <h1 className="text-2xl font-bold text-gray-900">
                    Welcome back! 👋
                </h1>
                <p className="text-gray-500 mt-1">
                    Manage your job posts and review applications.
                </p>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
                <Card className="p-5">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm text-gray-500">Total Posts</p>
                            <p className="text-2xl font-bold text-gray-900 mt-1">
                                {posts.length}
                            </p>
                        </div>
                        <div className="bg-primary-100 p-3 rounded-lg">
                            <FileText className="text-primary-600" size={22} />
                        </div>
                    </div>
                </Card>

                <Card className="p-5">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm text-gray-500">Active Posts</p>
                            <p className="text-2xl font-bold text-gray-900 mt-1">
                                {activePosts.length}
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
                            <p className="text-sm text-gray-500">Account Status</p>
                            <p className="text-sm font-semibold text-gray-900 mt-1">
                                {profile?.status || 'Loading...'}
                            </p>
                        </div>
                        <div className={`p-3 rounded-lg ${
                            profile?.status === 'APPROVED' ? 'bg-green-100' :
                            profile?.status === 'REJECTED' ? 'bg-red-100' :
                            'bg-amber-100'
                        }`}>
                            <Users className={`${
                                profile?.status === 'APPROVED' ? 'text-green-600' :
                                profile?.status === 'REJECTED' ? 'text-red-600' :
                                'text-amber-600'
                            }`} size={22} />
                        </div>
                    </div>
                </Card>
            </div>

            {/* Recent Posts */}
            <Card className="p-6">
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-base font-semibold text-gray-900">
                        Recent Posts
                    </h2>
                    <Link
                        to="/employer/posts"
                        className="flex items-center gap-1 text-sm text-primary-600 hover:text-primary-700"
                    >
                        View all <ArrowRight size={14} />
                    </Link>
                </div>

                {isLoading ? (
                    <div className="flex justify-center py-6">
                        <Spinner />
                    </div>
                ) : recentPosts.length === 0 ? (
                    <div className="text-center py-8">
                        <p className="text-sm text-gray-500 mb-3">
                            No job posts yet.
                        </p>
                        <Link to="/employer/posts">
                            <Button size="sm">
                                <Plus size={14} className="mr-1" />
                                Create your first post
                            </Button>
                        </Link>
                    </div>
                ) : (
                    <div className="space-y-3">
                        {recentPosts.map((post) => (
                            <div
                                key={post.id}
                                className="flex items-center justify-between py-3 border-b border-gray-100 last:border-0"
                            >
                                <div>
                                    <p className="text-sm font-medium text-gray-900">
                                        {post.title}
                                    </p>
                                    <p className="text-xs text-gray-500 mt-0.5">
                                        Posted {formatDate(post.publishedAt)}
                                    </p>
                                </div>
                                <div className="flex items-center gap-3">
                                    <Link
                                        to={`/employer/posts/${post.id}/applications`}
                                        className="text-xs text-primary-600 hover:text-primary-700"
                                    >
                                        View Applications
                                    </Link>
                                    <Badge variant={statusVariant[post.status]}>
                                        {post.status}
                                    </Badge>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </Card>
        </DashboardLayout>
    );
}