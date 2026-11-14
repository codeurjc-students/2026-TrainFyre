export const LOCAL_ZONE = Intl.DateTimeFormat().resolvedOptions().timeZone

/** Navega a una ruta interna de la aplicación (enrutado por hash). */
export function go(path: string) {
    globalThis.location.hash = path
}

/** Ruta actual del hash, sin la almohadilla. */
export function currentPath(): string {
    return globalThis.location.hash.slice(1) || globalThis.location.pathname || '/'
}

/** Lee un `Blob` como data URI (`data:<tipo>;base64,...`). */
export function blobToDataUrl(blob: Blob): Promise<string> {
    return new Promise((resolve, reject) => {
        const reader = new FileReader()

        reader.onload = () => resolve(String(reader.result))
        reader.onerror = () => reject(reader.error ?? new Error('No se pudo leer la imagen.'))
        reader.readAsDataURL(blob)
    })
}
