import EmptyState from './EmptyState';

export default function Table({
  columns,
  rows,
  getRowId = (row) => row.id,
  emptyTitle = 'Sin datos',
  emptyMessage = 'No hay registros para mostrar.',
  className = '',
  compact = false,
}) {
  if (!rows || rows.length === 0) {
    return <EmptyState title={emptyTitle} message={emptyMessage} />;
  }

  return (
    <div className={`table-wrap ${className}`}>
      <table className={`data-table ${compact ? 'data-table-compact' : ''}`}>
        <thead>
          <tr>
            {columns.map((column) => (
              <th key={column.key} className={column.align ? `cell-${column.align}` : undefined}>
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={getRowId(row)}>
              {columns.map((column) => (
                <td key={column.key} className={column.align ? `cell-${column.align}` : undefined}>
                  {column.render ? column.render(row) : row[column.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
