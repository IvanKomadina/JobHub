import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    Users, Briefcase, FileText,
    Search, Check, X, Trash2,
    UserX, UserCheck
} from 'lucide-react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Badge from '../../components/ui/Badge';
import Input from '../../components/ui/Input';
import Select from '../../components/ui/Select';
import Spinner from '../../components/ui/Spinner';
import Pagination from '../../components/ui/Pagination';
import { adminApi } from '../../api/adminApi';
import { formatDate } from '../../utils/formatters';
import toast from 'react-hot-toast';
import Modal from '../../components/ui/Modal';

const navItems = [
    { to: '/admin/dashboard', icon: <Briefcase size={18} />, label: 'Dashboard' },
    { to: '/admin/users', icon: <Users size={18} />, label: 'Users' },
    { to: '/admin/posts', icon: <FileText size={18} />, label: 'Job Posts' },
];

const ROLE_OPTIONS = [
    { value: '', label: 'All Roles' },
    { value: 'CANDIDATE', label: 'Candidates' },
    { value: 'EMPLOYER', label: 'Employers' },
    { value: 'ADMINISTRATOR', label: 'Admins' },
];

const roleVariant = {
    CANDIDATE: 'info',
    EMPLOYER: 'primary',
    ADMINISTRATOR: 'danger',
};

const employerStatusVariant = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
};

