import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    Briefcase, FileText, Plus, Edit2,
    Trash2, X, Users, Eye
} from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import DashboardLayout from '../../components/layout/DashboardLayout';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Badge from '../../components/ui/Badge';
import Modal from '../../components/ui/Modal';
import Input from '../../components/ui/Input';
import Textarea from '../../components/ui/Textarea';
import Select from '../../components/ui/Select';
import Spinner from '../../components/ui/Spinner';
import EmptyState from '../../components/ui/EmptyState';
import { jobPostApi } from '../../api/jobPostApi';
import { formatDate, formatEmploymentType } from '../../utils/formatters';
import toast from 'react-hot-toast';
import api from '../../api/axios';

const navItems = [
    { to: '/employer/dashboard', icon: <Briefcase size={18} />, label: 'Dashboard' },
    { to: '/employer/posts', icon: <FileText size={18} />, label: 'My Posts' },
];

const EMPLOYMENT_TYPES = [
    { value: 'FULL_TIME', label: 'Full Time' },
    { value: 'PART_TIME', label: 'Part Time' },
    { value: 'CONTRACT', label: 'Contract' },
    { value: 'INTERNSHIP', label: 'Internship' },
    { value: 'STUDENT', label: 'Student' },
];

const postSchema = z.object({
    title: z.string().min(1, 'Title is required'),
    description: z.string().min(1, 'Description is required'),
    requirements: z.string().optional(),
    employmentType: z.string().min(1, 'Employment type is required'),
    salaryMin: z.string().optional(),
    salaryMax: z.string().optional(),
    categoryId: z.string().optional(),
    locationId: z.string().optional(),
    closesAt: z.string().optional(),
});

const statusVariant = {
    ACTIVE: 'success',
    CLOSED: 'default',
    DELETED: 'danger',
};

export default function EmployerPostsPage() {
    const queryClient = useQueryClient();
    const [postModal, setPostModal] = useState({ open: false, data: null });

    const { data, isLoading } = useQuery({
        queryKey: ['my-posts'],
        queryFn: jobPostApi.getMyPosts,
    });

    const { data: categoriesData } = useQuery({
        queryKey: ['categories'],
        queryFn: () => api.get('/api/categories'),
    });

    const { data: locationsData } = useQuery({
        queryKey: ['locations'],
        queryFn: () => api.get('/api/locations'),
    });

    const deleteMutation = useMutation({
        mutationFn: jobPostApi.delete,
        onSuccess: () => {
            queryClient.invalidateQueries(['my-posts']);
            toast.success('Post deleted');
        },
        onError: (e) => toast.error(e.response?.data?.message || 'Failed to delete'),
    });

    const closeMutation = useMutation({
        mutationFn: jobPostApi.close,
        onSuccess: () => {
            queryClient.invalidateQueries(['my-posts']);
            toast.success('Post closed');
        },
        onError: (e) => toast.error(e.response?.data?.message || 'Failed to close'),
    });

    const posts = data?.data || [];
    const categories = categoriesData?.data?.map(c => ({
        value: String(c.id), label: c.name
    })) || [];
    const locations = locationsData?.data?.map(l => ({
        value: String(l.id), label: `${l.city}, ${l.country}`
    })) || [];

    return (
        <DashboardLayout navItems={navItems}>
            <div className="flex items-center justify-between mb-8">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">My Job Posts</h1>
                    <p className="text-gray-500 mt-1">
                        Manage your job listings.
                    </p>
                </div>
                <Button onClick={() => setPostModal({ open: true, data: null })}>
                    <Plus size={16} className="mr-2" />
                    New Post
                </Button>
            </div>

            {isLoading ? (
                <div className="flex justify-center py-16">
                    <Spinner />
                </div>
            ) : posts.length === 0 ? (
                <EmptyState
                    icon={<Briefcase size={48} />}
                    title="No job posts yet"
                    description="Create your first job post to start receiving applications."
                    action={
                        <Button onClick={() => setPostModal({ open: true, data: null })}>
                            <Plus size={16} className="mr-2" />
                            Create Post
                        </Button>
                    }
                />
            ) : (
                <div className="space-y-3">
                    {posts.map((post) => (
                        <Card key={post.id} className="p-5">
                            <div className="flex items-start justify-between gap-4">
                                <div className="flex-1">
                                    <div className="flex items-center gap-2 mb-1">
                                        <h3 className="font-semibold text-gray-900">
                                            {post.title}
                                        </h3>
                                        <Badge variant={statusVariant[post.status]}>
                                            {post.status}
                                        </Badge>
                                    </div>
                                    <p className="text-sm text-gray-500">
                                        {formatEmploymentType(post.employmentType)}
                                        {post.city && ` · ${post.city}`}
                                        {` · Posted ${formatDate(post.publishedAt)}`}
                                    </p>
                                </div>

                                <div className="flex items-center gap-2">
                                    <Link to={`/employer/posts/${post.id}/applications`}>
                                        <Button variant="secondary" size="sm">
                                            <Users size={14} className="mr-1" />
                                            Applications
                                        </Button>
                                    </Link>

                                    {post.status === 'ACTIVE' && (
                                        <>
                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                onClick={() => setPostModal({
                                                    open: true, data: post
                                                })}
                                            >
                                                <Edit2 size={14} />
                                            </Button>
                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                onClick={() => closeMutation.mutate(post.id)}
                                            >
                                                <X size={14} />
                                            </Button>
                                        </>
                                    )}

                                    <Button
                                        variant="ghost"
                                        size="sm"
                                        onClick={() => deleteMutation.mutate(post.id)}
                                    >
                                        <Trash2 size={14} className="text-red-400" />
                                    </Button>
                                </div>
                            </div>
                        </Card>
                    ))}
                </div>
            )}

            <JobPostModal
                isOpen={postModal.open}
                data={postModal.data}
                categories={categories}
                locations={locations}
                onClose={() => setPostModal({ open: false, data: null })}
                onSave={() => queryClient.invalidateQueries(['my-posts'])}
            />
        </DashboardLayout>
    );
}

