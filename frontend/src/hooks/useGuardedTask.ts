import { useCallback, useRef, useState } from 'react'

import { messageOf } from '../model'

async function execute(task: () => Promise<void>, fail: (message: string) => void) {
    try {
        await task()
    } catch (reason) {
        fail(messageOf(reason))
    }
}

/**
 * Ejecuta una tarea asíncrona evitando envíos duplicados y exponiendo
 * el estado de "ocupado" y el último error legible.
 */
export function useGuardedTask() {
    const locked = useRef(false)
    const [busy, setBusy] = useState(false)
    const [error, setError] = useState('')

    const run = useCallback((task: () => Promise<void>) => {
        if (locked.current) return

        locked.current = true
        setBusy(true)
        setError('')

        execute(task, setError).finally(() => {
            locked.current = false
            setBusy(false)
        })
    }, [])

    return { busy, error, run }
}
