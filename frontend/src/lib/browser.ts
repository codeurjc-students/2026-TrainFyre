export const LOCAL_ZONE = Intl.DateTimeFormat().resolvedOptions().timeZone

const UNREADABLE_IMAGE = 'No se pudo leer la imagen.'

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

        reader.onload = () => {
            const { result } = reader

            if (typeof result === 'string') resolve(result)
            else reject(new Error(UNREADABLE_IMAGE))
        }
        reader.onerror = () => reject(reader.error ?? new Error(UNREADABLE_IMAGE))
        reader.readAsDataURL(blob)
    })
}
