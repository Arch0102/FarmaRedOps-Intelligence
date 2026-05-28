import Card from './Card';

export default function StatCard({ label, value, helper, icon }) {
  return (
    <Card className="stat-card">
      <div className="stat-icon">{icon}</div>
      <div>
        <p>{label}</p>
        <strong>{value}</strong>
        {helper && <span>{helper}</span>}
      </div>
    </Card>
  );
}
