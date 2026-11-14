import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'

import type { Plugin } from 'vite'

const DEFAULT_API_BASE_URL = 'https://localhost:443/api/v1'

// Cabeceras seguras también en desarrollo (no bloquean vistas previas del IDE).
const DEV_SECURITY_HEADERS = {
    'X-Content-Type-Options': 'nosniff',
    'Referrer-Policy': 'no-referrer',
}

// Cabeceras adicionales solo para la versión compilada.
const BASE_SECURITY_HEADERS = {
    ...DEV_SECURITY_HEADERS,
    'X-Frame-Options': 'DENY',
    'Permissions-Policy': 'camera=(), microphone=(), geolocation=(), payment=()',
    'Cross-Origin-Opener-Policy': 'same-origin',
}

/**
 * Content-Security-Policy de la aplicación compilada. `frame-ancestors` solo
 * es válido como cabecera HTTP, no en una etiqueta <meta>.
 */
function contentSecurityPolicy(apiBaseUrl: string, withFrameAncestors: boolean): string {
    const directives = [
        "default-src 'self'",
        "script-src 'self'",
        "style-src 'self'",
        "img-src 'self' data:",
        "font-src 'self' data:",
        `connect-src 'self' ${new URL(apiBaseUrl).origin}`,
        "object-src 'none'",
        "base-uri 'self'",
        "form-action 'self'",
    ]

    if (withFrameAncestors) directives.push("frame-ancestors 'none'")

    return directives.join('; ')
}

/** Inserta la CSP como <meta> en el index.html de producción. */
function cspMeta(apiBaseUrl: string): Plugin {
    return {
        name: 'trainfyre:csp-meta',
        apply: 'build',
        transformIndexHtml: () => [
            {
                tag: 'meta',
                attrs: {
                    'http-equiv': 'Content-Security-Policy',
                    content: contentSecurityPolicy(apiBaseUrl, false),
                },
                injectTo: 'head-prepend',
            },
        ],
    }
}

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd(), 'VITE_')
    const apiBaseUrl = env.VITE_API_BASE_URL || DEFAULT_API_BASE_URL

    return {
        plugins: [react(), cspMeta(apiBaseUrl)],
        server: {
            host: 'localhost',
            port: 5173,
            strictPort: true,
            headers: DEV_SECURITY_HEADERS,
        },
        preview: {
            host: 'localhost',
            port: 5173,
            strictPort: true,
            headers: {
                ...BASE_SECURITY_HEADERS,
                'Content-Security-Policy': contentSecurityPolicy(apiBaseUrl, true),
            },
        },
    }
})
