export default function Card({ children, className = '', onClick }) {
    return (
        <div
            className={`
                bg-white rounded-xl border border-gray-200 shadow-sm
                ${onClick ? 'cursor-pointer hover:shadow-md transition-shadow' : ''}
                ${className}
            `}
            onClick={onClick}
        >
            {children}
        </div>
    );
}