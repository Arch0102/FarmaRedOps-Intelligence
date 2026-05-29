export default function Badge({ children, tone = 'neutral', size = 'md', className = '', title }) {
  return (
    <span className={`badge badge-${tone} badge-${size} ${className}`} title={title}>
      {children}
    </span>
  );
}
