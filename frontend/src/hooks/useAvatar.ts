import { useSyncExternalStore } from 'react'

import { api, getAvatarVersion, subscribeAvatar } from '../api'
import { blobToDataUrl } from '../lib/browser'
import { useResource } from './useResource'

/**
 * Carga el avatar de `id` (UUID o `me`) y expone una URL lista para `<img>`
 * (data URI, permitida por la CSP). Se recarga cuando cualquier vista sube
 * o elimina un avatar.
 */
export function useAvatar(id: string) {
    const version = useSyncExternalStore(subscribeAvatar, getAvatarVersion, getAvatarVersion)

    const resource = useResource(`avatar:${id}:${version}`, async signal => {
        const blob = await api.avatar(id, signal)

        return blob ? blobToDataUrl(blob) : undefined
    })

    return {
        url: resource.data,
        error: resource.error,
    }
}
