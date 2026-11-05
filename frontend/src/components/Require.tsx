import { useAuth } from '../hooks/useAuth'

import type { ReactNode } from 'react'

interface RequireProps {
    readonly children: ReactNode
    readonly admin?: boolean
}

/**
 * Guarda de interfaz. La autorización real la aplica siempre el backend;
 * esto solo evita mostrar pantallas que el usuario no podría usar.
 */
export function Require({ children, admin = false }: RequireProps) {
    const { session } = useAuth()

    if (!session) {
        return (
            <div className="card narrow">
                <h1>Inicia sesión</h1>
                <p>Necesitas una cuenta para acceder a esta sección.</p>
                <a className="button primary" href="#/acceso">Iniciar sesión</a>
            </div>
        )
    }

    if (admin && !session.user.roles?.includes('ADMIN')) {
        return (
            <div className="card narrow">
                <h1>Acceso restringido</h1>
                <p>Esta sección requiere el rol de administrador.</p>
                <a className="button secondary" href="#/">Volver al inicio</a>
            </div>
        )
    }

    return children
}
