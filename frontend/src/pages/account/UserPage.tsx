import { api, updateCurrentUser } from '../../api'
import { Heading } from '../../components/Heading'
import { Notice } from '../../components/Notice'
import { Retry } from '../../components/Retry'
import { useAuth } from '../../hooks/useAuth'
import { useResource } from '../../hooks/useResource'
import { AccountForm } from './AccountForm'
import { AvatarSection } from './AvatarSection'
import { DeleteAccount } from './DeleteAccount'
import { SubscriptionsSection } from './SubscriptionsSection'

import type { UserDetails } from '../../model'

interface UserPageProps {
    readonly id?: string
}

function rolesLabel(user: UserDetails, own: boolean, ownRoles?: string[]): string {
    const roles = user.roles?.join(', ')

    if (roles) return roles

    return own ? (ownRoles?.join(', ') ?? '') : 'No proporcionados por el backend'
}

export function UserPage({ id = 'me' }: UserPageProps) {
    const { session } = useAuth()
    const own = id === 'me' || id === session?.id
    const targetId = id === 'me' ? session?.id ?? '' : id

    const resource = useResource(
        `user:${id}:${session?.id}`,
        async signal => {
            const user = await api.user(id, signal)

            if (!signal.aborted && own) updateCurrentUser(targetId, user)

            return user
        },
    )

    if (resource.loading) return <p role="status">Cargando cuenta…</p>
    if (resource.error) return <Retry error={resource.error} retry={resource.reload} />
    if (!resource.data) return null

    const user = resource.data

    return (
        <>
            <Heading title={own ? 'Mi cuenta' : `Cuenta de ${user.username}`}>
                Gestiona los datos de acceso y las suscripciones.
            </Heading>

            <div className="detail-grid">
                <section className="card">
                    <h2>Datos de acceso</h2>
                    <p className="muted small">UUID: {targetId}</p>
                    <p className="muted small">
                        Roles: {rolesLabel(user, own, session?.user.roles)}
                    </p>

                    <AccountForm id={id} own={own} user={user} onSaved={resource.reload} />
                </section>

                <aside className="card">
                    <span className="badge">MVP</span>
                    <h2>Alertas en fase de prueba</h2>
                    <p>Las reglas se guardan realmente en el backend.</p>
                    <p>
                        El envío de correo y avisos al móvil todavía no está implementado.
                        Guardar una alerta no significa que vayas a recibir notificaciones.
                    </p>
                    <Notice>
                        La API aún no incluye el mapa en las alertas. Si dos mapas usan el
                        mismo ID de línea, la regla no puede distinguirlos.
                    </Notice>
                    <p className="muted small">Los roles se muestran únicamente para consulta: la API no permite modificarlos.</p>
                </aside>
            </div>

            <AvatarSection id={id} />

            <SubscriptionsSection id={id} user={user} onChanged={resource.reload} />

            <DeleteAccount username={user.username} targetId={targetId} own={own} />
        </>
    )
}
