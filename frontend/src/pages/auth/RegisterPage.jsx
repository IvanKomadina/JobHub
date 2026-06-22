import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { Briefcase } from 'lucide-react';
import { useState } from 'react';
import { authApi } from '../../api/authApi';
import useAuthStore from '../../store/authStore';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import toast from 'react-hot-toast';

const candidateSchema = z.object({
    email: z.string().email('Invalid email address'),
    password: z.string().min(8, 'Password must be at least 8 characters'),
    confirmPassword: z.string().min(1, 'Please confirm your password'),
    role: z.literal('CANDIDATE'),
    firstName: z.string().min(1, 'First name is required'),
    lastName: z.string().min(1, 'Last name is required'),
}).refine((data) => data.password === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
});

const employerSchema = z.object({
    email: z.string().email('Invalid email address'),
    password: z.string().min(8, 'Password must be at least 8 characters'),
    confirmPassword: z.string().min(1, 'Please confirm your password'),
    role: z.literal('EMPLOYER'),
    companyName: z.string().min(1, 'Company name is required'),
}).refine((data) => data.password === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
});

export default function RegisterPage() {
    const navigate = useNavigate();
    const setUser = useAuthStore((state) => state.setUser);
    const [isLoading, setIsLoading] = useState(false);
    const [role, setRole] = useState('CANDIDATE');

    const schema = role === 'CANDIDATE' ? candidateSchema : employerSchema;

    const { register, handleSubmit, formState: { errors }, reset } = useForm({
        resolver: zodResolver(schema),
        defaultValues: { role: 'CANDIDATE' },
    });

    const handleRoleChange = (newRole) => {
        setRole(newRole);
        reset({ role: newRole });
    };

    const onSubmit = async (data) => {
        setIsLoading(true);
        const { confirmPassword, ...payload } = data;
        try {
            const response = await authApi.register({ ...payload, role });
            setUser(response.data);
            toast.success('Account created successfully!');

            if (role === 'CANDIDATE') navigate('/candidate/dashboard');
            else if (role === 'EMPLOYER') {
                toast('Your employer account is pending approval by an admin.', {
                    icon: '⏳',
                    duration: 6000,
                });
                navigate('/employer/dashboard');
            }
        } catch (error) {
            const message = error.response?.data?.message || 'Registration failed';
            toast.error(message);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
            <div className="sm:mx-auto sm:w-full sm:max-w-md">
                <div className="flex justify-center items-center gap-2 mb-6">
                    <Briefcase className="text-primary-600" size={32} />
                    <span className="text-2xl font-bold text-gray-900">JobHub</span>
                </div>
                <h2 className="text-center text-2xl font-bold text-gray-900">
                    Create your account
                </h2>
                <p className="text-center text-sm text-gray-600 mt-2">
                    Already have an account?{' '}
                    <Link to="/login" className="text-primary-600 hover:text-primary-700 font-medium">
                        Sign in
                    </Link>
                </p>
            </div>

            <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
                <div className="bg-white py-8 px-6 shadow-sm rounded-xl border border-gray-200">

                    {/* Role Toggle */}
                    <div className="flex rounded-lg border border-gray-200 p-1 mb-6">
                        <button
                            type="button"
                            onClick={() => handleRoleChange('CANDIDATE')}
                            className={`flex-1 py-2 text-sm font-medium rounded-md transition-colors ${
                                role === 'CANDIDATE'
                                    ? 'bg-primary-600 text-white'
                                    : 'text-gray-600 hover:text-gray-900'
                            }`}
                        >
                            I'm a Candidate
                        </button>
                        <button
                            type="button"
                            onClick={() => handleRoleChange('EMPLOYER')}
                            className={`flex-1 py-2 text-sm font-medium rounded-md transition-colors ${
                                role === 'EMPLOYER'
                                    ? 'bg-primary-600 text-white'
                                    : 'text-gray-600 hover:text-gray-900'
                            }`}
                        >
                            I'm an Employer
                        </button>
                    </div>

                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
                        <Input
                            label="Email address"
                            type="email"
                            placeholder="you@example.com"
                            error={errors.email?.message}
                            {...register('email')}
                        />

                        <Input
                            label="Password"
                            type="password"
                            placeholder="At least 8 characters"
                            error={errors.password?.message}
                            {...register('password')}
                        />

                        <Input
                            label="Confirm Password"
                            type="password"
                            placeholder="Repeat your password"
                            error={errors.confirmPassword?.message}
                            {...register('confirmPassword')}
                        />

                        {role === 'CANDIDATE' && (
                            <div className="grid grid-cols-2 gap-4">
                                <Input
                                    label="First name"
                                    placeholder="John"
                                    error={errors.firstName?.message}
                                    {...register('firstName')}
                                />
                                <Input
                                    label="Last name"
                                    placeholder="Doe"
                                    error={errors.lastName?.message}
                                    {...register('lastName')}
                                />
                            </div>
                        )}

                        {role === 'EMPLOYER' && (
                            <Input
                                label="Company name"
                                placeholder="Acme Corp"
                                error={errors.companyName?.message}
                                {...register('companyName')}
                            />
                        )}

                        {role === 'EMPLOYER' && (
                            <div className="rounded-lg bg-amber-50 border border-amber-200 p-3">
                                <p className="text-xs text-amber-700">
                                    ⏳ Employer accounts require admin approval before you can post jobs.
                                </p>
                            </div>
                        )}

                        <Button
                            type="submit"
                            className="w-full"
                            isLoading={isLoading}
                        >
                            Create account
                        </Button>
                    </form>
                </div>
            </div>
        </div>
    );
}