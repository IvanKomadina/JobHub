import { Routes, Route, Navigate } from 'react-router-dom';
import PrivateRoute from './PrivateRoute';
import RoleRoute from './RoleRoute';

// Public pages
import HomePage from '../pages/public/HomePage';
import JobListPage from '../pages/public/JobListPage';
import JobDetailPage from '../pages/public/JobDetailPage';

// Auth pages
import LoginPage from '../pages/auth/LoginPage';
import RegisterPage from '../pages/auth/RegisterPage';

// Candidate pages
import CandidateDashboard from '../pages/candidate/CandidateDashboard';
import CandidateApplicationsPage from '../pages/candidate/CandidateApplicationsPage';
import CandidateResumePage from '../pages/candidate/CandidateResumePage';
import CandidateFavoritesPage from '../pages/candidate/CandidateFavoritesPage';
import ApplyPage from '../pages/candidate/ApplyPage';

// Employer pages
import EmployerDashboard from '../pages/employer/EmployerDashboard';
import EmployerPostsPage from '../pages/employer/EmployerPostsPage';
import EmployerApplicationsPage from '../pages/employer/EmployerApplicationsPage';

// Admin pages
import AdminDashboard from '../pages/admin/AdminDashboard';
import AdminUsersPage from '../pages/admin/AdminUsersPage';
import AdminPostsPage from '../pages/admin/AdminPostsPage';

export default function AppRoutes() {
    return (
        <Routes>
            {/* Public */}
            <Route path="/" element={<HomePage />} />
            <Route path="/jobs" element={<JobListPage />} />
            <Route path="/jobs/:id" element={<JobDetailPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/* Candidate */}
            <Route element={<PrivateRoute />}>
                <Route element={<RoleRoute role="CANDIDATE" />}>
                    <Route path="/candidate/dashboard" element={<CandidateDashboard />} />
                    <Route path="/candidate/applications" element={<CandidateApplicationsPage />} />
                    <Route path="/candidate/resume" element={<CandidateResumePage />} />
                    <Route path="/candidate/favorites" element={<CandidateFavoritesPage />} />
                    <Route path="/candidate/applications/new" element={<ApplyPage />} />
                </Route>
            </Route>

            {/* Employer */}
            <Route element={<PrivateRoute />}>
                <Route element={<RoleRoute role="EMPLOYER" />}>
                    <Route path="/employer/dashboard" element={<EmployerDashboard />} />
                    <Route path="/employer/posts" element={<EmployerPostsPage />} />
                    <Route path="/employer/posts/:postId/applications" element={<EmployerApplicationsPage />} />
                </Route>
            </Route>

            {/* Admin */}
            <Route element={<PrivateRoute />}>
                <Route element={<RoleRoute role="ADMINISTRATOR" />}>
                    <Route path="/admin/dashboard" element={<AdminDashboard />} />
                    <Route path="/admin/users" element={<AdminUsersPage />} />
                    <Route path="/admin/posts" element={<AdminPostsPage />} />
                </Route>
            </Route>

            {/* Fallback */}
            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
}