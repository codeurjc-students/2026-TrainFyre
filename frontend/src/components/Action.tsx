import { useGuardedTask } from '../hooks/useGuardedTask'
import { Notice } from './Notice'

import type { ReactNode } from 'react'

interface ActionProps {
    readonly children: ReactNode
    readonly danger?: boolean
    readonly onClick: () => Promise<void>
}

export function Action({ children, danger = false, onClick }: ActionProps) {
    const { busy, error, run } = useGuardedTask()

    return (
        <div>
            <button
                type="button"
                className={`button ${danger ? 'danger' : 'secondary'}`}
                disabled={busy}
                onClick={() => run(onClick)}
            >
                {busy ? 'Procesando…' : children}
            </button>
            <Notice error>{error}</Notice>
        </div>
    )
}
