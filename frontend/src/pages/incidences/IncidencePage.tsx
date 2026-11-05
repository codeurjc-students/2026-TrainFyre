import { api, loadIncidence, notify } from '../../api'
import { Action } from '../../components/Action'
import { Heading } from '../../components/Heading'
import { Retry } from '../../components/Retry'
import { useIsAdmin } from '../../hooks/useAuth'
import { useResource } from '../../hooks/useResource'
import { go } from '../../lib/browser'
import {
    causeLabels,
    displayDate,
    durationLabel,
    severityLabels,
} from '../../model'
import { IncidenceEditor } from './IncidenceEditor'

import type { Incidence } from '../../model'

interface IncidencePageProps {
    readonly id: string
    readonly edit?: boolean
}

function IncidenceDetail({ item }: { readonly item: Incidence }) {
    return (
        <div className="detail-grid">
            <article className="card">
                <span className={`badge severity-${item.classification.severity}`}>
                    {severityLabels[item.classification.severity]}
                </span>
                <h2>Qué está ocurriendo</h2>
                <p className="preserve">{item.description.summary}</p>
                <dl>
                    <dt>Causa</dt><dd>{causeLabels[item.classification.cause]}</dd>
                    <dt>Fecha</dt><dd>{displayDate(item.occurrence.timestamp)}</dd>
                    <dt>Duración / retraso previsto</dt><dd>{durationLabel(item.occurrence.duration)}</dd>
                </dl>
            </article>
            <aside className="card">
                <h2>Red afectada</h2>
                <p className="map-title">Mapa {item.affectedNetwork.mapId}</p>
                <div className="actions">
                    {item.affectedNetwork.lineIds.map(line => (
                        <span className="line-badge" key={line}>Línea {line}</span>
                    ))}
                </div>
                <p className="muted small">Los nombres de mapas y líneas estarán disponibles cuando exista su catálogo.</p>
            </aside>
        </div>
    )
}

export function IncidencePage({ id, edit = false }: IncidencePageProps) {
    const admin = useIsAdmin()
    const resource = useResource(
        `incidence:${id}:${admin}`,
        signal => loadIncidence(id, admin, signal),
    )

    if (resource.loading) return <p role="status">Cargando incidencia…</p>
    if (resource.error) return <Retry error={resource.error} retry={resource.reload} />
    if (!resource.data) return null

    const item = resource.data

    async function remove() {
        if (!globalThis.confirm(`¿Eliminar "${item.description.name}"? Esta acción no se puede deshacer.`)) return

        await api.deleteIncidence(id)
        notify('Incidencia eliminada.')
        go('/incidencias')
    }

    return (
        <>
            <a className="back-link" href="#/incidencias">← Volver a incidencias</a>

            <Heading
                title={edit ? 'Editar incidencia' : item.description.name}
                actions={!edit && admin ? (
                    <>
                        <a className="button primary" href={`#/incidencias/${id}/editar`}>Editar</a>
                        <Action danger onClick={remove}>Eliminar</Action>
                    </>
                ) : undefined}
            >
                {edit ? 'Actualiza los datos publicados.' : `Identificador: ${id}`}
            </Heading>

            {edit ? <IncidenceEditor initial={item} /> : <IncidenceDetail item={item} />}
        </>
    )
}
