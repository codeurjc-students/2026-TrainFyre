import { isUuid } from './validation'

import type { Role, TokenResponse } from './types'

const INVALID_IDENTITY = 'El backend devolvió un JWT con un identificador no válido.'

function isRole(role: unknown): role is Role {
    return role === 'USER' || role === 'ADMIN'
}

function decodePayload(token: string): Record<string, unknown> {
    const payload = token.split('.')[1]
    const base64 = payload.replaceAll('-', '+').replaceAll('_', '/')
    const bytes = Uint8Array.from(atob(base64), character => character.codePointAt(0) ?? 0)

    return JSON.parse(new TextDecoder().decode(bytes))
}

export function readIdentity(token: string): {
    id: string
    roles: Role[]
    exp?: number
} {
    try {
        const data = decodePayload(token)
        const subject = data.sub

        if (typeof subject !== 'string' || !isUuid(subject)) {
            throw new Error(INVALID_IDENTITY)
        }

        return {
            id: subject,
            roles: Array.isArray(data.roles) ? data.roles.filter(isRole) : [],
            exp: typeof data.exp === 'number' ? data.exp : undefined,
        }
    } catch (error) {
        throw new Error(INVALID_IDENTITY, { cause: error })
    }
}

export function tokenDeadline(response: TokenResponse, now = Date.now()): number {
    if (
        response.tokenType?.toLowerCase() !== 'bearer' ||
        !Number.isFinite(response.expiresIn) ||
        response.expiresIn <= 0
    ) {
        throw new Error('La respuesta de autenticación no es válida.')
    }

    const identity = readIdentity(response.accessToken)
    const deadline = Math.min(
        now + response.expiresIn * 1000,
        identity.exp === undefined ? Infinity : identity.exp * 1000,
    )

    if (deadline <= now) throw new Error('El token recibido ya ha caducado.')

    return deadline
}
