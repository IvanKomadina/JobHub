import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { Briefcase } from 'lucide-react';
import { authApi } from '../../api/authApi';
import useAuthStore from '../../store/authStore';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import toast from 'react-hot-toast';
import { useState } from 'react';

const schema = z.object({
    email: z.string().email('Invalid email address'),
    password: z.string().min(1, 'Password is required'),
});

export default function LoginPage() {
    const navigate = useNavigate();
    const setUser = useAuthStore((state) => state.setUser);
    const [isLoading, setIsLoading] = useState(false);

    const { register, handleSubmit, formState: { errors } } = useForm({
        resolver: zodResolver(schema),
    });

    const onSubmit = async (data) => {
        setIsLoading(true);
        try {
            const response = await authApi.login(data);
            setUser(response.data);
            toast.success('Welcome back!');

            const role = response.data.role;
            if (role === 'CANDIDATE') navigate('/candidate/dashboard');
            else if (role === 'EMPLOYER') navigate('/employer/dashboard');
            else if (role === 'ADMINISTRATOR') navigate('/admin/dashboard');
            else navigate('/');
        } catch (error) {
            const message = error.response?.data?.message || 'Invalid email or password';
            toast.error(message);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
            <div className="sm:mx-auto sm:w-full sm:max-w-md">
                {/* Logo */}
                <div className="flex justify-center items-center gap-2 mb-6">
                    <Briefcase className="text-primary-600" size={32} />
                    <span className="text-2xl font-bold text-gray-900">JobHub</span>
                </div>
                <h2 className="text-center text-2xl font-bold text-gray-900">
                    Log in to your account
                </h2>
                <p className="text-center text-sm text-gray-600 mt-2">
                    Don't have an account?{' '}
                    <Link to="/register" className="text-primary-600 hover:text-primary-700 font-medium">
                        Sign up
                    </Link>
                </p>
            </div>

            <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
                <div className="bg-white py-8 px-6 shadow-sm rounded-xl border border-gray-200">
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
                            placeholder="••••••••"
                            error={errors.password?.message}
                            {...register('password')}
                        />
                        <Button
                            type="submit"
                            className="w-full"
                            isLoading={isLoading}
                        >
                            Sign in
                        </Button>
                    </form>
                </div>
            </div>
        </div>
    );
}