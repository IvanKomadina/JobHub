import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    Briefcase, FileText, ArrowLeft,
    Download, ChevronDown, Brain, User
} from 'lucide-react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Badge from '../../components/ui/Badge';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import EmptyState from '../../components/ui/EmptyState';
import { applicationApi } from '../../api/applicationApi';
import { jobPostApi } from '../../api/jobPostApi';
import { formatDate, formatApplicationStatus, formatRecommendation } from '../../utils/formatters';
import toast from 'react-hot-toast';

const navItems = [
    { to: '/employer/dashboard', icon: <Briefcase size={18} />, label: 'Dashboard' },
    { to: '/employer/posts', icon: <FileText size={18} />, label: 'My Posts' },
    { to: '/employer/profile', icon: <User size={18} />, label: 'Company Profile' },
];

const statusVariant = {
    DRAFT: 'default',
    PENDING: 'warning',
    ACCEPTED: 'success',
    REJECTED: 'danger',
    WITHDRAWN: 'default',
};

const recommendationVariant = {
    RECOMMENDED: 'success',
    CONSIDER: 'warning',
    NOT_RECOMMENDED: 'danger',
};

export default function EmployerApplicationsPage() {
    const { postId } = useParams();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [selectedApp, setSelectedApp] = useState(null);
    const [assessingId, setAssessingId] = useState(null);

    const { data: postData } = useQuery({
        queryKey: ['job', postId],
        queryFn: () => jobPostApi.getById(postId),
    });

    const { data, isLoading } = useQuery({
        queryKey: ['post-applications', postId],
        queryFn: () => applicationApi.getForPost(postId),
    });

    const { data: docsData } = useQuery({
        queryKey: ['employer-docs', selectedApp?.id],
        queryFn: () => applicationApi.getDocumentsEmployer(selectedApp.id),
        enabled: !!selectedApp,
    });


    const { data: assessmentData, refetch: refetchAssessment } = useQuery({
        queryKey: ['assessment', selectedApp?.id],
        queryFn: () => applicationApi.getAssessment(selectedApp.id),
        enabled: !!selectedApp?.id,
        retry: false,
        // Poll every 5 seconds while assessment is generating or pending
        refetchInterval: (query) => {
            const status = query.state.data?.data?.assessmentStatus;
            return status === 'PENDING' || status === 'GENERATING' ? 2000 : false;
        }
    });

    const statusMutation = useMutation({
        mutationFn: ({ id, status }) => applicationApi.updateStatus(id, { status }),
        onSuccess: () => {
            queryClient.invalidateQueries(['post-applications', postId]);
            toast.success('Status updated');
        },
        onError: (e) => toast.error(e.response?.data?.message || 'Failed to update'),
    });

    const applications = data?.data || [];
    const documents = docsData?.data || [];
    const assessment = assessmentData?.data;
    const post = postData?.data;

    return (
        <DashboardLayout navItems={navItems}>
            <button
                onClick={() => navigate('/employer/posts')}
                className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700 mb-6"
            >
                <ArrowLeft size={16} />
                Back to posts
            </button>

            <div className="mb-8">
                <h1 className="text-2xl font-bold text-gray-900">
                    Applications
                </h1>
                {post && (
                    <p className="text-gray-500 mt-1">
                        {post.title} · {applications.length} application(s)
                    </p>
                )}
            </div>

            {isLoading ? (
                <div className="flex justify-center py-16">
                    <Spinner />
                </div>
            ) : applications.length === 0 ? (
                <EmptyState
                    icon={<FileText size={48} />}
                    title="No applications yet"
                    description="Applications will appear here once candidates apply."
                />
            ) : (
                <div className="space-y-3">
                    {applications.map((app) => (
                        <Card key={app.id} className="p-5">
                            <div className="flex items-start justify-between gap-4">
                                <div className="flex-1">
                                    <p className="font-semibold text-gray-900">
                                        {app.candidateFirstName} {app.candidateLastName}
                                    </p>
                                    <p className="text-sm text-gray-500 mt-0.5">
                                        {app.candidateEmail} · Applied {formatDate(app.appliedAt)}
                                    </p>
                                </div>

                                <div className="flex items-center gap-2">
                                    <Badge variant={statusVariant[app.status]}>
                                        {formatApplicationStatus(app.status)}
                                    </Badge>

                                    {app.status === 'PENDING' && (
                                        <>
                                            <Button
                                                variant="success"
                                                size="sm"
                                                onClick={() => statusMutation.mutate({
                                                    id: app.id, status: 'ACCEPTED'
                                                })}
                                            >
                                                Accept
                                            </Button>
                                            <Button
                                                variant="danger"
                                                size="sm"
                                                onClick={() => statusMutation.mutate({
                                                    id: app.id, status: 'REJECTED'
                                                })}
                                            >
                                                Reject
                                            </Button>
                                        </>
                                    )}

                                    <Button
                                        variant="secondary"
                                        size="sm"
                                        onClick={() => setSelectedApp(app)}
                                    >
                                        View Details
                                    </Button>
                                </div>
                            </div>
                        </Card>
                    ))}
                </div>
            )}

            {/* Application detail modal */}
            <Modal
                isOpen={!!selectedApp}
                onClose={() => setSelectedApp(null)}
                title="Application Details"
                size="lg"
            >
                {selectedApp && (
                    <div className="space-y-5">
                        {/* Candidate info */}
                        <div>
                            <h3 className="font-semibold text-gray-900 text-lg">
                                {selectedApp.candidateFirstName} {selectedApp.candidateLastName}
                            </h3>
                            <p className="text-gray-500 text-sm">{selectedApp.candidateEmail}</p>
                        </div>

                        <div className="flex items-center gap-3">
                            <Badge variant={statusVariant[selectedApp.status]}>
                                {formatApplicationStatus(selectedApp.status)}
                            </Badge>
                            <span className="text-sm text-gray-500">
                                Applied {formatDate(selectedApp.appliedAt)}
                            </span>
                        </div>

                        {/* Cover letter */}
                        {selectedApp.coverLetter && (
                            <div>
                                <p className="text-sm font-medium text-gray-700 mb-1">
                                    Cover Letter
                                </p>
                                <p className="text-sm text-gray-600 bg-gray-50 rounded-lg p-3">
                                    {selectedApp.coverLetter}
                                </p>
                            </div>
                        )}

                        {/* Documents */}
                        {documents.length > 0 && (
                            <div>
                                <p className="text-sm font-medium text-gray-700 mb-2">
                                    Documents
                                </p>
                                <div className="space-y-2">
                                    {documents.map((doc) => (
                                        <div
                                            key={doc.id}
                                            className="flex items-center justify-between bg-gray-50 rounded-lg p-3"
                                        >
                                            <div>
                                                <p className="text-sm font-medium text-gray-900">
                                                    {doc.fileName}
                                                </p>
                                                <p className="text-xs text-gray-500">
                                                    {doc.fileType}
                                                </p>
                                            </div>
                                            
                                            <a
                                                href={doc.fileUrl}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                className="flex items-center gap-1 text-xs text-primary-600 hover:underline"
                                            >
                                                <Download size={12} />
                                                Download
                                            </a>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}

                        {/* AI Assessment */}
                        <div className="border-t border-gray-100 pt-4">
                            <div className="flex items-center justify-between mb-3">
                                <p className="text-sm font-medium text-gray-700">
                                    AI Assessment
                                </p>
                            </div>

                            {!assessment && (
                                <p className="text-sm text-gray-400 italic">
                                    No assessment available.
                                </p>
                            )}

                            {assessment?.assessmentStatus === 'PENDING' && (
                                <div className="flex items-center gap-2 text-sm text-amber-600 bg-amber-50 rounded-lg p-3">
                                    <Spinner size="sm" />
                                    Assessment is queued and will generate shortly...
                                </div>
                            )}

                            {assessment?.assessmentStatus === 'GENERATING' && (
                                <div className="flex items-center gap-2 text-sm text-blue-600 bg-blue-50 rounded-lg p-3">
                                    <Spinner size="sm" />
                                    AI is analyzing the candidate... This may take a minute.
                                </div>
                            )}

                            {assessment?.assessmentStatus === 'FAILED' && (
                                <div className="space-y-2">
                                    <div className="flex items-center gap-2 text-sm text-red-600 bg-red-50 rounded-lg p-3">
                                        Assessment generation failed.
                                    </div>
                                    <Button
                                        size="sm"
                                        variant="secondary"
                                        onClick={() => {
                                            applicationApi.generateAssessment(selectedApp.id);
                                            toast('Retrying assessment generation...');
                                        }}
                                        isLoading={assessingId === selectedApp.id}
                                    >
                                        <Brain size={14} className="mr-1" />
                                        Retry Assessment
                                    </Button>
                                </div>
                                )}

                                {assessment?.assessmentStatus === 'COMPLETED' && (
                                    <div className="bg-gray-50 rounded-lg p-4 space-y-3">
                                        <div className="flex items-center justify-between">
                                            <div className="flex items-center gap-3">
                                                <div className="text-2xl font-bold text-primary-600">
                                                    {assessment.matchScore}%
                                                </div>
                                                <Badge variant={recommendationVariant[assessment.recommendation]}>
                                                    {formatRecommendation(assessment.recommendation)}
                                                </Badge>
                                            </div>
                                        </div>

                                        {/* Score breakdown */}
                                        <div className="grid grid-cols-2 gap-2 text-xs">
                                            <div className="flex justify-between bg-white rounded p-2">
                                                <span className="text-gray-500">Skills</span>
                                                <span className="font-medium">{assessment.skillsScore}%</span>
                                            </div>
                                            <div className="flex justify-between bg-white rounded p-2">
                                                <span className="text-gray-500">Semantic</span>
                                                <span className="font-medium">{assessment.semanticScore}%</span>
                                            </div>
                                            <div className="flex justify-between bg-white rounded p-2">
                                                <span className="text-gray-500">Experience</span>
                                                <span className="font-medium">{assessment.experienceScore}%</span>
                                            </div>
                                            <div className="flex justify-between bg-white rounded p-2">
                                                <span className="text-gray-500">Education</span>
                                                <span className="font-medium">{assessment.educationScore}%</span>
                                            </div>
                                        </div>

                                        {/* Explanation */}
                                        {assessment.explanation && (
                                            <div>
                                                <p className="text-xs font-medium text-gray-600 mb-1">
                                                    AI Analysis
                                                </p>
                                                <p className="text-xs text-gray-600 leading-relaxed">
                                                    {assessment.explanation}
                                                </p>
                                            </div>
                                        )}
                                    </div>
                                )}
                        </div>

                        {/* Actions */}
                        {selectedApp.status === 'PENDING' && (
                            <div className="flex gap-3 border-t border-gray-100 pt-4">
                                <Button
                                    variant="success"
                                    className="flex-1"
                                    onClick={() => {
                                        statusMutation.mutate({
                                            id: selectedApp.id,
                                            status: 'ACCEPTED'
                                        });
                                        setSelectedApp(null);
                                    }}
                                >
                                    Accept
                                </Button>
                                <Button
                                    variant="danger"
                                    className="flex-1"
                                    onClick={() => {
                                        statusMutation.mutate({
                                            id: selectedApp.id,
                                            status: 'REJECTED'
                                        });
                                        setSelectedApp(null);
                                    }}
                                >
                                    Reject
                                </Button>
                            </div>
                        )}
                    </div>
                )}
            </Modal>
        </DashboardLayout>
    );
}