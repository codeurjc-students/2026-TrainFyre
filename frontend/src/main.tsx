import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

import './App.css'

const container = document.querySelector('#root')

if (!container) throw new Error('No se encontró el elemento #root.')

const root = createRoot(container)

// La aplicación se importa de forma diferida para poder mostrar los errores de
// arranque (p. ej. una configuración .env inválida) en lugar de una pantalla en blanco.
import('./App')
    .then(({ default: App }) => {
        root.render(
            <StrictMode>
                <App />
            </StrictMode>,
        )
    })
    .catch((error: unknown) => {
        console.error(error)

        const message = document.createElement('pre')

        message.setAttribute('role', 'alert')
        message.textContent =
            `No se pudo iniciar la aplicación: ${error instanceof Error ? error.message : 'error desconocido'}`
        container.replaceChildren(message)
    })
