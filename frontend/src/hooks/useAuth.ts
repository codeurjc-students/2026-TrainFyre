import { useSyncExternalStore } from 'react'

import { getAuthState, subscribeAuth } from '../api'

export function useAuth() {
    return useSyncExternalStore(subscribeAuth, getAuthState, getAuthState)
}

export function useIsAdmin(): boolean {
    const { session } = useAuth()

    return !!session?.user.roles?.includes('ADMIN')
}
