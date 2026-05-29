export default function Button({
  children,
  variant = 'primary',
  size = 'md',
  className = '',
  type: buttonType = 'button',
  ...props
}) {
  return (
    <button type={buttonType} className={`btn btn-${variant} btn-${size} ${className}`} {...props}>
      {children}
    </button>
  );
}
