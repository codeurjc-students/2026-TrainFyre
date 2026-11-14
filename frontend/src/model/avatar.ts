export const AVATAR_TYPES = ['image/png', 'image/jpeg', 'image/webp'] as const
export const AVATAR_MAX_MB = 2
export const AVATAR_MAX_BYTES = AVATAR_MAX_MB * 1024 * 1024

const AVATAR_FIELD = 'file'

/** Normaliza un tipo MIME ignorando parámetros y mayúsculas (`image/PNG; q=1` → `image/png`). */
function essence(type: string): string {
    return type.split(';', 1)[0].trim().toLowerCase()
}

export function isAvatarType(type: string): boolean {
    return (AVATAR_TYPES as readonly string[]).includes(essence(type))
}

/** Valida en cliente tipo y tamaño. La validación definitiva la hace siempre el backend. */
export function validateAvatar(file: File): File {
    if (file.size === 0) {
        throw new Error('Selecciona una imagen.')
    }

    if (!isAvatarType(file.type)) {
        throw new Error('La imagen debe ser PNG, JPEG o WebP.')
    }

    if (file.size > AVATAR_MAX_BYTES) {
        throw new Error(`La imagen no puede superar los ${AVATAR_MAX_MB} MB.`)
    }

    return file
}

/** Lee y valida la imagen del campo `file` de un formulario. */
export function avatarFromForm(data: FormData): File {
    const value = data.get(AVATAR_FIELD)

    if (!(value instanceof File)) {
        throw new TypeError('Selecciona una imagen.')
    }

    return validateAvatar(value)
}

const SIGNATURE_BYTES = 12
const PNG = [0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A]
const JPEG = [0xFF, 0xD8, 0xFF]
const RIFF = [0x52, 0x49, 0x46, 0x46]
const WEBP = [0x57, 0x45, 0x42, 0x50]
const WEBP_OFFSET = 8

function startsWith(bytes: Uint8Array, signature: number[], offset = 0): boolean {
    return signature.every((value, index) => bytes[offset + index] === value)
}

/** Detecta el formato real de la imagen por sus primeros bytes, sin fiarse de la cabecera. */
export function detectAvatarType(bytes: Uint8Array): (typeof AVATAR_TYPES)[number] | undefined {
    if (startsWith(bytes, PNG)) return 'image/png'
    if (startsWith(bytes, JPEG)) return 'image/jpeg'
    if (startsWith(bytes, RIFF) && startsWith(bytes, WEBP, WEBP_OFFSET)) return 'image/webp'

    return undefined
}

/**
 * Interpreta la respuesta de descarga: vacía → "sin avatar"; imagen admitida →
 * Blob con el tipo correcto aunque el backend no envíe `Content-Type`; otro contenido → error.
 */
export async function readAvatarBlob(blob: Blob): Promise<Blob | null> {
    if (blob.size === 0) return null

    const head = new Uint8Array(await blob.slice(0, SIGNATURE_BYTES).arrayBuffer())
    const type = detectAvatarType(head)

    if (!type) {
        throw new TypeError('El backend devolvió una imagen de perfil no válida.')
    }

    return blob.type === type ? blob : new Blob([blob], { type })
}