export default function AdminUsersPage() {
    const queryClient = useQueryClient();
    const [page, setPage] = useState(0);
    const [role, setRole] = useState('');
    const [activeTab, setActiveTab] = useState('all'); // all | pending
    const [deleteUserId, setDeleteUserId] = useState(null);

    const { data, isLoading } = useQuery({
        queryKey: ['admin-users', role, page],
        queryFn: () => role
            ? adminApi.getUsersByRole(role, { page, size: 10 })
            : adminApi.getUsers({ page, size: 10 }),
    });

    const { data: pendingData, isLoading: loadingPending } = useQuery({
        queryKey: ['pending-employers', page],
        queryFn: () => adminApi.getPendingEmployers({ page, size: 10 }),
        enabled: activeTab === 'pending',
    });

    const deleteMutation = useMutation({
        mutationFn: adminApi.deleteUser,
        onSuccess: () => {
            queryClient.invalidateQueries(['admin-users']);
            toast.success('User deleted');
        },
        onError: (e) => toast.error(e.response?.data?.message || 'Failed to delete'),
    });

    const deactivateMutation = useMutation({
        mutationFn: adminApi.deactivateUser,
        onSuccess: () => {
            queryClient.invalidateQueries(['admin-users']);
            toast.success('User deactivated');
        },
    });

    const activateMutation = useMutation({
        mutationFn: adminApi.activateUser,
        onSuccess: () => {
            queryClient.invalidateQueries(['admin-users']);
            toast.success('User activated');
        },
    });

    const employerStatusMutation = useMutation({
        mutationFn: ({ id, status }) => adminApi.updateEmployerStatus(id, { status }),
        onSuccess: () => {
            queryClient.invalidateQueries(['admin-users']);
            queryClient.invalidateQueries(['pending-employers']);
            toast.success('Employer status updated');
        },
        onError: (e) => toast.error(e.response?.data?.message || 'Failed to update'),
    });

    const users = data?.data?.content || [];
    const totalPages = data?.data?.totalPages || 0;
    const pendingUsers = pendingData?.data?.content || [];
    const pendingTotalPages = pendingData?.data?.totalPages || 0;

    const displayUsers = activeTab === 'pending' ? pendingUsers : users;
    const displayTotalPages = activeTab === 'pending' ? pendingTotalPages : totalPages;
    const displayLoading = activeTab === 'pending' ? loadingPending : isLoading;

    return (
        <DashboardLayout navItems={navItems}>
            <div className="mb-8">
                <h1 className="text-2xl font-bold text-gray-900">Users</h1>
                <p className="text-gray-500 mt-1">
                    Manage all registered users.
                </p>
            </div>

            {/* Tabs */}
            <div className="flex gap-2 mb-6">
                <button
                    onClick={() => { setActiveTab('all'); setPage(0); }}
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                        activeTab === 'all'
                            ? 'bg-primary-600 text-white'
                            : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'
                    }`}
                >
                    All Users
                </button>
                <button
                    onClick={() => { setActiveTab('pending'); setPage(0); }}
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                        activeTab === 'pending'
                            ? 'bg-primary-600 text-white'
                            : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'
                    }`}
                >
                    Pending Employers
                </button>
            </div>

            {/* Filters - only for all tab */}
            {activeTab === 'all' && (
                <div className="flex gap-3 mb-4">
                    <Select
                        options={ROLE_OPTIONS.slice(1)}
                        placeholder="All Roles"
                        value={role}
                        onChange={(e) => {
                            setRole(e.target.value);
                            setPage(0);
                        }}
                        className="w-48"
                    />
                </div>
            )}

            {/* Table */}
            <Card>
                {displayLoading ? (
                    <div className="flex justify-center py-16">
                        <Spinner />
                    </div>
                ) : displayUsers.length === 0 ? (
                    <div className="text-center py-16 text-gray-500">
                        No users found.
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead>
                                <tr className="border-b border-gray-200">
                                    <th className="text-left text-xs font-medium text-gray-500 uppercase tracking-wide px-6 py-3">
                                        User
                                    </th>
                                    <th className="text-center text-xs font-medium text-gray-500 uppercase tracking-wide px-6 py-3">
                                        Role
                                    </th>
                                    <th className="text-center text-xs font-medium text-gray-500 uppercase tracking-wide px-6 py-3">
                                        Status
                                    </th>
                                    <th className="text-center text-xs font-medium text-gray-500 uppercase tracking-wide px-6 py-3">
                                        Joined
                                    </th>
                                    <th className="text-center text-xs font-medium text-gray-500 uppercase tracking-wide px-6 py-3">
                                        Actions
                                    </th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-100">
                                {displayUsers.map((user) => (
                                    <tr key={user.id} className="hover:bg-gray-50">
                                        <td className="px-6 py-4">
                                            <p className="text-sm font-medium text-gray-900">
                                                {user.role === 'CANDIDATE'
                                                    ? `${user.firstName} ${user.lastName}`
                                                    : user.companyName || user.email
                                                }
                                            </p>
                                            <p className="text-xs text-gray-500 mt-0.5">
                                                {user.email}
                                            </p>
                                        </td>
                                        <td className="px-6 py-4 text-center">
                                            <Badge variant={roleVariant[user.role]}>
                                                {user.role}
                                            </Badge>
                                        </td>
                                        <td className="px-6 py-4 text-center">
                                            <div className="flex flex-col items-center gap-1">
                                                <Badge variant={user.active ? 'success' : 'danger'}>
                                                    {user.active ? 'Active' : 'Inactive'}
                                                </Badge>
                                                {user.role === 'EMPLOYER' && user.employerStatus && (
                                                    <Badge variant={employerStatusVariant[user.employerStatus]}>
                                                        {user.employerStatus}
                                                    </Badge>
                                                )}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 text-center">
                                            <span className="text-sm text-gray-500">
                                                {formatDate(user.createdAt)}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-center">
                                            <div className="flex items-center justify-center gap-2">
                                                {/* Employer approval */}
                                                {user.role === 'EMPLOYER' &&
                                                 user.employerStatus === 'PENDING' && (
                                                    <>
                                                        <Button
                                                            variant="success"
                                                            size="sm"
                                                            onClick={() => employerStatusMutation.mutate({
                                                                id: user.employerId,
                                                                status: 'APPROVED'
                                                            })}
                                                        >
                                                            <Check size={14} className="mr-1" />
                                                            Approve
                                                        </Button>
                                                        <Button
                                                            variant="danger"
                                                            size="sm"
                                                            onClick={() => employerStatusMutation.mutate({
                                                                id: user.employerId,
                                                                status: 'REJECTED'
                                                            })}
                                                        >
                                                            <X size={14} className="mr-1" />
                                                            Reject
                                                        </Button>
                                                    </>
                                                )}

                                                {/* Activate/Deactivate */}
                                                {user.role !== 'ADMINISTRATOR' && (
                                                    user.active ? (
                                                        <Button
                                                            variant="ghost"
                                                            size="sm"
                                                            onClick={() => deactivateMutation.mutate(user.id)}
                                                        >
                                                            <UserX size={14} className="text-amber-500" />
                                                        </Button>
                                                    ) : (
                                                        <Button
                                                            variant="ghost"
                                                            size="sm"
                                                            onClick={() => activateMutation.mutate(user.id)}
                                                        >
                                                            <UserCheck size={14} className="text-green-500" />
                                                        </Button>
                                                    )
                                                )}

                                                {/* Delete */}
                                                {user.role !== 'ADMINISTRATOR' && (
                                                    <Button
                                                        variant="ghost"
                                                        size="sm"
                                                        onClick={() => {setDeleteUserId(user.id)}}
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
                totalPages={displayTotalPages}
                onPageChange={setPage}
            />

            <Modal
                isOpen={!!deleteUserId}
                onClose={() => setDeleteUserId(null)}
                title="Delete User"
                size="sm"
            >
                <p className="text-sm text-gray-600">
                    Are you sure you want to delete this user? This action cannot be undone.
                </p>

                <div className="flex gap-3 mt-6">
                    <Button
                        variant="secondary"
                        className="flex-1"
                        onClick={() => setDeleteUserId(null)}
                    >
                        Cancel
                    </Button>

                    <Button
                        variant="danger"
                        className="flex-1"
                        onClick={() => {
                            deleteMutation.mutate(deleteUserId);
                            setDeleteUserId(null);
                        }}
                    >
                        Delete
                    </Button>
                </div>
            </Modal>
        </DashboardLayout>
    );
}