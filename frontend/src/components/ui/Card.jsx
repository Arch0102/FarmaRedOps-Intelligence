export default function Card({ children, className = '' }) {
  return <section className={`card ${className}`}>{children}</section>;
}

export function CardHeader({ title, subtitle, action, meta }) {
  return (
    <div className="card-header">
      <div>
        <h2>{title}</h2>
        {subtitle && <p>{subtitle}</p>}
      </div>
      <div className="card-header-side">
        {meta && <span className="card-meta">{meta}</span>}
        {action}
      </div>
    </div>
  );
}
