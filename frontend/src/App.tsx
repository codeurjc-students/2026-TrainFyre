import { useEffect, useRef } from 'react'

import { checkSession, notify } from './api'
import { Notice } from './components/Notice'
import { SiteFooter } from './components/SiteFooter'
import { SiteHeader } from './components/SiteHeader'
import { useAuth } from './hooks/useAuth'
import { useHashPath } from './hooks/useHashPath'
import { Routes } from './Routes'

/** Revalida la sesión cuando la pestaña recupera el foco o vuelve a ser visible. */
function useSessionWatch() {
    useEffect(() => {
        const check = () => checkSession()

        globalThis.addEventListener('focus', check)
        document.addEventListener('visibilitychange', check)

        return () => {
            globalThis.removeEventListener('focus', check)
            document.removeEventListener('visibilitychange', check)
        }
    }, [])
}

export default function App() {
    const path = useHashPath()
    const { session, notice } = useAuth()
    const main = useRef<HTMLElement>(null)

    useSessionWatch()

    useEffect(() => {
        globalThis.scrollTo({ top: 0 })
        main.current?.focus()
        document.title = `${path === '/' ? 'Inicio' : path.split('/').find(Boolean)} · TrainFyre`
    }, [path])

    return (
        <div className="app">
            <a className="skip-link" href="#main" onClick={event => {
                event.preventDefault()
                main.current?.focus()
            }}>
                Saltar al contenido
            </a>

            <SiteHeader path={path} />

            <main id="main" ref={main} className="container" tabIndex={-1}>
                {notice && (
                    <div className="global-notice">
                        <Notice>{notice}</Notice>
                        <button aria-label="Cerrar aviso" onClick={() => notify('')}>×</button>
                    </div>
                )}
                <Routes key={`${path}:${session?.id ?? 'guest'}`} path={path} />
            </main>

            <SiteFooter />
        </div>
    )
}
