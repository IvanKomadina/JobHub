import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
    Briefcase, FileText, Heart,
    User, Trash2, Save, Search
} from 'lucide-react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Textarea from '../../components/ui/Textarea';
import Spinner from '../../components/ui/Spinner';
import { candidateApi } from '../../api/candidateApi';
import useAuthStore from '../../store/authStore';
import toast from 'react-hot-toast';

const navItems = [
    { to: '/jobs', icon: <Search size={18} />, label: 'Browse Jobs' },
    { to: '/candidate/dashboard', icon: <Briefcase size={18} />, label: 'Dashboard' },
    { to: '/candidate/applications', icon: <FileText size={18} />, label: 'My Applications' },
    { to: '/candidate/resume', icon: <FileText size={18} />, label: 'My Resume' },
    { to: '/candidate/favorites', icon: <Heart size={18} />, label: 'Saved Jobs' },
    { to: '/candidate/profile', icon: <User size={18} />, label: 'My Profile' },
];

const schema = z.object({
    firstName: z.string().min(1, 'First name is required'),
    lastName: z.string().min(1, 'Last name is required'),
    phone: z.string().optional(),
    city: z.string().optional(),
    country: z.string().optional(),
    bio: z.string().optional(),
});

export default function CandidateProfilePage() {
    const navigate = useNavigate();
    const clearUser = useAuthStore((state) => state.clearUser);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const queryClient = useQueryClient();

    const { data, isLoading } = useQuery({
        queryKey: ['candidate-profile'],
        queryFn: candidateApi.getProfile,
    });

    const profile = data?.data;

    const { register, handleSubmit, formState: { errors, isDirty } } = useForm({
        resolver: zodResolver(schema),
        values: profile ? {
            firstName: profile.firstName || '',
            lastName: profile.lastName || '',
            phone: profile.phone || '',
            city: profile.city || '',
            country: profile.country || '',
            bio: profile.bio || '',
        } : {},
    });

    const updateMutation = useMutation({
        mutationFn: candidateApi.updateProfile,
        onSuccess: () => toast.success('Profile updated successfully'),
        onError: (e) => toast.error(e.response?.data?.message || 'Failed to update profile'),
    });

    const handleDeleteAccount = async () => {
        setIsDeleting(true);
        try {
            await candidateApi.deleteAccount();
            clearUser();
            navigate('/');
            toast.success('Account deleted successfully');
        } catch (e) {
            toast.error('Failed to delete account');
        } finally {
            setIsDeleting(false);
            queryClient.clear();
        }
    };

    if (isLoading) {
        return (
            <DashboardLayout navItems={navItems}>
                <div className="flex justify-center py-16">
                    <Spinner />
                </div>
            </DashboardLayout>
        );
    }

    return (
        <DashboardLayout navItems={navItems}>
            <div className="max-w-2xl mx-auto">
                <div className="mb-8">
                    <h1 className="text-2xl font-bold text-gray-900">My Profile</h1>
                    <p className="text-gray-500 mt-1">
                        Manage your personal information.
                    </p>
                </div>

                {/* Profile form */}
                <Card className="p-6 mb-6">
                    <h2 className="text-base font-semibold text-gray-900 mb-4">
                        Personal Information
                    </h2>

                    <form
                        onSubmit={handleSubmit((data) => updateMutation.mutate(data))}
                        className="space-y-4"
                    >
                        {/* Email - read only */}
                        <Input
                            label="Email"
                            value={profile?.email || ''}
                            disabled
                            hint="Email cannot be changed"
                        />

                        <div className="grid grid-cols-2 gap-4">
                            <Input
                                label="First Name *"
                                placeholder="John"
                                error={errors.firstName?.message}
                                {...register('firstName')}
                            />
                            <Input
                                label="Last Name *"
                                placeholder="Doe"
                                error={errors.lastName?.message}
                                {...register('lastName')}
                            />
                        </div>

                        <Input
                            label="Phone"
                            placeholder="+385 91 234 5678"
                            {...register('phone')}
                        />

                        <div className="grid grid-cols-2 gap-4">
                            <Input
                                label="City"
                                placeholder="Zagreb"
                                {...register('city')}
                            />
                            <Input
                                label="Country"
                                placeholder="Croatia"
                                {...register('country')}
                            />
                        </div>

                        <Textarea
                            label="Bio"
                            placeholder="Tell employers a bit about yourself..."
                            rows={3}
                            {...register('bio')}
                        />

                        <Button
                            type="submit"
                            isLoading={updateMutation.isPending}
                            disabled={!isDirty}
                        >
                            <Save size={16} className="mr-2" />
                            Save Changes
                        </Button>
                    </form>
                </Card>

                {/* Danger zone */}
                <Card className="p-6 border-red-200">
                    <h2 className="text-base font-semibold text-red-600 mb-2">
                        Danger Zone
                    </h2>
                    <p className="text-sm text-gray-600 mb-4">
                        Deleting your account is permanent. All your applications,
                        resume and saved jobs will be lost.
                    </p>

                    {!showDeleteConfirm ? (
                        <Button
                            variant="danger"
                            onClick={() => setShowDeleteConfirm(true)}
                        >
                            <Trash2 size={16} className="mr-2" />
                            Delete Account
                        </Button>
                    ) : (
                        <div className="bg-red-50 rounded-lg p-4 space-y-3">
                            <p className="text-sm font-medium text-red-700">
                                Are you sure? This cannot be undone.
                            </p>
                            <div className="flex gap-3">
                                <Button
                                    variant="danger"
                                    onClick={handleDeleteAccount}
                                    isLoading={isDeleting}
                                >
                                    Yes, delete my account
                                </Button>
                                <Button
                                    variant="secondary"
                                    onClick={() => setShowDeleteConfirm(false)}
                                >
                                    Cancel
                                </Button>
                            </div>
                        </div>
                    )}
                </Card>
            </div>
        </DashboardLayout>
    );
}