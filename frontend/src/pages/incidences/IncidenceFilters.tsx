import { Field } from '../../components/Field'
import { causeLabels, severityLabels } from '../../model'

import type { Filters } from '../../model'

interface IncidenceFiltersProps {
    readonly filters: Filters
    readonly onChange: (key: keyof Filters, value: string) => void
    readonly onClear: () => void
}

export function IncidenceFilters({ filters, onChange, onClear }: IncidenceFiltersProps) {
    return (
        <div className="card filters">
            <Field label="Buscar">
                <input
                    type="search"
                    placeholder="Nombre o descripción"
                    value={filters.text}
                    onChange={event => onChange('text', event.target.value)}
                />
            </Field>
            <Field label="ID del mapa">
                <input value={filters.map} onChange={event => onChange('map', event.target.value)} />
            </Field>
            <Field label="ID de línea">
                <input value={filters.line} onChange={event => onChange('line', event.target.value)} />
            </Field>
            <Field label="Gravedad">
                <select value={filters.severity} onChange={event => onChange('severity', event.target.value)}>
                    <option value="">Todas</option>
                    {Object.entries(severityLabels).map(([value, label]) => (
                        <option key={value} value={value}>{label}</option>
                    ))}
                </select>
            </Field>
            <Field label="Causa">
                <select value={filters.cause} onChange={event => onChange('cause', event.target.value)}>
                    <option value="">Todas</option>
                    {Object.entries(causeLabels).map(([value, label]) => (
                        <option key={value} value={value}>{label}</option>
                    ))}
                </select>
            </Field>
            <Field label="Desde">
                <input type="date" value={filters.from} onChange={event => onChange('from', event.target.value)} />
            </Field>
            <Field label="Hasta">
                <input type="date" value={filters.to} onChange={event => onChange('to', event.target.value)} />
            </Field>
            <button className="button secondary" onClick={onClear}>
                Limpiar filtros
            </button>
        </div>
    )
}
