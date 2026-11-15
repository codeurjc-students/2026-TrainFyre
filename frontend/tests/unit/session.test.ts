import { afterEach, describe, expect, it } from 'vitest'

import {
    getAuthState,
    logout,
    publishSession,
    updateCurrentUser,
} from '../../src/api/session'

import type { UserDetails } from '../../src/model'

const ID = '00000000-0000-4000-8000-000000000001'

const current: UserDetails = { username: 'pablo', roles: ['ADMIN'], alertSubscriptions: [] }

afterEach(() => {
    logout('')
})

describe('updateCurrentUser', () => {
    function login() {
        publishSession({ id: ID, token: 'token', deadline: Date.now() + 60_000, user: current })
    }

    it('reemplaza los datos del usuario de la sesión actual', () => {
        login()

        updateCurrentUser(ID, { username: 'nuevo', roles: ['USER'], alertSubscriptions: [] })

        expect(getAuthState().session?.user).toEqual({
            username: 'nuevo',
            roles: ['USER'],
            alertSubscriptions: [],
        })
    })

    it('conserva los roles de la sesión si la respuesta no los incluye', () => {
        login()

        updateCurrentUser(ID, { username: 'nuevo', alertSubscriptions: [] })

        expect(getAuthState().session?.user.roles).toEqual(['ADMIN'])
    })

    it('ignora usuarios distintos al de la sesión', () => {
        login()

        updateCurrentUser('otro', { username: 'nuevo', alertSubscriptions: [] })

        expect(getAuthState().session?.user.username).toBe('pablo')
    })

    it('ignora la actualización si no hay sesión', () => {
        updateCurrentUser(ID, { username: 'nuevo', alertSubscriptions: [] })

        expect(getAuthState().session).toBeNull()
    })
})
