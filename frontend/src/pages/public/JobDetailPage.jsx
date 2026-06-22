import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import {
    MapPin, Clock, DollarSign, Building2,
    Briefcase, ArrowLeft, Heart, Share2
} from 'lucide-react';
import PageLayout from '../../components/layout/PageLayout';
import Button from '../../components/ui/Button';
import Badge from '../../components/ui/Badge';
import Spinner from '../../components/ui/Spinner';
import { jobPostApi } from '../../api/jobPostApi';
import { favoriteApi } from '../../api/favoriteApi';
import { formatSalary, formatDate, formatEmploymentType } from '../../utils/formatters';
import useAuthStore from '../../store/authStore';
import { useState } from 'react';
import toast from 'react-hot-toast';

export default function JobDetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user, isAuthenticated } = useAuthStore();
    const [isFavoriting, setIsFavoriting] = useState(false);
    const queryClient = useQueryClient();

    const { data, isLoading } = useQuery({
        queryKey: ['job', id],
        queryFn: () => jobPostApi.getById(id),
    });

    const { data: favoriteData, refetch: refetchFavorite } = useQuery({
        queryKey: ['favorite-check', id],
        queryFn: () => favoriteApi.checkIsFavorite(id),
        enabled: isAuthenticated && user?.role === 'CANDIDATE',
    });

    const isFavorite = favoriteData?.data === true;

    const post = data?.data;

    const handleApply = () => {
        if (!isAuthenticated) {
            toast.error('Please log in to apply');
            navigate('/login');
            return;
        }
        if (user?.role !== 'CANDIDATE') {
            toast.error('Only candidates can apply for jobs');
            return;
        }
        navigate(`/candidate/applications/new?jobPostId=${id}`);
    };

    const handleFavorite = async () => {
        if (!isAuthenticated) {
            toast.error('Please log in to save jobs');
            navigate('/login');
            return;
        }
        setIsFavoriting(true);
        try {
            if (isFavorite) {
                await favoriteApi.remove(id);
                toast.success('Removed from favorites');
            } else {
                await favoriteApi.add(id);
                toast.success('Saved to favorites');
            }

            await queryClient.invalidateQueries({
                queryKey: ['my-favorites']
            });

            await refetchFavorite();
        } catch (error) {
            toast.error('Something went wrong');
        } finally {
            setIsFavoriting(false);
        }
    };

    if (isLoading) {
        return (
            <PageLayout>
                <div className="flex justify-center py-16">
                    <Spinner />
                </div>
            </PageLayout>
        );
    }

    if (!post) {
        return (
            <PageLayout>
                <div className="text-center py-16">
                    <p className="text-gray-500">Job not found.</p>
                </div>
            </PageLayout>
        );
    }

    return (
        <PageLayout>
            <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
                {/* Back */}
                <button
                    onClick={() => navigate(-1)}
                    className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700 mb-6"
                >
                    <ArrowLeft size={16} />
                    Back to jobs
                </button>

                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    {/* Main content */}
                    <div className="lg:col-span-2 space-y-6">
                        {/* Header */}
                        <div className="bg-white rounded-xl border border-gray-200 p-6">
                            <div className="flex items-start gap-4">
                                {post.companyLogo ? (
                                    <img
                                        src={post.companyLogo}
                                        alt={post.companyName}
                                        className="w-16 h-16 rounded-lg object-cover"
                                    />
                                ) : (
                                    <div className="w-16 h-16 rounded-lg bg-primary-100 flex items-center justify-center flex-shrink-0">
                                        <Building2 size={28} className="text-primary-600" />
                                    </div>
                                )}
                                <div className="flex-1">
                                    <h1 className="text-xl font-bold text-gray-900 mb-1">
                                        {post.title}
                                    </h1>
                                    <p className="text-gray-600 font-medium">{post.companyName}</p>
                                </div>
                            </div>

                            <div className="flex flex-wrap gap-3 mt-4">
                                {post.city && (
                                    <span className="flex items-center gap-1 text-sm text-gray-500">
                                        <MapPin size={14} />
                                        {post.city}, {post.country}
                                    </span>
                                )}
                                <span className="flex items-center gap-1 text-sm text-gray-500">
                                    <Clock size={14} />
                                    Posted {formatDate(post.publishedAt)}
                                </span>
                                {(post.salaryMin || post.salaryMax) && (
                                    <span className="flex items-center gap-1 text-sm text-gray-500">
                                        {formatSalary(post.salaryMin, post.salaryMax)}
                                    </span>
                                )}
                                {post.employmentType && (
                                    <Badge variant="primary">
                                        {formatEmploymentType(post.employmentType)}
                                    </Badge>
                                )}
                                {post.categoryName && (
                                    <Badge variant="default">
                                        {post.categoryName}
                                    </Badge>
                                )}
                            </div>
                        </div>

                        {/* Description */}
                        <div className="bg-white rounded-xl border border-gray-200 p-6">
                            <h2 className="text-base font-semibold text-gray-900 mb-3">
                                Job Description
                            </h2>
                            <p className="text-sm text-gray-700 whitespace-pre-line leading-relaxed">
                                {post.description}
                            </p>
                        </div>

                        {/* Requirements */}
                        {post.requirements && (
                            <div className="bg-white rounded-xl border border-gray-200 p-6">
                                <h2 className="text-base font-semibold text-gray-900 mb-3">
                                    Requirements
                                </h2>
                                <p className="text-sm text-gray-700 whitespace-pre-line leading-relaxed">
                                    {post.requirements}
                                </p>
                            </div>
                        )}
                    </div>

                    {/* Sidebar */}
                    <div className="space-y-4">
                        <div className="bg-white rounded-xl border border-gray-200 p-5 sticky top-20">
                            <Button
                                className="w-full mb-3"
                                onClick={handleApply}
                            >
                                <Briefcase size={16} className="mr-2" />
                                Apply Now
                            </Button>

                            {isAuthenticated && user?.role === 'CANDIDATE' && (
                                <Button
                                    variant="secondary"
                                    className="w-full"
                                    onClick={handleFavorite}
                                    isLoading={isFavoriting}
                                >
                                    <Heart
                                        size={16}
                                        className="mr-2"
                                        fill={isFavorite ? 'currentColor' : 'none'}
                                    />
                                    {isFavorite ? 'Saved' : 'Save Job'}
                                </Button>
                            )}

                            <div className="mt-4 pt-4 border-t border-gray-100 space-y-2">
                                <div className="flex justify-between text-sm">
                                    <span className="text-gray-500">Employment</span>
                                    <span className="font-medium text-gray-900">
                                        {formatEmploymentType(post.employmentType)}
                                    </span>
                                </div>
                                {post.closesAt && (
                                    <div className="flex justify-between text-sm">
                                        <span className="text-gray-500">Closes</span>
                                        <span className="font-medium text-gray-900">
                                            {formatDate(post.closesAt)}
                                        </span>
                                    </div>
                                )}
                                {post.categoryName && (
                                    <div className="flex gap-2 text-sm">
                                        <span className="text-gray-500 min-w-[90px]">
                                            Category
                                        </span>
                                        <span className="font-medium text-gray-900 text-right flex-1">
                                            {post.categoryName}
                                        </span>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </PageLayout>
    );
}