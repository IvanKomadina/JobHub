import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { FileText, Heart, Briefcase, Eye, X, Upload, User, Trash2, Search } from 'lucide-react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import DashboardLayout from '../../components/layout/DashboardLayout';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import EmptyState from '../../components/ui/EmptyState';
import { applicationApi } from '../../api/applicationApi';
import { formatDate, formatApplicationStatus } from '../../utils/formatters';
import toast from 'react-hot-toast';

const navItems = [
    { to: '/jobs', icon: <Search size={18} />, label: 'Browse Jobs' },
    { to: '/candidate/dashboard', icon: <Briefcase size={18} />, label: 'Dashboard' },
    { to: '/candidate/applications', icon: <FileText size={18} />, label: 'My Applications' },
    { to: '/candidate/resume', icon: <FileText size={18} />, label: 'My Resume' },
    { to: '/candidate/favorites', icon: <Heart size={18} />, label: 'Saved Jobs' },
    { to: '/candidate/profile', icon: <User size={18} />, label: 'My Profile' },
];

const statusVariant = {
    DRAFT: 'default',
    PENDING: 'warning',
    ACCEPTED: 'success',
    REJECTED: 'danger',
    WITHDRAWN: 'default',
};

export default function CandidateApplicationsPage() {
    const queryClient = useQueryClient();
    const [selectedApp, setSelectedApp] = useState(null);
    const [uploadFile, setUploadFile] = useState(null);
    const [documentType, setDocumentType] = useState('RESUME');
    const [isUploading, setIsUploading] = useState(false);

    const { data, isLoading } = useQuery({
        queryKey: ['my-applications'],
        queryFn: applicationApi.getMyApplications,
    });

    const { data: documentsData } = useQuery({
        queryKey: ['app-documents', selectedApp?.id],
        queryFn: () => applicationApi.getDocuments(selectedApp.id),
        enabled: !!selectedApp,
    });

    const withdrawMutation = useMutation({
        mutationFn: applicationApi.withdraw,
        onSuccess: () => {
            queryClient.invalidateQueries(['my-applications']);
            toast.success('Application withdrawn');
        },
        onError: (error) => {
            toast.error(error.response?.data?.message || 'Failed to withdraw');
        },
    });

    const discardMutation = useMutation({
        mutationFn: applicationApi.discardDraft,
        onSuccess: () => {
            queryClient.invalidateQueries(['my-applications']);
            toast.success('Draft discarded');
        },
    });

    const submitMutation = useMutation({
        mutationFn: applicationApi.submit,
        onSuccess: () => {
            queryClient.invalidateQueries(['my-applications']);
            toast.success('Application submitted successfully!');
            setSelectedApp(null);
        },
        onError: (error) => {
            toast.error(error.response?.data?.message || 'Failed to submit');
        },
    });

    const handleUploadDocument = async () => {
        if (!uploadFile || !selectedApp) return;
        setIsUploading(true);
        try {
            const formData = new FormData();
            formData.append('file', uploadFile);
            formData.append('type', documentType);
            await applicationApi.uploadDocument(selectedApp.id, formData);
            setUploadFile(null);
            queryClient.invalidateQueries(['app-documents', selectedApp.id]);
            toast.success('Document uploaded');
        } catch (error) {
            toast.error(error.response?.data?.message || 'Upload failed');
        } finally {
            setIsUploading(false);
        }
    };

    const handleDeleteDocument = async (documentId) => {
        try {
            await applicationApi.deleteDocument(selectedApp.id, documentId);
            queryClient.invalidateQueries(['app-documents', selectedApp.id]);
            toast.success('Document removed');
        } catch (error) {
            toast.error('Failed to remove document');
        }
    };

    const applications = data?.data || [];
    const documents = documentsData?.data || [];

    return (
        <DashboardLayout navItems={navItems}>
            <div className="mb-8">
                <h1 className="text-2xl font-bold text-gray-900">My Applications</h1>
                <p className="text-gray-500 mt-1">
                    Track your job applications and their status.
                </p>
            </div>

            {isLoading ? (
                <div className="flex justify-center py-16">
                    <Spinner />
                </div>
            ) : applications.length === 0 ? (
                <EmptyState
                    icon={<Briefcase size={48} />}
                    title="No applications yet"
                    description="Start applying to jobs to see them here."
                    action={
                        <Link to="/jobs">
                            <Button>Browse Jobs</Button>
                        </Link>
                    }
                />
            ) : (
                <div className="space-y-3">
                    {applications.map((app) => (
                        <Card key={app.id} className="p-5">
                            <div className="flex items-start justify-between gap-4">
                                <div className="flex-1">
                                    <h3 className="font-semibold text-gray-900">
                                        {app.jobPostTitle}
                                    </h3>
                                    <p className="text-sm text-gray-500 mt-0.5">
                                        {app.companyName} · Applied {formatDate(app.appliedAt)}
                                    </p>
                                </div>
                                <div className="flex items-center gap-2">
                                    <Badge variant={statusVariant[app.status]}>
                                        {formatApplicationStatus(app.status)}
                                    </Badge>
                                    <Button
                                        variant="ghost"
                                        size="sm"
                                        onClick={() => setSelectedApp(app)}
                                    >
                                        <Eye size={16} />
                                    </Button>
                                    {app.status === 'PENDING' && (
                                        <Button
                                            variant="danger"
                                            size="sm"
                                            onClick={() => withdrawMutation.mutate(app.id)}
                                            isLoading={withdrawMutation.isPending}
                                        >
                                            Withdraw
                                        </Button>
                                    )}
                                    {app.status === 'DRAFT' && (
                                        <Button
                                            variant="danger"
                                            size="sm"
                                            onClick={() => discardMutation.mutate(app.id)}
                                            isLoading={discardMutation.isPending}
                                        >
                                            Discard
                                        </Button>
                                    )}
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
                        <div>
                            <h3 className="font-semibold text-gray-900 text-lg">
                                {selectedApp.jobPostTitle}
                            </h3>
                            <p className="text-gray-500">{selectedApp.companyName}</p>
                        </div>

                        <div className="flex items-center gap-3">
                            <Badge variant={statusVariant[selectedApp.status]}>
                                {formatApplicationStatus(selectedApp.status)}
                            </Badge>
                            <span className="text-sm text-gray-500">
                                Applied {formatDate(selectedApp.appliedAt)}
                            </span>
                        </div>

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
                        <div>
                            <p className="text-sm font-medium text-gray-700 mb-2">
                                Documents
                            </p>
                            {documents.length === 0 ? (
                                <p className="text-sm text-gray-500">No documents uploaded.</p>
                            ) : (
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
                                                <p className="text-xs text-gray-500">{doc.fileType}</p>
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <a
                                                    href={doc.fileUrl}
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                    className="text-xs text-primary-600 hover:underline"
                                                >
                                                    View
                                                </a>
                                                {selectedApp.status === 'DRAFT' && (
                                                    <button
                                                        onClick={() => handleDeleteDocument(doc.id)}
                                                        className="text-red-500 hover:text-red-700"
                                                    >
                                                        <X size={14} />
                                                    </button>
                                                )}
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>

                        {/* Upload document (only for drafts) */}
                        {selectedApp.status === 'DRAFT' && (
                            <div className="border-t border-gray-100 pt-4">
                                <p className="text-sm font-medium text-gray-700 mb-2">
                                    Upload Document
                                </p>
                                <div className="flex gap-2 mb-2">
                                    <select
                                        value={documentType}
                                        onChange={(e) => setDocumentType(e.target.value)}
                                        className="border border-gray-300 rounded-lg px-3 py-2 text-sm"
                                    >
                                        <option value="RESUME">Resume/CV</option>
                                        <option value="COVER_LETTER">Cover Letter</option>
                                        <option value="CERTIFICATE">Certificate</option>
                                        <option value="OTHER">Other</option>
                                    </select>
                                    <input
                                        type="file"
                                        accept=".pdf,.doc,.docx"
                                        onChange={(e) => setUploadFile(e.target.files[0])}
                                        className="flex-1 text-sm border border-gray-300 rounded-lg px-3 py-2"
                                    />
                                </div>
                                <Button
                                    size="sm"
                                    onClick={handleUploadDocument}
                                    isLoading={isUploading}
                                    disabled={!uploadFile}
                                >
                                    <Upload size={14} className="mr-1" />
                                    Upload
                                </Button>
                            </div>
                        )}

                        {/* Submit button for drafts */}
                        {selectedApp.status === 'DRAFT' && (
                            <div className="border-t border-gray-100 pt-4">
                                <Button
                                    className="w-full"
                                    onClick={() => submitMutation.mutate(selectedApp.id)}
                                    isLoading={submitMutation.isPending}
                                >
                                    Submit Application
                                </Button>
                                <p className="text-xs text-gray-500 text-center mt-2">
                                    Make sure you've uploaded your CV before submitting.
                                </p>
                            </div>
                        )}
                    </div>
                )}
            </Modal>
        </DashboardLayout>
    );
}