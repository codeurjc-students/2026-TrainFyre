export const LOCAL_ZONE = Intl.DateTimeFormat().resolvedOptions().timeZone

/** Navega a una ruta interna de la aplicación (enrutado por hash). */
export function go(path: string) {
    globalThis.location.hash = path
}

/** Ruta actual del hash, sin la almohadilla. */
export function currentPath(): string {
    return globalThis.location.hash.slice(1) || '/'
}
