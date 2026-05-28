export default function Loading({ text = 'Cargando informacion...' }) {
  return (
    <div className="loading-state">
      <span className="loader" />
      <p>{text}</p>
    </div>
  );
}
