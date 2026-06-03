import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Heart, Briefcase, FileText, MapPin, Trash2 } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import DashboardLayout from '../../components/layout/DashboardLayout';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import EmptyState from '../../components/ui/EmptyState';
import { favoriteApi } from '../../api/favoriteApi';
import { formatDate } from '../../utils/formatters';
import toast from 'react-hot-toast';

const navItems = [
    { to: '/candidate/dashboard', icon: <Briefcase size={18} />, label: 'Dashboard' },
    { to: '/candidate/applications', icon: <FileText size={18} />, label: 'My Applications' },
    { to: '/candidate/resume', icon: <FileText size={18} />, label: 'My Resume' },
    { to: '/candidate/favorites', icon: <Heart size={18} />, label: 'Saved Jobs' },
];

export default function CandidateFavoritesPage() {
    const queryClient = useQueryClient();
    const navigate = useNavigate();

    const { data, isLoading } = useQuery({
        queryKey: ['my-favorites'],
        queryFn: favoriteApi.getAll,
    });

    const removeMutation = useMutation({
        mutationFn: favoriteApi.remove,
        onSuccess: () => {
            queryClient.invalidateQueries(['my-favorites']);
            toast.success('Removed from favorites');
        },
    });

    const favorites = data?.data || [];

    return (
        <DashboardLayout navItems={navItems}>
            <div className="mb-8">
                <h1 className="text-2xl font-bold text-gray-900">Saved Jobs</h1>
                <p className="text-gray-500 mt-1">
                    Jobs you've saved for later.
                </p>
            </div>

            {isLoading ? (
                <div className="flex justify-center py-16">
                    <Spinner />
                </div>
            ) : favorites.length === 0 ? (
                <EmptyState
                    icon={<Heart size={48} />}
                    title="No saved jobs"
                    description="Save jobs you're interested in to come back to them later."
                    action={
                        <Link to="/jobs">
                            <Button>Browse Jobs</Button>
                        </Link>
                    }
                />
            ) : (
                <div className="space-y-3">
                    {favorites.map((fav) => (
                        <Card key={fav.id} className="p-5">
                            <div className="flex items-start justify-between gap-4">
                                <div                                >
                                    <h3 className="font-semibold text-gray-900">
                                        {fav.jobPostTitle}
                                    </h3>
                                    <div className="flex items-center gap-3 mt-1">
                                        <span className="text-sm text-gray-500">
                                            {fav.companyName}
                                        </span>
                                        {fav.city && (
                                            <span className="flex items-center gap-1 text-sm text-gray-500">
                                                <MapPin size={12} />
                                                {fav.city}, {fav.country}
                                            </span>
                                        )}
                                        <span className="text-xs text-gray-400">
                                            Saved {formatDate(fav.savedAt)}
                                        </span>
                                    </div>
                                </div>
                                <div className="flex items-center gap-2">
                                    <Button
                                        variant="secondary"
                                        size="sm"
                                        onClick={() => navigate(`/jobs/${fav.jobPostId}`)}
                                    >
                                        View Job
                                    </Button>
                                    <Button
                                        variant="ghost"
                                        size="sm"
                                        onClick={() => removeMutation.mutate(fav.id)}
                                        isLoading={removeMutation.isPending}
                                    >
                                        <Trash2 size={16} className="text-red-500" />
                                    </Button>
                                </div>
                            </div>
                        </Card>
                    ))}
                </div>
            )}
        </DashboardLayout>
    );
}