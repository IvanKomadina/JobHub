import { Link } from 'react-router-dom';
import { Search, Briefcase, Users, Building2, ArrowRight } from 'lucide-react';
import PageLayout from '../../components/layout/PageLayout';
import Button from '../../components/ui/Button';
import { useQuery } from '@tanstack/react-query';
import { jobPostApi } from '../../api/jobPostApi';
import JobPostCard from '../../components/shared/JobPostCard';
import Spinner from '../../components/ui/Spinner';
import useAuthStore from '../../store/authStore';

export default function HomePage() {
    const { user } = useAuthStore();
    const { data, isLoading } = useQuery({
        queryKey: ['jobs', 'recent'],
        queryFn: () => jobPostApi.getAll({ page: 0, size: 6, sortBy: 'publishedAt', sortDirection: 'desc' }),
    });

    const posts = data?.data?.content || [];

    return (
        <PageLayout>
            {/* Hero */}
            <section className="bg-gradient-to-br from-primary-700 to-primary-900 text-white py-20">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
                    <h1 className="text-4xl md:text-5xl font-bold mb-4">
                        Find Your Dream Job
                    </h1>
                    <p className="text-primary-200 text-lg mb-10 max-w-xl mx-auto">
                        Connect with top employers and discover opportunities that match your skills and ambitions.
                    </p>
                    <div className="flex flex-col sm:flex-row gap-4 justify-center">
                        <Link to="/jobs">
                            <Button size="lg" className="text-primary-700 hover:bg-primary-50 w-full sm:w-auto">
                                <Search size={18} className="mr-2" />
                                Browse Jobs
                            </Button>
                        </Link>
                        <Link to="/register">
                            <Button size="lg" className="border-white text-white hover:bg-primary-50 w-full sm:w-auto">
                                Post a Job
                            </Button>
                        </Link>
                    </div>
                </div>
            </section>

            {/* Stats */}
            <section className="bg-white border-b border-gray-200">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
                    <div className="grid grid-cols-3 gap-8 text-center">
                        <div>
                            <div className="flex justify-center mb-2">
                                <Briefcase className="text-primary-600" size={28} />
                            </div>
                            <p className="text-2xl font-bold text-gray-900">500+</p>
                            <p className="text-sm text-gray-500">Active Jobs</p>
                        </div>
                        <div>
                            <div className="flex justify-center mb-2">
                                <Users className="text-primary-600" size={28} />
                            </div>
                            <p className="text-2xl font-bold text-gray-900">10k+</p>
                            <p className="text-sm text-gray-500">Candidates</p>
                        </div>
                        <div>
                            <div className="flex justify-center mb-2">
                                <Building2 className="text-primary-600" size={28} />
                            </div>
                            <p className="text-2xl font-bold text-gray-900">200+</p>
                            <p className="text-sm text-gray-500">Companies</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* Recent Jobs */}
            <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
                <div className="flex items-center justify-between mb-8">
                    <h2 className="text-2xl font-bold text-gray-900">Latest Jobs</h2>
                    <Link
                        to="/jobs"
                        className="flex items-center gap-1 text-sm text-primary-600 hover:text-primary-700 font-medium"
                    >
                        View all
                        <ArrowRight size={16} />
                    </Link>
                </div>

                {isLoading ? (
                    <div className="flex justify-center py-12">
                        <Spinner />
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                        {posts.map((post) => (
                            <JobPostCard key={post.id} post={post} />
                        ))}
                    </div>
                )}
            </section>

            {/* CTA */}
            {user == null && (
                <section className="bg-primary-50 border-t border-primary-100">
                    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 text-center">
                        <h2 className="text-2xl font-bold text-gray-900 mb-3">
                            Ready to take the next step?
                        </h2>
                        <p className="text-gray-600 mb-6">
                            Join thousands of candidates and employers on JobHub.
                        </p>
                        <Link to="/register">
                            <Button size="lg">
                                Get Started Free
                                <ArrowRight size={18} className="ml-2" />
                            </Button>
                        </Link>
                    </div>
                </section>
            )}
        </PageLayout>
    );
}