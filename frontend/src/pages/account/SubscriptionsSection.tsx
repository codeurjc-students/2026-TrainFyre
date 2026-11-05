import { useRef, useState } from 'react'

import { api, notify } from '../../api'
import { Action } from '../../components/Action'
import {
    clock,
    dayLabels,
    displayDate,
    sameSubscription,
    subscriptionKey,
    subscriptionPatch,
} from '../../model'
import { SubscriptionEditor } from './SubscriptionEditor'

import type { Subscription, UserDetails } from '../../model'

interface SubscriptionsSectionProps {
    readonly id: string
    readonly user: UserDetails
    readonly onChanged: () => void
}

/** Asigna claves únicas y estables aunque el backend devuelva suscripciones repetidas. */
function withKeys(subscriptions: Subscription[]) {
    const counts = new Map<string, number>()

    return subscriptions.map(subscription => {
        const base = subscriptionKey(subscription)
        const occurrence = counts.get(base) ?? 0

        counts.set(base, occurrence + 1)

        return { key: `${base}#${occurrence}`, subscription }
    })
}

export function SubscriptionsSection({ id, user, onChanged }: SubscriptionsSectionProps) {
    const [editing, setEditing] = useState<Subscription>()
    const editor = useRef<HTMLDivElement>(null)
    const keyed = withKeys(user.alertSubscriptions)

    function edit(subscription: Subscription) {
        setEditing(subscription)
        editor.current?.scrollIntoView({ behavior: 'smooth' })
    }

    async function remove(subscription: Subscription) {
        if (!globalThis.confirm('¿Eliminar esta suscripción?')) return

        await api.updateUser(id, {
            // Se devuelve exactamente el objeto recibido.
            alertSubscriptionListToDelete: [subscription],
        })

        setEditing(undefined)
        notify('Alerta eliminada.')
        onChanged()
    }

    async function save(next: Subscription) {
        const changes = subscriptionPatch(editing, next)

        if (!changes) {
            notify('La alerta no tiene cambios.')
            return
        }

        if (user.alertSubscriptions.some(
            item => item !== editing && sameSubscription(item, next),
        )) {
            throw new Error('Ya existe una suscripción idéntica.')
        }

        // Edición en una única petición: añadir el nuevo valor y borrar el original.
        await api.updateUser(id, changes)
        setEditing(undefined)
        notify('Alerta guardada. El envío de notificaciones sigue en fase de prueba.')
        onChanged()
    }

    return (
        <section className="card">
            <div className="table-heading">
                <h2>Suscripciones ({user.alertSubscriptions.length})</h2>
                {editing && (
                    <button className="button secondary" onClick={() => setEditing(undefined)}>
                        Cancelar edición
                    </button>
                )}
            </div>

            {user.alertSubscriptions.length === 0 && (
                <p className="muted">Todavía no hay alertas configuradas.</p>
            )}

            <div className="subscription-grid">
                {keyed.map(({ key, subscription }) => (
                    <article className="subscription" key={key}>
                        <h3>Líneas {subscription.lines.join(', ')}</h3>
                        <p>{clock(subscription.startTime)} – {clock(subscription.endTime)}</p>
                        <p>{subscription.daysOfTheWeek.map(day => dayLabels[day]).join(', ')}</p>
                        <p className="muted small">Expira: {displayDate(subscription.expiration)}</p>
                        <div className="actions">
                            <button className="button secondary" onClick={() => edit(subscription)}>
                                Editar
                            </button>
                            <Action danger onClick={() => remove(subscription)}>
                                Eliminar
                            </Action>
                        </div>
                    </article>
                ))}
            </div>

            <div className="editor-section" id="alert-editor" ref={editor}>
                <h2>{editing ? 'Editar alerta' : 'Nueva alerta'}</h2>
                <SubscriptionEditor
                    key={editing ? subscriptionKey(editing) : 'new'}
                    initial={editing}
                    onSave={save}
                />
            </div>
        </section>
    )
}
