export default function Select({ label, children, error, className = '', ...props }) {
  return (
    <label className={`field ${className}`}>
      {label && <span>{label}</span>}
      <select {...props}>{children}</select>
      {error && <small className="field-error">{error}</small>}
    </label>
  );
}
