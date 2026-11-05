import { useGuardedTask } from '../hooks/useGuardedTask'
import { Notice } from './Notice'

import type { FormEvent, ReactNode } from 'react'

interface FormProps {
    readonly children: ReactNode
    readonly label?: string
    readonly onSubmit: (data: FormData) => Promise<void>
}

export function Form({ children, label = 'Guardar cambios', onSubmit }: FormProps) {
    const { busy, error, run } = useGuardedTask()

    function submit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()

        // Los datos se leen de forma síncrona: tras un `await` el formulario ya no es accesible.
        const data = new FormData(event.currentTarget)

        run(() => onSubmit(data))
    }

    return (
        <form onSubmit={submit}>
            <fieldset disabled={busy}>
                {children}
                <button className="button primary" type="submit">
                    {busy ? 'Procesando…' : label}
                </button>
            </fieldset>
            <Notice error>{error}</Notice>
        </form>
    )
}
