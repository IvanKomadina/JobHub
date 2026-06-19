import { useState } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Upload, X, FileText, Briefcase, Heart, User, Search } from 'lucide-react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Textarea from '../../components/ui/Textarea';
import Spinner from '../../components/ui/Spinner';
import { applicationApi } from '../../api/applicationApi';
import { jobPostApi } from '../../api/jobPostApi';
import { formatEmploymentType } from '../../utils/formatters';
import toast from 'react-hot-toast';

const navItems = [
    { to: '/jobs', icon: <Search size={18} />, label: 'Browse Jobs' },
    { to: '/candidate/dashboard', icon: <Briefcase size={18} />, label: 'Dashboard' },
    { to: '/candidate/applications', icon: <FileText size={18} />, label: 'My Applications' },
    { to: '/candidate/resume', icon: <FileText size={18} />, label: 'My Resume' },
    { to: '/candidate/favorites', icon: <Heart size={18} />, label: 'Saved Jobs' },
    { to: '/candidate/profile', icon: <User size={18} />, label: 'My Profile' },
];

export default function ApplyPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const jobPostId = searchParams.get('jobPostId');
    const queryClient = useQueryClient();

    const [step, setStep] = useState(1); // 1: cover letter, 2: documents, 3: review
    const [applicationId, setApplicationId] = useState(null);
    const [coverLetter, setCoverLetter] = useState('');
    const [documents, setDocuments] = useState([]);
    const [uploadFile, setUploadFile] = useState(null);
    const [documentType, setDocumentType] = useState('RESUME');
    const [isUploading, setIsUploading] = useState(false);

    const { data: jobData, isLoading } = useQuery({
        queryKey: ['job', jobPostId],
        queryFn: () => jobPostApi.getById(jobPostId),
        enabled: !!jobPostId,
    });

    const createDraftMutation = useMutation({
        mutationFn: () => applicationApi.createDraft(jobPostId, { coverLetter }),
        onSuccess: (data) => {
            queryClient.invalidateQueries(['my-applications']);
            setApplicationId(data.data.id);
            setStep(2);
            toast.success('Application draft created');
        },
        onError: (error) => {
            toast.error(error.response?.data?.message || 'Failed to create application');
        },
    });

    const submitMutation = useMutation({
        mutationFn: () => applicationApi.submit(applicationId),
        onSuccess: () => {
            toast.success('Application submitted successfully! 🎉');
            navigate('/candidate/applications');
        },
        onError: (error) => {
            toast.error(error.response?.data?.message || 'Failed to submit');
        },
    });

    const handleUploadDocument = async () => {
        if (!uploadFile) return;
        setIsUploading(true);
        try {
            const formData = new FormData();
            formData.append('file', uploadFile);
            formData.append('type', documentType);
            const response = await applicationApi.uploadDocument(applicationId, formData);
            setDocuments(prev => [...prev, response.data]);
            setUploadFile(null);
            toast.success('Document uploaded');
        } catch (error) {
            toast.error(error.response?.data?.message || 'Upload failed');
        } finally {
            setIsUploading(false);
        }
    };

    const handleDeleteDocument = async (documentId) => {
        try {
            await applicationApi.deleteDocument(applicationId, documentId);
            setDocuments(prev => prev.filter(d => d.id !== documentId));
            toast.success('Document removed');
        } catch (error) {
            toast.error('Failed to remove document');
        }
    };

    const hasResume = documents.some(d => d.fileType === 'RESUME');
    const post = jobData?.data;

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
                {/* Back */}
                <button
                    onClick={() => navigate(-1)}
                    className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700 mb-6"
                >
                    <ArrowLeft size={16} />
                    Back
                </button>

                <h1 className="text-2xl font-bold text-gray-900 mb-2">Apply for Job</h1>

                {/* Job info */}
                {post && (
                    <Card className="p-4 mb-6">
                        <h2 className="font-semibold text-gray-900">{post.title}</h2>
                        <p className="text-sm text-gray-500">
                            {post.companyName}
                            {post.city && ` · ${post.city}`}
                            {post.employmentType && ` · ${formatEmploymentType(post.employmentType)}`}
                        </p>
                    </Card>
                )}

                {/* Steps indicator */}
                <div className="flex items-center gap-2 mb-8">
                    {[1, 2, 3].map((s) => (
                        <div key={s} className="flex items-center gap-2">
                            <div className={`
                                w-7 h-7 rounded-full flex items-center justify-center text-xs font-medium
                                ${step >= s
                                    ? 'bg-primary-600 text-white'
                                    : 'bg-gray-200 text-gray-500'
                                }
                            `}>
                                {s}
                            </div>
                            <span className={`text-sm ${step >= s ? 'text-gray-900 font-medium' : 'text-gray-400'}`}>
                                {s === 1 ? 'Cover Letter' : s === 2 ? 'Documents' : 'Review'}
                            </span>
                            {s < 3 && <div className="w-8 h-px bg-gray-200 mx-1" />}
                        </div>
                    ))}
                </div>

                {/* Step 1 - Cover letter */}
                {step === 1 && (
                    <Card className="p-6">
                        <h3 className="font-semibold text-gray-900 mb-4">
                            Cover Letter (Optional)
                        </h3>
                        <Textarea
                            placeholder="Write a brief cover letter explaining why you're a good fit for this position..."
                            rows={6}
                            value={coverLetter}
                            onChange={(e) => setCoverLetter(e.target.value)}
                        />
                        <Button
                            className="w-full mt-4"
                            onClick={() => createDraftMutation.mutate()}
                            isLoading={createDraftMutation.isPending}
                        >
                            Continue to Documents
                        </Button>
                    </Card>
                )}

                {/* Step 2 - Documents */}
                {step === 2 && (
                    <Card className="p-6">
                        <h3 className="font-semibold text-gray-900 mb-1">
                            Upload Documents
                        </h3>
                        <p className="text-sm text-gray-500 mb-4">
                            A CV/Resume is required to submit your application.
                        </p>

                        {/* Uploaded documents */}
                        {documents.length > 0 && (
                            <div className="space-y-2 mb-4">
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
                                        <button
                                            onClick={() => handleDeleteDocument(doc.id)}
                                            className="text-red-400 hover:text-red-600"
                                        >
                                            <X size={16} />
                                        </button>
                                    </div>
                                ))}
                            </div>
                        )}

                        {/* Upload form */}
                        <div className="border-2 border-dashed border-gray-200 rounded-lg p-4 mb-4">
                            <div className="flex gap-2 mb-3">
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
                                    className="flex-1 text-sm"
                                />
                            </div>
                            <Button
                                size="sm"
                                variant="secondary"
                                onClick={handleUploadDocument}
                                isLoading={isUploading}
                                disabled={!uploadFile}
                            >
                                <Upload size={14} className="mr-1" />
                                Upload File
                            </Button>
                        </div>

                        {!hasResume && (
                            <p className="text-xs text-amber-600 bg-amber-50 rounded-lg p-2 mb-4">
                                ⚠️ Please upload your CV/Resume before submitting.
                            </p>
                        )}

                        <Button
                            className="w-full"
                            onClick={() => setStep(3)}
                            disabled={!hasResume}
                        >
                            Review Application
                        </Button>
                    </Card>
                )}

                {/* Step 3 - Review */}
                {step === 3 && (
                    <Card className="p-6">
                        <h3 className="font-semibold text-gray-900 mb-4">
                            Review Your Application
                        </h3>

                        <div className="space-y-4">
                            <div>
                                <p className="text-sm font-medium text-gray-700">Position</p>
                                <p className="text-sm text-gray-900">{post?.title} at {post?.companyName}</p>
                            </div>

                            {coverLetter && (
                                <div>
                                    <p className="text-sm font-medium text-gray-700">Cover Letter</p>
                                    <p className="text-sm text-gray-600 bg-gray-50 rounded p-3 mt-1">
                                        {coverLetter}
                                    </p>
                                </div>
                            )}

                            <div>
                                <p className="text-sm font-medium text-gray-700">Documents ({documents.length})</p>
                                <ul className="mt-1 space-y-1">
                                    {documents.map(doc => (
                                        <li key={doc.id} className="text-sm text-gray-600">
                                            • {doc.fileName} ({doc.fileType})
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        </div>

                        <div className="flex gap-3 mt-6">
                            <Button
                                variant="secondary"
                                onClick={() => setStep(2)}
                                className="flex-1"
                            >
                                Back
                            </Button>
                            <Button
                                className="flex-1"
                                onClick={() => submitMutation.mutate()}
                                isLoading={submitMutation.isPending}
                            >
                                Submit Application 🚀
                            </Button>
                        </div>
                    </Card>
                )}
            </div>
        </DashboardLayout>
    );
}