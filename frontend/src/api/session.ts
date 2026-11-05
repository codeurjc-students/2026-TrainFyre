import type { UserDetails } from '../model'

export interface Session {
    id: string
    token: string
    deadline: number
    user: UserDetails
}

interface AuthState {
    session: Session | null
    notice: string
}

const MAX_TIMER_DELAY = 2_147_483_647

let state: AuthState = { session: null, notice: '' }
let expirationTimer: ReturnType<typeof setTimeout> | undefined
let authGeneration = 0

const listeners = new Set<() => void>()

export const getAuthState = () => state

export function subscribeAuth(listener: () => void) {
    listeners.add(listener)

    return () => {
        listeners.delete(listener)
    }
}

function emit(next: AuthState) {
    state = next

    for (const listener of listeners) listener()
}

export function notify(notice: string) {
    emit({ ...state, notice })
}

export function logout(notice = 'Has cerrado la sesión.') {
    authGeneration++
    clearTimeout(expirationTimer)
    emit({ session: null, notice })
}

export function checkSession() {
    if (state.session && Date.now() >= state.session.deadline) {
        logout('Tu sesión ha caducado. Vuelve a iniciar sesión.')
    }
}

function armExpiration() {
    clearTimeout(expirationTimer)

    if (!state.session) return

    const delay = Math.min(
        Math.max(state.session.deadline - Date.now(), 0),
        MAX_TIMER_DELAY,
    )

    expirationTimer = setTimeout(() => {
        checkSession()
        if (state.session) armExpiration()
    }, delay)
}

/** Marca el inicio de un login. Cualquier logout posterior invalida el intento. */
export function beginLogin(): number {
    return ++authGeneration
}

export function isCurrentLogin(attempt: number): boolean {
    return attempt === authGeneration
}

export function publishSession(session: Session) {
    emit({ notice: '', session })
    armExpiration()
}

export function updateCurrentUser(id: string, user: UserDetails) {
    const current = state.session

    if (!current || current.id !== id) return

    emit({
        ...state,
        session: {
            ...current,
            user: { ...user, roles: user.roles ?? current.user.roles },
        },
    })
}
