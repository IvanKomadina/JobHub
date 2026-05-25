import { Briefcase } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function Footer() {
    return (
        <footer className="bg-gray-900 text-gray-400 mt-auto">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
                <div className="flex flex-col md:flex-row justify-between items-start gap-8">
                    <div>
                        <div className="flex items-center gap-2 mb-3">
                            <Briefcase className="text-primary-400" size={20} />
                            <span className="text-white font-bold text-lg">JobHub</span>
                        </div>
                        <p className="text-sm max-w-xs">
                            Connecting talented candidates with great employers.
                        </p>
                    </div>
                    <div className="flex gap-12">
                        <div>
                            <h4 className="text-white font-medium mb-3 text-sm">For Candidates</h4>
                            <ul className="space-y-2 text-sm">
                                <li><Link to="/jobs" className="hover:text-white transition-colors">Browse Jobs</Link></li>
                                <li><Link to="/register" className="hover:text-white transition-colors">Create Account</Link></li>
                            </ul>
                        </div>
                        <div>
                            <h4 className="text-white font-medium mb-3 text-sm">For Employers</h4>
                            <ul className="space-y-2 text-sm">
                                <li><Link to="/register" className="hover:text-white transition-colors">Post a Job</Link></li>
                            </ul>
                        </div>
                    </div>
                </div>
                <div className="border-t border-gray-800 mt-8 pt-8 text-sm text-center">
                    © {new Date().getFullYear()} JobHub. All rights reserved.
                </div>
            </div>
        </footer>
    );
}