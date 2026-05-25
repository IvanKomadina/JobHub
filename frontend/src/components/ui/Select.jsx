import { forwardRef } from 'react';

const Select = forwardRef(({
    label,
    error,
    options = [],
    placeholder = 'Select...',
    className = '',
    ...props
}, ref) => {
    return (
        <div className="flex flex-col gap-1">
            {label && (
                <label className="text-sm font-medium text-gray-700">
                    {label}
                </label>
            )}
            <select
                ref={ref}
                className={`
                    w-full px-3 py-2 border rounded-lg text-sm bg-white
                    focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent
                    disabled:bg-gray-50 disabled:text-gray-500
                    ${error ? 'border-red-500' : 'border-gray-300'}
                    ${className}
                `}
                {...props}
            >
                <option value="">{placeholder}</option>
                {options.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                        {opt.label}
                    </option>
                ))}
            </select>
            {error && <p className="text-xs text-red-500">{error}</p>}
        </div>
    );
});

Select.displayName = 'Select';
export default Select;