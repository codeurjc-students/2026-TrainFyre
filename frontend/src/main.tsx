import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

import './App.css'
import { ErrorBoundary } from './components/ErrorBoundary'
import { ErrorPage } from './pages/ErrorPage'

const container = document.querySelector('#root')

if (!container) throw new Error('No se encontró el elemento #root.')

const root = createRoot(container)

// La aplicación se importa de forma diferida para poder mostrar los errores de
// arranque (p. ej. una configuración .env inválida) en lugar de una pantalla en blanco.
import('./App')
    .then(({ default: App }) => {
        root.render(
            <StrictMode>
                <ErrorBoundary>
                    <App />
                </ErrorBoundary>
            </StrictMode>,
        )
    })
    .catch((error: unknown) => {
        // oxlint-disable-next-line no-console
        console.error('No se pudo iniciar la aplicación.', error)
        root.render(
            <StrictMode>
                <ErrorPage />
            </StrictMode>,
        )
    })
