import { logout } from '../api'
import { useAuth, useIsAdmin } from '../hooks/useAuth'
import { useAvatar } from '../hooks/useAvatar'
import { go } from '../lib/browser'
import { Avatar } from './Avatar'

interface SiteHeaderProps {
    readonly path: string
}

function HeaderAvatar() {
    const { url } = useAvatar('me')

    return <Avatar url={url} />
}

export function SiteHeader({ path }: SiteHeaderProps) {
    const { session } = useAuth()
    const admin = useIsAdmin()

    return (
        <header className="site-header">
            <div className="header-inner">
                <a className="brand" href="#/" aria-label="TrainFyre, inicio">
                    <img src="/logo.jpg" alt="" />
                    <span>Train<span>Fyre</span></span>
                </a>

                <nav aria-label="Navegación principal">
                    <a href="#/" aria-current={path === '/' ? 'page' : undefined}>Inicio</a>
                    <a href="#/incidencias" aria-current={path.startsWith('/incidencias') ? 'page' : undefined}>
                        Incidencias
                    </a>
                    {admin && (
                        <a href="#/admin/usuarios" aria-current={path.startsWith('/admin') ? 'page' : undefined}>
                            Administración
                        </a>
                    )}
                </nav>

                <div className="account-nav">
                    {session ? (
                        <>
                            {/* La clave reinicia la carga del avatar al cambiar de usuario. */}
                            <HeaderAvatar key={session.id} />
                            <a className="user-link" href="#/cuenta">{session.user.username}</a>
                            <button className="button secondary compact" onClick={() => {
                                logout()
                                go('/')
                            }}>
                                Salir
                            </button>
                        </>
                    ) : (
                        <a className="button primary compact" href="#/acceso">Acceder</a>
                    )}
                </div>
            </div>
        </header>
    )
}
