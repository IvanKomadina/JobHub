import { ChevronLeft, ChevronRight } from 'lucide-react';
import Button from './Button';

export default function Pagination({ page, totalPages, onPageChange }) {
    if (totalPages <= 1) return null;

    return (
        <div className="flex items-center justify-center gap-2 mt-8">
            <Button
                variant="secondary"
                size="sm"
                onClick={() => onPageChange(page - 1)}
                disabled={page === 0}
            >
                <ChevronLeft size={16} />
            </Button>

            <span className="text-sm text-gray-600">
                Page {page + 1} of {totalPages}
            </span>

            <Button
                variant="secondary"
                size="sm"
                onClick={() => onPageChange(page + 1)}
                disabled={page >= totalPages - 1}
            >
                <ChevronRight size={16} />
            </Button>
        </div>
    );
}