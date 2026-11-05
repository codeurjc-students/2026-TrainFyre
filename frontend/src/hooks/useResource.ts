import { useCallback, useEffect, useEffectEvent, useState } from 'react'

import { messageOf } from '../model'

interface Settled<T> {
    id: string
    data?: T
    error: string
}

/**
 * Carga un recurso asíncrono y lo recarga cuando cambia `key` o se llama a `reload`.
 * Cancela la petición en curso al desmontar o al cambiar de clave.
 */
export function useResource<T>(
    key: string,
    loader: (signal: AbortSignal) => Promise<T>,
) {
    const [revision, setRevision] = useState(0)
    const [result, setResult] = useState<Settled<T>>()
    const load = useEffectEvent(loader)
    const id = `${key}#${revision}`

    useEffect(() => {
        const controller = new AbortController()

        load(controller.signal)
            .then(data => {
                if (!controller.signal.aborted) setResult({ id, data, error: '' })
            })
            .catch((reason: unknown) => {
                if (!controller.signal.aborted) setResult({ id, error: messageOf(reason) })
            })

        return () => controller.abort()
    }, [id])

    const reload = useCallback(() => setRevision(value => value + 1), [])
    const settled = result?.id === id ? result : undefined

    return {
        data: settled?.data,
        error: settled?.error ?? '',
        loading: !settled,
        reload,
    }
}
