import { useState } from 'react'

import { api, login } from '../api'
import { Field } from '../components/Field'
import { Form } from '../components/Form'
import { Notice } from '../components/Notice'
import { useAuth } from '../hooks/useAuth'
import { go } from '../lib/browser'
import { messageOf, text, validateNewCredentials } from '../model'

interface AccessProps {
    readonly register?: boolean
}

async function signIn(data: FormData, justCreated: boolean) {
    try {
        await login(text(data, 'username'), text(data, 'password'))
        go('/cuenta')
    } catch (error) {
        throw justCreated
            ? new Error(
                `Cuenta creada, pero no se pudo iniciar sesión. ${messageOf(error)}`,
                { cause: error },
            )
            : error
    }
}

interface AccessFieldsProps {
    readonly register: boolean
    readonly creating: boolean
}

function AccessFields({ register, creating }: AccessFieldsProps) {
    return (
        <>
        <Field
            label="Usuario"
            hint={register ? 'Entre 3 y 32 caracteres: letras, números, punto, guion o guion bajo.' : undefined}
        >
            <input
                name="username"
                required
                minLength={creating ? 3 : 1}
                maxLength={32}
                autoComplete="username"
                autoCapitalize="none"
                spellCheck={false}
            />
        </Field>

        <Field
            label="Contraseña"
            hint={creating ? 'Entre 12 y 128 caracteres.' : undefined}
        >
            <input
                name="password"
                type="password"
                required
                minLength={creating ? 12 : 1}
                maxLength={128}
                autoComplete={creating ? 'new-password' : 'current-password'}
            />
        </Field>

        {creating && (
            <Field label="Repetir contraseña">
                <input
                    name="confirmation"
                    type="password"
                    required
                    minLength={12}
                    maxLength={128}
                    autoComplete="new-password"
                />
            </Field>
        )}
        </>
    )
}

function AlreadySignedIn({ username }: { readonly username: string }) {
    return (
        <div className="card narrow">
            <h1>Ya has iniciado sesión</h1>
            <p>Estás conectado como {username}.</p>
            <a className="button primary" href="#/cuenta">Ir a mi cuenta</a>
        </div>
    )
}

export function Access({ register = false }: AccessProps) {
    const { session } = useAuth()
    const [created, setCreated] = useState(false)
    const creating = register && !created

    if (session) return <AlreadySignedIn username={session.user.username} />

    async function submit(data: FormData) {
        let justCreated = false

        if (creating) {
            const { username, password } = validateNewCredentials(data)

            await api.register(username, password)
            setCreated(true)
            justCreated = true
        }

        await signIn(data, justCreated)
    }

    return (
        <section className="auth-layout">
            <div className="auth-intro">
                <span className="eyebrow">BIENVENIDO A TRAINFYRE</span>
                <h1>{register ? 'Tu próximo viaje empieza aquí.' : 'Qué bueno verte de nuevo.'}</h1>
                <p>Gestiona tu cuenta y configura tus preferencias de incidencias.</p>
                <p className="muted">
                    La sesión se mantiene únicamente mientras esta página permanezca abierta.
                    Al recargar o caducar tendrás que identificarte nuevamente.
                </p>
            </div>

            <div className="card">
                <h2>{register ? 'Crear una cuenta' : 'Iniciar sesión'}</h2>

                {created && (
                    <Notice>
                        Tu cuenta ya está creada. Puedes volver a intentar el inicio de sesión
                        sin repetir el registro.
                    </Notice>
                )}

                <Form
                    label={creating ? 'Crear cuenta y entrar' : 'Iniciar sesión'}
                    onSubmit={submit}
                >
                    <AccessFields register={register} creating={creating} />
                </Form>

                <p className="muted">
                    {register ? '¿Ya tienes una cuenta? ' : '¿Todavía no tienes cuenta? '}
                    <a href={register ? '#/acceso' : '#/registro'}>
                        {register ? 'Inicia sesión' : 'Regístrate'}
                    </a>
                </p>
            </div>
        </section>
    )
}
