import type { ReactNode } from 'react'

interface NoticeProps {
    readonly children?: ReactNode
    readonly error?: boolean
}

export function Notice({ children, error = false }: NoticeProps) {
    if (!children) return null

    return (
        <div className={`notice ${error ? 'error' : ''}`} role={error ? 'alert' : 'status'}>
            {children}
        </div>
    )
}
