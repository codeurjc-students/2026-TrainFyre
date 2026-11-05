import { api, logout, notify } from '../../api'
import { Field } from '../../components/Field'
import { Form } from '../../components/Form'
import { go } from '../../lib/browser'
import {
    text,
    validatePassword,
    validateUsername,
} from '../../model'

import type { UpdateUser, UserDetails } from '../../model'

interface AccountFormProps {
    readonly id: string
    readonly own: boolean
    readonly user: UserDetails
    readonly onSaved: () => void
}

export function AccountForm({ id, own, user, onSaved }: AccountFormProps) {
    async function submit(data: FormData) {
        const username = validateUsername(text(data, 'username'))
        const password = text(data, 'password')
        const changes: UpdateUser = {}

        if (username !== user.username) changes.username = username
        if (password) changes.password = validatePassword(password)

        if (Object.keys(changes).length === 0) {
            throw new Error('No hay cambios que guardar.')
        }

        await api.updateUser(id, changes)

        if (own && password) {
            logout('Contraseña actualizada. Vuelve a iniciar sesión.')
            go('/acceso')
            return
        }

        notify('Datos de la cuenta actualizados.')
        onSaved()
    }

    return (
        <Form onSubmit={submit}>
            <Field label="Nombre de usuario" hint="Entre 3 y 32 caracteres.">
                <input name="username" required minLength={3} maxLength={32} defaultValue={user.username} autoComplete="username" />
            </Field>
            <Field label="Nueva contraseña" hint="Déjala vacía para conservarla. Si la cambias: entre 12 y 128 caracteres.">
                <input name="password" type="password" minLength={12} maxLength={128} autoComplete="new-password" />
            </Field>
        </Form>
    )
}
