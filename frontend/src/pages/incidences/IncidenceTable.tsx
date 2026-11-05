import {
    causeLabels,
    displayDate,
    durationLabel,
    severityLabels,
} from '../../model'

import type { Incidence } from '../../model'

interface IncidenceTableProps {
    readonly rows: Incidence[]
    readonly total: number
    readonly filteredCount: number
    readonly page: number
    readonly lastPage: number
    readonly onPageChange: (page: number) => void
}

export function IncidenceTable({
    rows,
    total,
    filteredCount,
    page,
    lastPage,
    onPageChange,
}: IncidenceTableProps) {
    return (
        <div className="card table-card">
            <div className="table-heading">
                <h2>Estado de la red</h2>
                <span className="badge neutral">
                    {filteredCount} de {total} incidencias
                </span>
            </div>

            {filteredCount === 0 ? (
                <div className="empty">
                    <h3>No hay incidencias para esta consulta</h3>
                    <p>Prueba otros filtros o actualiza el listado.</p>
                </div>
            ) : (
                <>
                    <div className="table-scroll">
                        <table>
                            <thead>
                                <tr>
                                    <th>Incidencia</th>
                                    <th>Red afectada</th>
                                    <th>Gravedad</th>
                                    <th>Duración / retraso</th>
                                    <th>Fecha</th>
                                </tr>
                            </thead>
                            <tbody>
                                {rows.map(row => (
                                    <tr key={row.id}>
                                        <td>
                                            <a className="table-link" href={`#/incidencias/${row.id}`}>
                                                {row.description.name}
                                            </a>
                                            <small>{causeLabels[row.classification.cause]}</small>
                                        </td>
                                        <td>
                                            Mapa {row.affectedNetwork.mapId}
                                            <small>Líneas {row.affectedNetwork.lineIds.join(', ')}</small>
                                        </td>
                                        <td>
                                            <span className={`badge severity-${row.classification.severity}`}>
                                                {severityLabels[row.classification.severity]}
                                            </span>
                                        </td>
                                        <td>{durationLabel(row.occurrence.duration)}</td>
                                        <td>{displayDate(row.occurrence.timestamp)}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    <nav className="pagination" aria-label="Paginación">
                        <button
                            className="button secondary"
                            disabled={page === 0}
                            onClick={() => onPageChange(page - 1)}
                        >
                            Anterior
                        </button>
                        <span>Página {page + 1} de {lastPage + 1}</span>
                        <button
                            className="button secondary"
                            disabled={page === lastPage}
                            onClick={() => onPageChange(page + 1)}
                        >
                            Siguiente
                        </button>
                    </nav>
                </>
            )}
        </div>
    )
}
