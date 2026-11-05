import { Field } from '../../components/Field'
import { Form } from '../../components/Form'
import { Notice } from '../../components/Notice'
import { LOCAL_ZONE } from '../../lib/browser'
import {
    clock,
    dayLabels,
    localInput,
    makeSubscription,
} from '../../model'

import type { Day, Subscription } from '../../model'

interface SubscriptionEditorProps {
    readonly initial?: Subscription
    readonly onSave: (next: Subscription) => Promise<void>
}

function isChecked(day: Day, initial?: Subscription): boolean {
    return initial
        ? initial.daysOfTheWeek.includes(day)
        : day !== 'SATURDAY' && day !== 'SUNDAY'
}

export function SubscriptionEditor({ initial, onSave }: SubscriptionEditorProps) {
    return (
        <Form
            label={initial ? 'Guardar alerta' : 'Añadir alerta'}
            onSubmit={async data => onSave(makeSubscription(data, initial))}
        >
            <Field label="IDs de líneas" hint="Separados por comas. Por ahora la API no permite indicar el mapa.">
                <input name="lines" required defaultValue={initial?.lines.join(', ') ?? ''} />
            </Field>

            <div className="form-grid">
                <Field label="Desde las">
                    <input name="startTime" type="time" step="1" required defaultValue={initial ? clock(initial.startTime) : '07:00:00'} />
                </Field>
                <Field label="Hasta las">
                    <input name="endTime" type="time" step="1" required defaultValue={initial ? clock(initial.endTime) : '09:00:00'} />
                </Field>
            </div>

            <fieldset className="days">
                <legend>Días de la semana</legend>
                {(Object.entries(dayLabels) as [Day, string][]).map(([day, label]) => (
                    <label className="check" key={day}>
                        <input
                            name="days"
                            type="checkbox"
                            value={day}
                            defaultChecked={isChecked(day, initial)}
                        />
                        {label}
                    </label>
                ))}
            </fieldset>

            <Field
                label="Expiración opcional"
                hint={`Si la dejas vacía, se enviará una expiración de 30 días desde el guardado. Se muestra en ${LOCAL_ZONE}.`}
            >
                <input
                    name="expiration"
                    type="datetime-local"
                    step="1"
                    defaultValue={initial ? localInput(initial.expiration) : ''}
                />
            </Field>

            <Notice>
                El backend compara las horas literales, sin convertir zonas horarias.
                Utiliza la misma referencia horaria con la que se generan las incidencias.
                Las franjas no pueden cruzar medianoche.
            </Notice>
        </Form>
    )
}
