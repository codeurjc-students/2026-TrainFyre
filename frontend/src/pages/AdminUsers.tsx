import { api, notify } from '../api'
import { Field } from '../components/Field'
import { Form } from '../components/Form'
import { Heading } from '../components/Heading'
import { Notice } from '../components/Notice'
import { go } from '../lib/browser'
import { isUuid, text, validateNewCredentials } from '../model'

export function AdminUsers() {
    return (
        <>
            <Heading title="Administrar usuarios">
                Busca una cuenta por UUID o crea una nueva.
            </Heading>

            <Notice>
                El backend recibido solo autoriza al propietario en UserAccessPolicy.
                La gestión de otras cuentas requiere habilitar también el rol ADMIN en esa política.
            </Notice>

            <div className="detail-grid">
                <section className="card">
                    <h2>Buscar por UUID</h2>
                    <Form label="Abrir cuenta" onSubmit={async data => {
                        const id = text(data, 'id').trim()

                        if (!isUuid(id)) throw new Error('Introduce un UUID válido.')

                        go(`/admin/usuarios/${id}`)
                    }}>
                        <Field label="UUID del usuario">
                            <input name="id" required placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" />
                        </Field>
                    </Form>
                    <p className="muted small">No existe un endpoint para listar todos los usuarios.</p>
                </section>

                <section className="card">
                    <h2>Crear usuario</h2>
                    <Form label="Crear usuario" onSubmit={async data => {
                        const { username, password } = validateNewCredentials(data)
                        const id = await api.register(username, password)

                        const suffix = isUuid(id) ? `: ${id}` : ''

                        notify(`Usuario creado${suffix}. Se mantiene tu sesión de administrador.`)

                        if (isUuid(id)) go(`/admin/usuarios/${id}`)
                    }}>
                        <Field label="Usuario">
                            <input name="username" required minLength={3} maxLength={32} autoComplete="off" />
                        </Field>
                        <Field label="Contraseña">
                            <input name="password" required type="password" minLength={12} maxLength={128} autoComplete="new-password" />
                        </Field>
                        <Field label="Repetir contraseña">
                            <input name="confirmation" required type="password" minLength={12} maxLength={128} autoComplete="new-password" />
                        </Field>
                    </Form>
                    <p className="muted small">El backend asigna USER a las cuentas nuevas.</p>
                </section>
            </div>
        </>
    )
}
