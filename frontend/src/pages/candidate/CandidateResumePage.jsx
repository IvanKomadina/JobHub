import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    FileText, Briefcase, Heart, Plus, Trash2,
    Edit2, Download, ChevronDown, ChevronUp, X, User, Search
} from 'lucide-react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Textarea from '../../components/ui/Textarea';
import Select from '../../components/ui/Select';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import { resumeApi } from '../../api/resumeApi';
import toast from 'react-hot-toast';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

const navItems = [
    { to: '/jobs', icon: <Search size={18} />, label: 'Browse Jobs' },
    { to: '/candidate/dashboard', icon: <Briefcase size={18} />, label: 'Dashboard' },
    { to: '/candidate/applications', icon: <FileText size={18} />, label: 'My Applications' },
    { to: '/candidate/resume', icon: <FileText size={18} />, label: 'My Resume' },
    { to: '/candidate/favorites', icon: <Heart size={18} />, label: 'Saved Jobs' },
    { to: '/candidate/profile', icon: <User size={18} />, label: 'My Profile' },
];

const SKILL_LEVELS = [
    { value: 'BEGINNER', label: 'Beginner' },
    { value: 'INTERMEDIATE', label: 'Intermediate' },
    { value: 'ADVANCED', label: 'Advanced' },
];

const LANGUAGE_LEVELS = [
    { value: 'A1', label: 'A1 - Beginner' },
    { value: 'A2', label: 'A2 - Elementary' },
    { value: 'B1', label: 'B1 - Intermediate' },
    { value: 'B2', label: 'B2 - Upper Intermediate' },
    { value: 'C1', label: 'C1 - Advanced' },
    { value: 'C2', label: 'C2 - Proficient' },
    { value: 'NATIVE', label: 'Native' },
];

// ==================== SCHEMAS ====================

const educationSchema = z.object({
    institution: z.string().min(1, 'Institution is required'),
    degree: z.string().optional(),
    fieldOfStudy: z.string().optional(),
    startDate: z.string().optional(),
    endDate: z.string().optional(),
    description: z.string().optional(),
});

const experienceSchema = z.object({
    company: z.string().min(1, 'Company is required'),
    position: z.string().min(1, 'Position is required'),
    location: z.string().optional(),
    startDate: z.string().optional(),
    endDate: z.string().optional(),
    description: z.string().optional(),
});

const skillSchema = z.object({
    skillName: z.string().min(1, 'Skill name is required'),
    skillLevel: z.string().optional(),
});

const languageSchema = z.object({
    languageName: z.string().min(1, 'Language is required'),
    languageLevel: z.string().optional(),
});

// ==================== SECTION WRAPPER ====================

function ResumeSection({ title, children, onAdd, addLabel = 'Add' }) {
    const [isOpen, setIsOpen] = useState(true);

    return (
        <Card className="overflow-hidden">
            <div
                className="flex items-center justify-between p-5 cursor-pointer hover:bg-gray-50"
                onClick={() => setIsOpen(o => !o)}
            >
                <h3 className="font-semibold text-gray-900">{title}</h3>
                <div className="flex items-center gap-2" onClick={e => e.stopPropagation()}>
                    {onAdd && (
                        <Button size="sm" variant="secondary" onClick={onAdd}>
                            <Plus size={14} className="mr-1" />
                            {addLabel}
                        </Button>
                    )}
                    {isOpen
                        ? <ChevronUp size={18} className="text-gray-400" />
                        : <ChevronDown size={18} className="text-gray-400" />
                    }
                </div>
            </div>
            {isOpen && (
                <div className="border-t border-gray-100 p-5">
                    {children}
                </div>
            )}
        </Card>
    );
}

// ==================== MAIN PAGE ====================

