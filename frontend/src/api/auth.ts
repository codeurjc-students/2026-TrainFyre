import { readIdentity, tokenDeadline } from '../model'
import { request } from './http'
import { beginLogin, isCurrentLogin, publishSession } from './session'

import type { TokenResponse, UserDetails } from '../model'

export function validateUser(user: UserDetails): UserDetails {
    if (
        !user ||
        typeof user.username !== 'string' ||
        !Array.isArray(user.alertSubscriptions)
    ) {
        throw new Error('El backend devolvió un perfil no válido.')
    }

    if (user.roles !== undefined && !Array.isArray(user.roles)) {
        throw new TypeError('El campo roles debe ser un array.')
    }

    return user
}

export async function login(username: string, password: string): Promise<void> {
    const attempt = beginLogin()

    const response = await request<TokenResponse>('/auth/login', {
        method: 'POST',
        public: true,
        body: { username, password },
    })

    const identity = readIdentity(response.accessToken)
    const deadline = tokenDeadline(response)

    // No se publica la sesión hasta recuperar correctamente el perfil.
    const user = validateUser(await request<UserDetails>('/users/me', {
        token: response.accessToken,
    }))

    if (!isCurrentLogin(attempt)) {
        throw new Error('El inicio de sesión se ha cancelado.')
    }

    if (deadline <= Date.now()) {
        throw new Error('La sesión caducó durante el inicio de sesión. Inténtalo de nuevo.')
    }

    publishSession({
        id: identity.id,
        token: response.accessToken,
        deadline,
        user: { ...user, roles: user.roles ?? identity.roles },
    })
}
