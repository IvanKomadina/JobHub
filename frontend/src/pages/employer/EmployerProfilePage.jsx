import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Briefcase, FileText, User, Trash2, Save, Building2, Upload } from 'lucide-react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Textarea from '../../components/ui/Textarea';
import Badge from '../../components/ui/Badge';
import Spinner from '../../components/ui/Spinner';
import { employerApi } from '../../api/employerApi';
import useAuthStore from '../../store/authStore';
import toast from 'react-hot-toast';

const navItems = [
    { to: '/employer/dashboard', icon: <Briefcase size={18} />, label: 'Dashboard' },
    { to: '/employer/posts', icon: <FileText size={18} />, label: 'My Posts' },
    { to: '/employer/profile', icon: <User size={18} />, label: 'Company Profile' },
];

const schema = z.object({
    companyName: z.string().min(1, 'Company name is required'),
    industry: z.string().optional(),
    website: z.string().optional(),
    city: z.string().optional(),
    country: z.string().optional(),
    description: z.string().optional(),
});

const statusVariant = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
};

export default function EmployerProfilePage() {
    const navigate = useNavigate();
    const clearUser = useAuthStore((state) => state.clearUser);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [isUploadingLogo, setIsUploadingLogo] = useState(false);
    const queryClient = useQueryClient();

    const { data, isLoading } = useQuery({
        queryKey: ['employer-profile'],
        queryFn: employerApi.getProfile,
    });

    const profile = data?.data;

    const { register, handleSubmit, formState: { errors, isDirty } } = useForm({
        resolver: zodResolver(schema),
        values: profile ? {
            companyName: profile.companyName || '',
            industry: profile.industry || '',
            website: profile.website || '',
            city: profile.city || '',
            country: profile.country || '',
            description: profile.description || '',
        } : {},
    });

    const updateMutation = useMutation({
        mutationFn: employerApi.updateProfile,
        onSuccess: () => toast.success('Company profile updated'),
        onError: (e) => toast.error(e.response?.data?.message || 'Failed to update'),
    });

    const handleLogoUpload = async (e) => {
        const file = e.target.files[0];
        if (!file) return;
        setIsUploadingLogo(true);
        try {
            const formData = new FormData();
            formData.append('file', file);
            await employerApi.updateLogo(formData);
            queryClient.invalidateQueries(['employer-profile']);
            toast.success('Logo updated');
        } catch (err) {
            toast.error('Failed to upload logo');
        } finally {
            setIsUploadingLogo(false);
        }
    };

    const handleDeleteAccount = async () => {
        setIsDeleting(true);
        try {
            await employerApi.deleteAccount();
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
                    <div className="flex items-center gap-3">
                        <h1 className="text-2xl font-bold text-gray-900">
                            Company Profile
                        </h1>
                        {profile?.status && (
                            <Badge variant={statusVariant[profile.status]}>
                                {profile.status}
                            </Badge>
                        )}
                    </div>
                    <p className="text-gray-500 mt-1">
                        Manage your company information.
                    </p>
                    {profile?.status === 'PENDING' && (
                        <div className="mt-3 bg-amber-50 border border-amber-200 rounded-lg p-3">
                            <p className="text-sm text-amber-700">
                                ⏳ Your account is pending admin approval.
                                You'll be able to post jobs once approved.
                            </p>
                        </div>
                    )}
                    {profile?.status === 'REJECTED' && (
                        <div className="mt-3 bg-red-50 border border-red-200 rounded-lg p-3">
                            <p className="text-sm text-red-700">
                                ❌ Your account was rejected. Please contact support.
                            </p>
                        </div>
                    )}
                </div>

                {/* Logo */}
                <div className="flex items-center gap-5 mb-6 pb-6 border-b border-gray-100">
                    <div className="w-20 h-20 rounded-xl border border-gray-200 flex items-center justify-center overflow-hidden bg-gray-50 flex-shrink-0">
                        {profile?.logoUrl ? (
                            <img
                                src={profile.logoUrl}
                                alt="Company logo"
                                className="w-full h-full object-cover"
                            />
                        ) : (
                            <Building2 size={32} className="text-gray-300" />
                        )}
                    </div>
                    <div>
                        <p className="text-sm font-medium text-gray-700 mb-1">
                            Company Logo
                        </p>
                        <p className="text-xs text-gray-500 mb-2">
                            PNG, JPG up to 10MB
                        </p>
                        <label className={`
                            inline-flex items-center gap-2 px-3 py-1.5 rounded-lg border border-gray-300
                            text-sm font-medium text-gray-700 bg-white hover:bg-gray-50
                            cursor-pointer transition-colors
                            ${isUploadingLogo ? 'opacity-50 cursor-not-allowed' : ''}
                        `}>
                            <Upload size={14} />
                            {isUploadingLogo ? 'Uploading...' : 'Upload Logo'}
                            <input
                                type="file"
                                accept="image/jpeg,image/png"
                                className="hidden"
                                disabled={isUploadingLogo}
                                onChange={handleLogoUpload}
                            />
                        </label>
                    </div>
                </div>

                {/* Profile form */}
                <Card className="p-6 mb-6">
                    <h2 className="text-base font-semibold text-gray-900 mb-4">
                        Company Information
                    </h2>

                    <form
                        onSubmit={handleSubmit((data) => updateMutation.mutate(data))}
                        className="space-y-4"
                    >
                        <Input
                            label="Email"
                            value={profile?.email || ''}
                            disabled
                            hint="Email cannot be changed"
                        />

                        <Input
                            label="Company Name *"
                            placeholder="Acme Corp"
                            error={errors.companyName?.message}
                            {...register('companyName')}
                        />

                        <div className="grid grid-cols-2 gap-4">
                            <Input
                                label="Industry"
                                placeholder="Information Technology"
                                {...register('industry')}
                            />
                            <Input
                                label="Website"
                                placeholder="https://yourcompany.com"
                                {...register('website')}
                            />
                        </div>

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
                            label="Description"
                            placeholder="Tell candidates about your company..."
                            rows={4}
                            {...register('description')}
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
                        Deleting your account is permanent. All your job posts
                        and received applications will be lost.
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