// ==================== JOB POST MODAL ====================

function JobPostModal({ isOpen, data, categories, locations, onClose, onSave }) {
    const { register, handleSubmit, reset, formState: { errors } } = useForm({
        resolver: zodResolver(postSchema),
        defaultValues: data ? {
            ...data,
            categoryId: data.categoryId ? String(data.categoryId) : '',
            locationId: data.locationId ? String(data.locationId) : '',
            salaryMin: data.salaryMin ? String(data.salaryMin) : '',
            salaryMax: data.salaryMax ? String(data.salaryMax) : '',
            closesAt: data.closesAt ? data.closesAt.split('T')[0] : '',
        } : {},
    });

    const onSubmit = async (formData) => {
        const payload = {
            ...formData,
            categoryId: formData.categoryId ? Number(formData.categoryId) : null,
            locationId: formData.locationId ? Number(formData.locationId) : null,
            salaryMin: formData.salaryMin ? Number(formData.salaryMin) : null,
            salaryMax: formData.salaryMax ? Number(formData.salaryMax) : null,
            closesAt: formData.closesAt ? `${formData.closesAt}T00:00:00` : null,
        };

        try {
            if (data) {
                await jobPostApi.update(data.id, payload);
                toast.success('Post updated');
            } else {
                await jobPostApi.create(payload);
                toast.success('Post created');
            }
            onSave();
            onClose();
            reset();
        } catch (e) {
            toast.error(e.response?.data?.message || 'Failed to save post');
        }
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title={data ? 'Edit Job Post' : 'Create Job Post'}
            size="lg"
            footer={
                <>
                    <Button variant="secondary" onClick={onClose}>Cancel</Button>
                    <Button onClick={handleSubmit(onSubmit)}>
                        {data ? 'Update Post' : 'Create Post'}
                    </Button>
                </>
            }
        >
            <div className="space-y-4">
                <Input
                    label="Job Title *"
                    placeholder="e.g. Senior Java Developer"
                    error={errors.title?.message}
                    {...register('title')}
                />

                <Textarea
                    label="Description *"
                    placeholder="Describe the role and responsibilities..."
                    rows={4}
                    error={errors.description?.message}
                    {...register('description')}
                />

                <Textarea
                    label="Requirements"
                    placeholder="List required skills and experience..."
                    rows={3}
                    {...register('requirements')}
                />

                <div className="grid grid-cols-2 gap-4">
                    <Select
                        label="Employment Type *"
                        options={EMPLOYMENT_TYPES}
                        error={errors.employmentType?.message}
                        {...register('employmentType')}
                    />
                    <Select
                        label="Category"
                        options={categories}
                        placeholder="Select category"
                        {...register('categoryId')}
                    />
                </div>

                <div className="grid grid-cols-2 gap-4">
                    <Select
                        label="Location"
                        options={locations}
                        placeholder="Select location"
                        {...register('locationId')}
                    />
                    <Input
                        label="Closing Date"
                        type="date"
                        {...register('closesAt')}
                    />
                </div>

                <div className="grid grid-cols-2 gap-4">
                    <Input
                        label="Min Salary (€)"
                        type="number"
                        placeholder="3000"
                        {...register('salaryMin')}
                    />
                    <Input
                        label="Max Salary (€)"
                        type="number"
                        placeholder="5000"
                        {...register('salaryMax')}
                    />
                </div>
            </div>
        </Modal>
    );
}