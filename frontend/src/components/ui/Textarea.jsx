import { forwardRef } from 'react';

const Textarea = forwardRef(({
    label,
    error,
    hint,
    className = '',
    rows = 4,
    ...props
}, ref) => {
    return (
        <div className="flex flex-col gap-1">
            {label && (
                <label className="text-sm font-medium text-gray-700">
                    {label}
                </label>
            )}
            <textarea
                ref={ref}
                rows={rows}
                className={`
                    w-full px-3 py-2 border rounded-lg text-sm resize-none
                    focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent
                    disabled:bg-gray-50 disabled:text-gray-500
                    ${error ? 'border-red-500' : 'border-gray-300'}
                    ${className}
                `}
                {...props}
            />
            {error && <p className="text-xs text-red-500">{error}</p>}
            {hint && !error && <p className="text-xs text-gray-500">{hint}</p>}
        </div>
    );
});

Textarea.displayName = 'Textarea';
export default Textarea;