export default function CandidateResumePage() {
    const queryClient = useQueryClient();

    // Modal states
    const [educationModal, setEducationModal] = useState({ open: false, data: null });
    const [experienceModal, setExperienceModal] = useState({ open: false, data: null });
    const [skillModal, setSkillModal] = useState({ open: false, data: null });
    const [languageModal, setLanguageModal] = useState({ open: false, data: null });
    const [summaryEdit, setSummaryEdit] = useState(false);
    const [summaryText, setSummaryText] = useState('');
    const [isDownloading, setIsDownloading] = useState(false);
    const [isCreating, setIsCreating] = useState(false);

    // Load resume
    const { data, isLoading, isError } = useQuery({
        queryKey: ['my-resume'],
        queryFn: resumeApi.get,
        retry: false,
    });

    const resume = data?.data;

    // Invalidate helper
    const invalidate = () => queryClient.invalidateQueries(['my-resume']);

    // Create resume
    const handleCreateResume = async () => {
        setIsCreating(true);
        try {
            await resumeApi.create({ summary: '' });
            invalidate();
            toast.success('Resume created!');
        } catch (e) {
            toast.error('Failed to create resume');
        } finally {
            setIsCreating(false);
        }
    };

    // Download PDF
    const handleDownloadPdf = async () => {
        setIsDownloading(true);
        try {
            const response = await resumeApi.downloadPdf();
            const blob = new Blob([response.data], { type: 'application/pdf' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'resume.pdf';
            a.click();
            URL.revokeObjectURL(url);
            toast.success('Resume downloaded!');
        } catch (e) {
            toast.error('Failed to download resume');
        } finally {
            setIsDownloading(false);
        }
    };

    // Update summary
    const handleSaveSummary = async () => {
        try {
            await resumeApi.updateSummary({ summary: summaryText });
            invalidate();
            setSummaryEdit(false);
            toast.success('Summary updated');
        } catch (e) {
            toast.error('Failed to update summary');
        }
    };

    if (isLoading) {
        return (
            <DashboardLayout navItems={navItems}>
                <div className="flex justify-center py-16">
                    <Spinner />
                </div>
            </DashboardLayout>
        );
    }

    if (isError || !resume) {
        return (
            <DashboardLayout navItems={navItems}>
                <div className="max-w-2xl mx-auto text-center py-16">
                    <FileText size={48} className="text-gray-300 mx-auto mb-4" />
                    <h2 className="text-xl font-bold text-gray-900 mb-2">
                        No resume yet
                    </h2>
                    <p className="text-gray-500 mb-6">
                        Create your resume to start applying for jobs.
                    </p>
                    <Button onClick={handleCreateResume} isLoading={isCreating}>
                        Create Resume
                    </Button>
                </div>
            </DashboardLayout>
        );
    }

    return (
        <DashboardLayout navItems={navItems}>
            <div className="max-w-3xl mx-auto">
                {/* Header */}
                <div className="flex items-center justify-between mb-8">
                    <div>
                        <h1 className="text-2xl font-bold text-gray-900">My Resume</h1>
                        <p className="text-gray-500 mt-1">
                            Build and manage your professional profile.
                        </p>
                    </div>
                    <Button
                        variant="secondary"
                        onClick={handleDownloadPdf}
                        isLoading={isDownloading}
                    >
                        <Download size={16} className="mr-2" />
                        Download PDF
                    </Button>
                </div>

                <div className="space-y-4">
                    {/* Summary */}
                    <ResumeSection title="Professional Summary">
                        {summaryEdit ? (
                            <div className="space-y-3">
                                <Textarea
                                    rows={4}
                                    value={summaryText}
                                    onChange={(e) => setSummaryText(e.target.value)}
                                    placeholder="Write a brief professional summary..."
                                />
                                <div className="flex gap-2">
                                    <Button size="sm" onClick={handleSaveSummary}>
                                        Save
                                    </Button>
                                    <Button
                                        size="sm"
                                        variant="secondary"
                                        onClick={() => setSummaryEdit(false)}
                                    >
                                        Cancel
                                    </Button>
                                </div>
                            </div>
                        ) : (
                            <div className="flex items-start justify-between gap-4">
                                <p className="text-sm text-gray-600 flex-1">
                                    {resume.summary || (
                                        <span className="text-gray-400 italic">
                                            No summary yet. Click edit to add one.
                                        </span>
                                    )}
                                </p>
                                <Button
                                    size="sm"
                                    variant="ghost"
                                    onClick={() => {
                                        setSummaryText(resume.summary || '');
                                        setSummaryEdit(true);
                                    }}
                                >
                                    <Edit2 size={14} />
                                </Button>
                            </div>
                        )}
                    </ResumeSection>

                    {/* Experience */}
                    <ResumeSection
                        title="Work Experience"
                        onAdd={() => setExperienceModal({ open: true, data: null })}
                    >
                        {resume.experience?.length === 0 ? (
                            <p className="text-sm text-gray-400 italic">
                                No experience added yet.
                            </p>
                        ) : (
                            <div className="space-y-4">
                                {resume.experience?.map((exp) => (
                                    <div
                                        key={exp.id}
                                        className="flex items-start justify-between gap-4 pb-4 border-b border-gray-100 last:border-0 last:pb-0"
                                    >
                                        <div className="flex-1">
                                            <p className="font-medium text-gray-900">
                                                {exp.position}
                                            </p>
                                            <p className="text-sm text-gray-600">
                                                {exp.company}
                                                {exp.location && ` · ${exp.location}`}
                                            </p>
                                            <p className="text-xs text-gray-400 mt-0.5">
                                                {exp.startDate} – {exp.current ? 'Present' : exp.endDate}
                                            </p>
                                            {exp.description && (
                                                <p className="text-sm text-gray-500 mt-1">
                                                    {exp.description}
                                                </p>
                                            )}
                                        </div>
                                        <div className="flex gap-1">
                                            <Button
                                                size="sm"
                                                variant="ghost"
                                                onClick={() => setExperienceModal({
                                                    open: true, data: exp
                                                })}
                                            >
                                                <Edit2 size={14} />
                                            </Button>
                                            <Button
                                                size="sm"
                                                variant="ghost"
                                                onClick={async () => {
                                                    await resumeApi.deleteExperience(exp.id);
                                                    invalidate();
                                                    toast.success('Experience removed');
                                                }}
                                            >
                                                <Trash2 size={14} className="text-red-400" />
                                            </Button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </ResumeSection>

                    {/* Education */}
                    <ResumeSection
                        title="Education"
                        onAdd={() => setEducationModal({ open: true, data: null })}
                    >
                        {resume.education?.length === 0 ? (
                            <p className="text-sm text-gray-400 italic">
                                No education added yet.
                            </p>
                        ) : (
                            <div className="space-y-4">
                                {resume.education?.map((edu) => (
                                    <div
                                        key={edu.id}
                                        className="flex items-start justify-between gap-4 pb-4 border-b border-gray-100 last:border-0 last:pb-0"
                                    >
                                        <div className="flex-1">
                                            <p className="font-medium text-gray-900">
                                                {edu.degree}
                                                {edu.fieldOfStudy && ` in ${edu.fieldOfStudy}`}
                                            </p>
                                            <p className="text-sm text-gray-600">
                                                {edu.institution}
                                            </p>
                                            <p className="text-xs text-gray-400 mt-0.5">
                                                {edu.startDate} – {edu.endDate || 'Present'}
                                            </p>
                                        </div>
                                        <div className="flex gap-1">
                                            <Button
                                                size="sm"
                                                variant="ghost"
                                                onClick={() => setEducationModal({
                                                    open: true, data: edu
                                                })}
                                            >
                                                <Edit2 size={14} />
                                            </Button>
                                            <Button
                                                size="sm"
                                                variant="ghost"
                                                onClick={async () => {
                                                    await resumeApi.deleteEducation(edu.id);
                                                    invalidate();
                                                    toast.success('Education removed');
                                                }}
                                            >
                                                <Trash2 size={14} className="text-red-400" />
                                            </Button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </ResumeSection>

                    {/* Skills */}
                    <ResumeSection
                        title="Skills"
                        onAdd={() => setSkillModal({ open: true, data: null })}
                    >
                        {resume.skills?.length === 0 ? (
                            <p className="text-sm text-gray-400 italic">
                                No skills added yet.
                            </p>
                        ) : (
                            <div className="flex flex-wrap gap-2">
                                {resume.skills?.map((skill) => (
                                    <div
                                        key={skill.id}
                                        className="flex items-center gap-1.5 bg-primary-50 text-primary-700 rounded-full px-3 py-1 text-sm"
                                    >
                                        <span>{skill.displayName}</span>
                                        {skill.skillLevel && (
                                            <span className="text-primary-400 text-xs">
                                                · {skill.skillLevel}
                                            </span>
                                        )}
                                        <button
                                            onClick={async () => {
                                                await resumeApi.deleteSkill(skill.id);
                                                invalidate();
                                                toast.success('Skill removed');
                                            }}
                                            className="text-primary-400 hover:text-primary-600 ml-1"
                                        >
                                            <X size={12} />
                                        </button>
                                    </div>
                                ))}
                            </div>
                        )}
                    </ResumeSection>

                    {/* Languages */}
                    <ResumeSection
                        title="Languages"
                        onAdd={() => setLanguageModal({ open: true, data: null })}
                    >
                        {resume.languages?.length === 0 ? (
                            <p className="text-sm text-gray-400 italic">
                                No languages added yet.
                            </p>
                        ) : (
                            <div className="flex flex-wrap gap-2">
                                {resume.languages?.map((lang) => (
                                    <div
                                        key={lang.id}
                                        className="flex items-center gap-1.5 bg-gray-100 text-gray-700 rounded-full px-3 py-1 text-sm"
                                    >
                                        <span>{lang.languageName}</span>
                                        {lang.languageLevel && (
                                            <span className="text-gray-400 text-xs">
                                                · {lang.languageLevel}
                                            </span>
                                        )}
                                        <button
                                            onClick={async () => {
                                                await resumeApi.deleteLanguage(lang.id);
                                                invalidate();
                                                toast.success('Language removed');
                                            }}
                                            className="text-gray-400 hover:text-gray-600 ml-1"
                                        >
                                            <X size={12} />
                                        </button>
                                    </div>
                                ))}
                            </div>
                        )}
                    </ResumeSection>
                </div>
            </div>

            {/* Education Modal */}
            <EducationModal
                isOpen={educationModal.open}
                data={educationModal.data}
                onClose={() => setEducationModal({ open: false, data: null })}
                onSave={invalidate}
            />

            {/* Experience Modal */}
            <ExperienceModal
                isOpen={experienceModal.open}
                data={experienceModal.data}
                onClose={() => setExperienceModal({ open: false, data: null })}
                onSave={invalidate}
            />

            {/* Skill Modal */}
            <SkillModal
                isOpen={skillModal.open}
                data={skillModal.data}
                onClose={() => setSkillModal({ open: false, data: null })}
                onSave={invalidate}
            />

            {/* Language Modal */}
            <LanguageModal
                isOpen={languageModal.open}
                data={languageModal.data}
                onClose={() => setLanguageModal({ open: false, data: null })}
                onSave={invalidate}
            />
        </DashboardLayout>
    );
}

// ==================== MODALS ====================

function EducationModal({ isOpen, data, onClose, onSave }) {
    const { register, handleSubmit, reset, formState: { errors } } = useForm({
        resolver: zodResolver(educationSchema),
        values: data ? {
            institution: data.institution || '',
            degree: data.degree || '',
            fieldOfStudy: data.fieldOfStudy || '',
            startDate: data.startDate || '',
            endDate: data.endDate || '',
            description: data.description || '',
        } : {
            institution: '',
            degree: '',
            fieldOfStudy: '',
            startDate: '',
            endDate: '',
            description: '',
        },
    });

    const onSubmit = async (formData) => {
        try {
            if (data) {
                await resumeApi.updateEducation(data.id, formData);
                toast.success('Education updated');
            } else {
                await resumeApi.addEducation(formData);
                toast.success('Education added');
            }
            onSave();
            onClose();
            reset();
        } catch (e) {
            toast.error(e.response?.data?.message || 'Failed to save');
        }
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title={data ? 'Edit Education' : 'Add Education'}
            footer={
                <>
                    <Button variant="secondary" onClick={onClose}>Cancel</Button>
                    <Button onClick={handleSubmit(onSubmit)}>Save</Button>
                </>
            }
        >
            <div className="space-y-4">
                <Input
                    label="Institution *"
                    placeholder="University name"
                    error={errors.institution?.message}
                    {...register('institution')}
                />
                <div className="grid grid-cols-2 gap-4">
                    <Input
                        label="Degree"
                        placeholder="Bachelor's"
                        {...register('degree')}
                    />
                    <Input
                        label="Field of Study"
                        placeholder="Computer Science"
                        {...register('fieldOfStudy')}
                    />
                </div>
                <div className="grid grid-cols-2 gap-4">
                    <Input
                        label="Start Date"
                        type="date"
                        {...register('startDate')}
                    />
                    <Input
                        label="End Date"
                        type="date"
                        {...register('endDate')}
                    />
                </div>
                <Textarea
                    label="Description"
                    placeholder="Describe your studies..."
                    {...register('description')}
                />
            </div>
        </Modal>
    );
}

function ExperienceModal({ isOpen, data, onClose, onSave }) {
    const { register, handleSubmit, reset, formState: { errors } } = useForm({
        resolver: zodResolver(experienceSchema),
        values: data ? {
            company: data.company || '',
            position: data.position || '',
            location: data.location || '',
            startDate: data.startDate || '',
            endDate: data.endDate || '',
            description: data.description || '',
        } : {
            company: '',
            position: '',
            location: '',
            startDate: '',
            endDate: '',
            description: '',
        },
    });

    const onSubmit = async (formData) => {
        try {
            if (data) {
                await resumeApi.updateExperience(data.id, formData);
                toast.success('Experience updated');
            } else {
                await resumeApi.addExperience(formData);
                toast.success('Experience added');
            }
            onSave();
            onClose();
            reset();
        } catch (e) {
            toast.error(e.response?.data?.message || 'Failed to save');
        }
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title={data ? 'Edit Experience' : 'Add Experience'}
            footer={
                <>
                    <Button variant="secondary" onClick={onClose}>Cancel</Button>
                    <Button onClick={handleSubmit(onSubmit)}>Save</Button>
                </>
            }
        >
            <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                    <Input
                        label="Company *"
                        placeholder="Company name"
                        error={errors.company?.message}
                        {...register('company')}
                    />
                    <Input
                        label="Position *"
                        placeholder="Job title"
                        error={errors.position?.message}
                        {...register('position')}
                    />
                </div>
                <Input
                    label="Location"
                    placeholder="City, Country"
                    {...register('location')}
                />
                <div className="grid grid-cols-2 gap-4">
                    <Input
                        label="Start Date"
                        type="date"
                        {...register('startDate')}
                    />
                    <Input
                        label="End Date"
                        type="date"
                        {...register('endDate')}
                    />
                </div>
                <Textarea
                    label="Description"
                    placeholder="Describe your responsibilities..."
                    {...register('description')}
                />
            </div>
        </Modal>
    );
}

function SkillModal({ isOpen, data, onClose, onSave }) {
    const { register, handleSubmit, reset, formState: { errors } } = useForm({
        resolver: zodResolver(skillSchema),
        defaultValues: data ? {
            skillName: data.displayName,
            skillLevel: data.skillLevel
        } : {},
    });

    const onSubmit = async (formData) => {
        try {
            if (data) {
                await resumeApi.updateSkill(data.id, formData);
                toast.success('Skill updated');
            } else {
                await resumeApi.addSkill(formData);
                toast.success('Skill added');
            }
            onSave();
            onClose();
            reset();
        } catch (e) {
            toast.error(e.response?.data?.message || 'Failed to save');
        }
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title={data ? 'Edit Skill' : 'Add Skill'}
            footer={
                <>
                    <Button variant="secondary" onClick={onClose}>Cancel</Button>
                    <Button onClick={handleSubmit(onSubmit)}>Save</Button>
                </>
            }
        >
            <div className="space-y-4">
                <Input
                    label="Skill Name *"
                    placeholder="e.g. JavaScript"
                    error={errors.skillName?.message}
                    {...register('skillName')}
                />
                <Select
                    label="Proficiency Level"
                    options={SKILL_LEVELS}
                    placeholder="Select level"
                    {...register('skillLevel')}
                />
            </div>
        </Modal>
    );
}

function LanguageModal({ isOpen, data, onClose, onSave }) {
    const { register, handleSubmit, reset, formState: { errors } } = useForm({
        resolver: zodResolver(languageSchema),
        defaultValues: data ? {
            languageName: data.languageName,
            languageLevel: data.languageLevel
        } : {},
    });

    const onSubmit = async (formData) => {
        try {
            if (data) {
                await resumeApi.updateLanguage(data.id, formData);
                toast.success('Language updated');
            } else {
                await resumeApi.addLanguage(formData);
                toast.success('Language added');
            }
            onSave();
            onClose();
            reset();
        } catch (e) {
            toast.error(e.response?.data?.message || 'Failed to save');
        }
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title={data ? 'Edit Language' : 'Add Language'}
            footer={
                <>
                    <Button variant="secondary" onClick={onClose}>Cancel</Button>
                    <Button onClick={handleSubmit(onSubmit)}>Save</Button>
                </>
            }
        >
            <div className="space-y-4">
                <Input
                    label="Language *"
                    placeholder="e.g. English"
                    error={errors.languageName?.message}
                    {...register('languageName')}
                />
                <Select
                    label="Proficiency Level"
                    options={LANGUAGE_LEVELS}
                    placeholder="Select level"
                    {...register('languageLevel')}
                />
            </div>
        </Modal>
    );
}