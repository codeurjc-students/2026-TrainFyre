import { useState } from 'react'

import { api, notify } from '../../api'
import { Field } from '../../components/Field'
import { Form } from '../../components/Form'
import { Notice } from '../../components/Notice'
import { go, LOCAL_ZONE } from '../../lib/browser'
import {
    causeLabels,
    durationSeconds,
    isUuid,
    localInput,
    severityLabels,
} from '../../model'
import { buildIncidencePayload } from './payload'

import type { Incidence, Severity } from '../../model'

interface IncidenceEditorProps {
    readonly initial?: Incidence
}

function defaultsOf(initial?: Incidence) {
    return {
        name: initial?.description.name ?? '',
        summary: initial?.description.summary ?? '',
        mapId: initial?.affectedNetwork.mapId ?? '',
        lineIds: initial?.affectedNetwork.lineIds.join(', ') ?? '',
        cause: initial?.classification.cause ?? 'OTHER',
        duration: initial?.occurrence.duration ?? 'PT15M',
        timestamp: localInput(initial?.occurrence.timestamp ?? new Date().toISOString()),
    }
}

export function IncidenceEditor({ initial }: IncidenceEditorProps) {
    // Se calculan una sola vez: un renderizado posterior no debe alterar el formulario.
    const [defaults] = useState(() => defaultsOf(initial))
    const [severity, setSeverity] = useState<Severity>(
        initial?.classification.severity ?? 'LOW',
    )
    const [instant, setInstant] = useState(
        initial ? durationSeconds(initial.occurrence.duration) === 0 : false,
    )
    const informational = severity === 'INFORMATIONAL'

    async function submit(data: FormData) {
        const payload = buildIncidencePayload(data, { initial, severity, instant })

        if (initial) {
            await api.updateIncidence(initial.id, payload)
            notify('Incidencia actualizada.')
            go(`/incidencias/${initial.id}`)
            return
        }

        const id = await api.createIncidence(payload)

        notify('Incidencia creada.')
        go(isUuid(id) ? `/incidencias/${id}` : '/incidencias')
    }

    return (
        <div className="card">
            <Form label={initial ? 'Guardar incidencia' : 'Crear incidencia'} onSubmit={submit}>
                <Field label="Nombre">
                    <input name="name" required defaultValue={defaults.name} />
                </Field>

                <Field label="Descripción">
                    <textarea name="summary" required rows={5} defaultValue={defaults.summary} />
                </Field>

                <div className="form-grid">
                    <Field label="ID del mapa">
                        <input name="mapId" required defaultValue={defaults.mapId} />
                    </Field>
                    <Field label="IDs de líneas" hint="Separados por comas. Ejemplo: 1, 4, 7.">
                        <input name="lineIds" required defaultValue={defaults.lineIds} />
                    </Field>
                    <Field label="Gravedad">
                        <select
                            value={severity}
                            onChange={event => {
                                const value = event.target.value as Severity

                                setSeverity(value)
                                if (value === 'INFORMATIONAL') setInstant(true)
                            }}
                        >
                            {Object.entries(severityLabels).map(([value, label]) => (
                                <option key={value} value={value}>{label}</option>
                            ))}
                        </select>
                    </Field>
                    <Field label="Causa">
                        <select name="cause" defaultValue={defaults.cause}>
                            {Object.entries(causeLabels).map(([value, label]) => (
                                <option key={value} value={value}>{label}</option>
                            ))}
                        </select>
                    </Field>
                    <Field label="Fecha y hora" hint={`Zona horaria del navegador: ${LOCAL_ZONE}.`}>
                        <input
                            name="timestamp"
                            type="datetime-local"
                            step="1"
                            required
                            defaultValue={defaults.timestamp}
                        />
                    </Field>
                    <Field label="Duración ISO 8601" hint="PT15M = 15 minutos; PT1H30M = 1 hora y 30 minutos.">
                        <input
                            name="duration"
                            required={!instant && !informational}
                            disabled={instant || informational}
                            defaultValue={defaults.duration}
                        />
                    </Field>
                </div>

                <label className="check">
                    <input
                        type="checkbox"
                        checked={instant}
                        disabled={informational}
                        onChange={event => setInstant(event.target.checked)}
                    />
                    Sin retraso / instantánea
                </label>

                {informational && (
                    <Notice>Las incidencias informativas se guardan con duración PT0S.</Notice>
                )}

                <p className="muted small">
                    Para incidencias pasadas, indica cuánto duraron; para futuras, el retraso previsto.
                </p>
            </Form>
        </div>
    )
}
