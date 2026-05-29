export default function EmptyState({ title = 'Sin datos', message = 'No hay informacion disponible.', icon = null }) {
  return (
    <div className="empty-state">
      <div className="empty-state-mark">{icon}</div>
      <h3>{title}</h3>
      <p>{message}</p>
    </div>
  );
}
