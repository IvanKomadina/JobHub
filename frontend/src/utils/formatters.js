export const formatSalary = (min, max) => {
    if (!min && !max) return 'Not specified';
    if (min && max) return `€${min.toLocaleString()} - €${max.toLocaleString()}`;
    if (min) return `From €${min.toLocaleString()}`;
    return `Up to €${max.toLocaleString()}`;
};

export const formatDate = (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('en-GB', {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
    });
};

export const formatEmploymentType = (type) => {
    const map = {
        FULL_TIME: 'Full Time',
        PART_TIME: 'Part Time',
        CONTRACT: 'Contract',
        INTERNSHIP: 'Internship',
        STUDENT: 'Student',
    };
    return map[type] || type;
};

export const formatApplicationStatus = (status) => {
    const map = {
        DRAFT: 'Draft',
        PENDING: 'Pending',
        ACCEPTED: 'Accepted',
        REJECTED: 'Rejected',
        WITHDRAWN: 'Withdrawn',
    };
    return map[status] || status;
};

export const formatRecommendation = (rec) => {
    const map = {
        RECOMMENDED: 'Recommended',
        CONSIDER: 'Consider',
        NOT_RECOMMENDED: 'Not Recommended',
    };
    return map[rec] || rec;
};