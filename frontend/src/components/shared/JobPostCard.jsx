import { MapPin, Clock, DollarSign, Building2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import Card from '../ui/Card';
import Badge from '../ui/Badge';
import { formatSalary, formatDate, formatEmploymentType } from '../../utils/formatters';

export default function JobPostCard({ post }) {
    const navigate = useNavigate();

    const employmentTypeVariant = {
        FULL_TIME: 'primary',
        PART_TIME: 'info',
        CONTRACT: 'warning',
        INTERNSHIP: 'success',
        STUDENT: 'default',
    };

    return (
        <Card
            className="p-5 hover:border-primary-300 transition-colors"
            onClick={() => navigate(`/jobs/${post.id}`)}
        >
            <div className="flex items-start justify-between gap-4">
                <div className="flex-1 min-w-0">
                    {/* Company */}
                    <div className="flex items-center gap-2 mb-2">
                        {post.companyLogo ? (
                            <img
                                src={post.companyLogo}
                                alt={post.companyName}
                                className="w-8 h-8 rounded object-cover"
                            />
                        ) : (
                            <div className="w-8 h-8 rounded bg-primary-100 flex items-center justify-center">
                                <Building2 size={16} className="text-primary-600" />
                            </div>
                        )}
                        <span className="text-sm text-gray-600 font-medium">
                            {post.companyName}
                        </span>
                    </div>

                    {/* Title */}
                    <h3 className="text-base font-semibold text-gray-900 mb-2 truncate">
                        {post.title}
                    </h3>

                    {/* Meta */}
                    <div className="flex flex-wrap items-center gap-3 text-xs text-gray-500">
                        {post.city && (
                            <span className="flex items-center gap-1">
                                <MapPin size={12} />
                                {post.city}, {post.country}
                            </span>
                        )}
                        <span className="flex items-center gap-1">
                            <Clock size={12} />
                            {formatDate(post.publishedAt)}
                        </span>
                        {(post.salaryMin || post.salaryMax) && (
                            <span className="flex items-center gap-1">
                                <DollarSign size={12} />
                                {formatSalary(post.salaryMin, post.salaryMax)}
                            </span>
                        )}
                    </div>
                </div>

                {/* Badge */}
                {post.employmentType && (
                    <Badge variant={employmentTypeVariant[post.employmentType] || 'default'}>
                        {formatEmploymentType(post.employmentType)}
                    </Badge>
                )}
            </div>
        </Card>
    );
}