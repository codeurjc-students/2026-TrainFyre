import { useSyncExternalStore } from 'react'

import { currentPath } from '../lib/browser'

function subscribe(listener: () => void) {
    globalThis.addEventListener('hashchange', listener)

    return () => globalThis.removeEventListener('hashchange', listener)
}

export function useHashPath(): string {
    return useSyncExternalStore(subscribe, currentPath, () => '/')
}
