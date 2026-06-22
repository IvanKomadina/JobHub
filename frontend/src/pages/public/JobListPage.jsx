import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Search, SlidersHorizontal, X } from 'lucide-react';
import PageLayout from '../../components/layout/PageLayout';
import JobPostCard from '../../components/shared/JobPostCard';
import Input from '../../components/ui/Input';
import Select from '../../components/ui/Select';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import Pagination from '../../components/ui/Pagination';
import EmptyState from '../../components/ui/EmptyState';
import { jobPostApi } from '../../api/jobPostApi';
import { useQuery as useLocationQuery } from '@tanstack/react-query';
import api from '../../api/axios';

const EMPLOYMENT_TYPES = [
    { value: 'FULL_TIME', label: 'Full Time' },
    { value: 'PART_TIME', label: 'Part Time' },
    { value: 'CONTRACT', label: 'Contract' },
    { value: 'INTERNSHIP', label: 'Internship' },
    { value: 'STUDENT', label: 'Student' },
];

export default function JobListPage() {
    const [page, setPage] = useState(0);
    const [filters, setFilters] = useState({
        keyword: '',
        categoryId: '',
        location: '',
        employmentType: '',
        sortBy: 'publishedAt',
        sortDirection: 'desc',
    });
    const [appliedFilters, setAppliedFilters] = useState(filters);

    const { data: categoriesData } = useQuery({
        queryKey: ['categories'],
        queryFn: () => api.get('/api/categories'),
    });

    const { data, isLoading } = useQuery({
        queryKey: ['jobs', appliedFilters, page],
        queryFn: () => jobPostApi.getAll({
            ...appliedFilters,
            page,
            size: 10,
        }),
    });

    const posts = data?.data?.content || [];
    const totalPages = data?.data?.totalPages || 0;

    const categories = categoriesData?.data?.map(c => ({
        value: c.id, label: c.name
    })) || [];

    const handleSearch = () => {
        setPage(0);
        setAppliedFilters(filters);
    };

    const handleClear = () => {
        const reset = {
            keyword: '',
            categoryId: '',
            location: '',
            employmentType: '',
            sortBy: 'publishedAt',
            sortDirection: 'desc',
        };
        setFilters(reset);
        setAppliedFilters(reset);
        setPage(0);
    };

    const hasActiveFilters = appliedFilters.keyword ||
        appliedFilters.categoryId ||
        appliedFilters.location ||
        appliedFilters.employmentType;

    return (
        <PageLayout>
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
                <h1 className="text-2xl font-bold text-gray-900 mb-6">Browse Jobs</h1>

                {/* Filters */}
                <div className="bg-white rounded-xl border border-gray-200 p-5 mb-6">
                    <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-4">
                        <div className="md:col-span-2">
                            <Input
                                placeholder="Search by job title..."
                                value={filters.keyword}
                                onChange={(e) => setFilters(f => ({
                                    ...f, keyword: e.target.value
                                }))}
                                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                            />
                        </div>
                        <Input
                            placeholder="Location"
                            value={filters.location}
                            onChange={(e) => setFilters(f => ({ ...f, location: e.target.value }))}
                            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                        />
                        <Select
                            placeholder="All Categories"
                            options={categories}
                            value={filters.categoryId}
                            onChange={(e) => setFilters(f => ({
                                ...f, categoryId: e.target.value
                            }))}
                        />
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                        <Select
                            placeholder="Employment Type"
                            options={EMPLOYMENT_TYPES}
                            value={filters.employmentType}
                            onChange={(e) => setFilters(f => ({
                                ...f, employmentType: e.target.value
                            }))}
                        />
                        <Select
                            placeholder="Order"
                            options={[
                                { value: 'desc', label: 'Newest First' },
                                { value: 'asc', label: 'Oldest First' },
                            ]}
                            value={filters.sortDirection}
                            onChange={(e) => setFilters(f => ({
                                ...f, sortDirection: e.target.value
                            }))}
                        />
                        <div className="flex gap-2">
                            <Button
                                onClick={handleSearch}
                                className="flex-1"
                            >
                                <Search size={16} className="mr-2" />
                                Search
                            </Button>
                            {hasActiveFilters && (
                                <Button
                                    variant="secondary"
                                    onClick={handleClear}
                                >
                                    <X size={16} />
                                </Button>
                            )}
                        </div>
                    </div>
                </div>

                {/* Results */}
                {isLoading ? (
                    <div className="flex justify-center py-16">
                        <Spinner />
                    </div>
                ) : posts.length === 0 ? (
                    <EmptyState
                        icon={<Search size={48} />}
                        title="No jobs found"
                        description="Try adjusting your search filters to find more results."
                        action={
                            hasActiveFilters && (
                                <Button variant="secondary" onClick={handleClear}>
                                    Clear filters
                                </Button>
                            )
                        }
                    />
                ) : (
                    <>
                        <p className="text-sm text-gray-500 mb-4">
                            {data?.data?.totalElements} jobs found
                        </p>
                        <div className="space-y-3">
                            {posts.map((post) => (
                                <JobPostCard key={post.id} post={post} />
                            ))}
                        </div>
                        <Pagination
                            page={page}
                            totalPages={totalPages}
                            onPageChange={setPage}
                        />
                    </>
                )}
            </div>
        </PageLayout>
    );
}