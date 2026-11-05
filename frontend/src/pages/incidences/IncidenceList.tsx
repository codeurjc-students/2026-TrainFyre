import { useState } from 'react'

import { loadIncidences } from '../../api'
import { Heading } from '../../components/Heading'
import { Notice } from '../../components/Notice'
import { Retry } from '../../components/Retry'
import { useIsAdmin } from '../../hooks/useAuth'
import { useResource } from '../../hooks/useResource'
import { LOCAL_ZONE } from '../../lib/browser'
import { emptyFilters, filterIncidences } from '../../model'
import { IncidenceFilters } from './IncidenceFilters'
import { IncidenceTable } from './IncidenceTable'

import type { Filters } from '../../model'

const PAGE_LENGTH = 10

export function IncidenceList() {
    const admin = useIsAdmin()
    const resource = useResource('incidences', loadIncidences)
    const [filters, setFilters] = useState<Filters>({ ...emptyFilters })
    const [page, setPage] = useState(0)

    const filtered = filterIncidences(resource.data ?? [], filters)
    const lastPage = Math.max(0, Math.ceil(filtered.length / PAGE_LENGTH) - 1)
    const currentPage = Math.min(page, lastPage)
    const visible = filtered.slice(currentPage * PAGE_LENGTH, (currentPage + 1) * PAGE_LENGTH)

    function change(key: keyof Filters, value: string) {
        setFilters(previous => ({ ...previous, [key]: value }))
        setPage(0)
    }

    function clear() {
        setFilters({ ...emptyFilters })
        setPage(0)
    }

    function results() {
        if (resource.loading) {
            return <div className="card" role="status">Cargando todas las páginas de incidencias…</div>
        }

        if (resource.error) {
            return <Retry error={resource.error} retry={resource.reload} />
        }

        return (
            <IncidenceTable
                rows={visible}
                total={resource.data?.length ?? 0}
                filteredCount={filtered.length}
                page={currentPage}
                lastPage={lastPage}
                onPageChange={setPage}
            />
        )
    }

    return (
        <>
            <Heading
                title="Incidencias"
                actions={
                    <>
                        <button className="button secondary" onClick={resource.reload}>
                            Actualizar
                        </button>
                        {admin && (
                            <a className="button primary" href="#/incidencias/nueva">
                                Nueva incidencia
                            </a>
                        )}
                    </>
                }
            >
                Consulta el estado de la red. Fechas mostradas en {LOCAL_ZONE}.
            </Heading>

            <IncidenceFilters filters={filters} onChange={change} onClear={clear} />

            {filters.from && filters.to && filters.from > filters.to && (
                <Notice error>La fecha inicial es posterior a la final.</Notice>
            )}

            {results()}

            <p className="muted small">
                MVP: se consultan todas las páginas para aplicar filtros globales en el navegador.
                Los catálogos de mapas y líneas todavía no están disponibles.
            </p>
        </>
    )
}
