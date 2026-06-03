import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Users, Briefcase, FileText, Trash2, Search } from 'lucide-react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Badge from '../../components/ui/Badge';
import Select from '../../components/ui/Select';
import Spinner from '../../components/ui/Spinner';
import Pagination from '../../components/ui/Pagination';
import { jobPostApi } from '../../api/jobPostApi';
import { formatDate, formatEmploymentType } from '../../utils/formatters';
import toast from 'react-hot-toast';

const navItems = [
    { to: '/admin/dashboard', icon: <Briefcase size={18} />, label: 'Dashboard' },
    { to: '/admin/users', icon: <Users size={18} />, label: 'Users' },
    { to: '/admin/posts', icon: <FileText size={18} />, label: 'Job Posts' },
];

const statusVariant = {
    ACTIVE: 'success',
    CLOSED: 'default',
    DELETED: 'danger',
};

const STATUS_OPTIONS = [
    { value: '', label: 'All Statuses' },
    { value: 'ACTIVE', label: 'Active' },
    { value: 'CLOSED', label: 'Closed' },
    { value: 'DELETED', label: 'Deleted' },
];

export default function AdminPostsPage() {
    const queryClient = useQueryClient();
    const [page, setPage] = useState(0);
    const [statusFilter, setStatusFilter] = useState('');

    const { data, isLoading } = useQuery({
        queryKey: ['admin-posts', page, statusFilter],
        queryFn: () => jobPostApi.adminGetAll({
            page,
            size: 10,
            postStatus: statusFilter || undefined,
        }),
    });

    const deleteMutation = useMutation({
        mutationFn: jobPostApi.adminDelete,
        onSuccess: () => {
            queryClient.invalidateQueries(['admin-posts']);
            toast.success('Post deleted');
        },
        onError: (e) => toast.error(e.response?.data?.message || 'Failed to delete'),
    });

    const posts = data?.data?.content || [];
    const totalPages = data?.data?.totalPages || 0;

    return (
        <DashboardLayout navItems={navItems}>
            <div className="mb-8">
                <h1 className="text-2xl font-bold text-gray-900">Job Posts</h1>
                <p className="text-gray-500 mt-1">
                    Manage all job posts on the platform.
                </p>
            </div>

            {/* Filters */}
            <div className="flex gap-3 mb-4">
                <Select
                    options={STATUS_OPTIONS.slice(1)}
                    placeholder="All Statuses"
                    value={statusFilter}
                    onChange={(e) => {
                        setStatusFilter(e.target.value);
                        setPage(0);
                    }}
                    className="w-48"
                />
            </div>

            {/* Table */}
            <Card>
                {isLoading ? (
                    <div className="flex justify-center py-16">
                        <Spinner />
                    </div>
                ) : posts.length === 0 ? (
                    <div className="text-center py-16 text-gray-500">
                        No posts found.
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead>
                                <tr className="border-b border-gray-200">
                                    <th className="text-left text-xs font-medium text-gray-500 uppercase tracking-wide px-6 py-3">
                                        Job Post
                                    </th>
                                    <th className="text-left text-xs font-medium text-gray-500 uppercase tracking-wide px-6 py-3">
                                        Company
                                    </th>
                                    <th className="text-left text-xs font-medium text-gray-500 uppercase tracking-wide px-6 py-3">
                                        Type
                                    </th>
                                    <th className="text-left text-xs font-medium text-gray-500 uppercase tracking-wide px-6 py-3">
                                        Status
                                    </th>
                                    <th className="text-left text-xs font-medium text-gray-500 uppercase tracking-wide px-6 py-3">
                                        Published
                                    </th>
                                    <th className="text-right text-xs font-medium text-gray-500 uppercase tracking-wide px-6 py-3">
                                        Actions
                                    </th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-100">
                                {posts.map((post) => (
                                    <tr key={post.id} className="hover:bg-gray-50">
                                        <td className="px-6 py-4">
                                            <p className="text-sm font-medium text-gray-900">
                                                {post.title}
                                            </p>
                                            {post.city && (
                                                <p className="text-xs text-gray-500 mt-0.5">
                                                    {post.city}, {post.country}
                                                </p>
                                            )}
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className="text-sm text-gray-600">
                                                {post.companyName}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className="text-sm text-gray-600">
                                                {formatEmploymentType(post.employmentType)}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4">
                                            <Badge variant={statusVariant[post.status]}>
                                                {post.status}
                                            </Badge>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className="text-sm text-gray-500">
                                                {formatDate(post.publishedAt)}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="flex justify-end">
                                                {post.status !== 'DELETED' && (
                                                    <Button
                                                        variant="ghost"
                                                        size="sm"
                                                        onClick={() => {
                                                            if (confirm('Delete this post?')) {
                                                                deleteMutation.mutate(post.id);
                                                            }
                                                        }}
                                                    >
                                                        <Trash2 size={14} className="text-red-400" />
                                                    </Button>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </Card>

            <Pagination
                page={page}
                totalPages={totalPages}
                onPageChange={setPage}
            />
        </DashboardLayout>
    );
}