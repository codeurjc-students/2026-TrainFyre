import { api, logout, notify } from '../../api'
import { Action } from '../../components/Action'
import { go } from '../../lib/browser'

interface DeleteAccountProps {
    readonly username: string
    readonly targetId: string
    readonly own: boolean
}

export function DeleteAccount({ username, targetId, own }: DeleteAccountProps) {
    async function remove() {
        if (!globalThis.confirm(`¿Eliminar permanentemente la cuenta "${username}"?`)) return

        await api.deleteUser(targetId)

        if (own) {
            logout('Tu cuenta se ha eliminado.')
            go('/')
        } else {
            notify('Cuenta eliminada.')
            go('/admin/usuarios')
        }
    }

    return (
        <section className="card danger-zone">
            <h2>Eliminar cuenta</h2>
            <p>
                Se eliminará la cuenta y sus suscripciones. Esta acción no se puede deshacer.
            </p>
            <Action danger onClick={remove}>
                {own ? 'Eliminar mi cuenta' : 'Eliminar esta cuenta'}
            </Action>
        </section>
    )
}
