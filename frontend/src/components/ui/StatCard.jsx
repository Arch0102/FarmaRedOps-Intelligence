import Card from './Card';

export default function StatCard({ label, value, helper, icon, tone = 'primary', meta }) {
  return (
    <Card className={`stat-card stat-card-${tone}`}>
      <div className="stat-icon">{icon}</div>
      <div className="stat-content">
        <p>{label}</p>
        <strong>{value}</strong>
        {helper && <span>{helper}</span>}
      </div>
      {meta && <small>{meta}</small>}
    </Card>
  );
}
