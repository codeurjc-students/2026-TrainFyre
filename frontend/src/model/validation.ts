const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i
const USERNAME_PATTERN = /^[\w.-]{3,32}$/
const INTEGER_PATTERN = /^-?\d+$/

export const PASSWORD_MISMATCH = 'Las contraseñas no coinciden.'

export function messageOf(error: unknown): string {
    return error instanceof Error ? error.message : 'Ha ocurrido un error inesperado.'
}

export function text(data: FormData, name: string): string {
    const value = data.get(name)

    return typeof value === 'string' ? value : ''
}

export function isUuid(value: string): boolean {
    return UUID_PATTERN.test(value)
}

export function parseId(value: string): number {
    if (!INTEGER_PATTERN.test(value.trim())) {
        throw new Error('Los IDs deben ser números enteros.')
    }

    const number = Number(value)

    if (!Number.isSafeInteger(number)) {
        throw new Error('El ID excede el rango de enteros seguro del navegador.')
    }

    return number
}

export function parseIds(value: string): number[] {
    if (!value.trim()) {
        throw new Error('Indica al menos una línea.')
    }

    return [...new Set(value.split(',').map(item => parseId(item)))]
}

export function validateUsername(value: string): string {
    if (!USERNAME_PATTERN.test(value)) {
        throw new Error(
            'El usuario debe tener entre 3 y 32 caracteres: letras, números, punto, guion o guion bajo.',
        )
    }

    return value
}

export function validatePassword(value: string): string {
    if (!value.trim() || value.length < 12 || value.length > 128) {
        throw new Error(
            'La contraseña debe tener entre 12 y 128 caracteres y no puede contener solo espacios.',
        )
    }

    return value
}

export function validateNewCredentials(data: FormData): { username: string; password: string } {
    const username = validateUsername(text(data, 'username'))
    const password = validatePassword(text(data, 'password'))

    if (password !== text(data, 'confirmation')) {
        throw new Error(PASSWORD_MISMATCH)
    }

    return { username, password }
}
