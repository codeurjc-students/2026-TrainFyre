import type { ReactNode } from 'react'

interface FieldProps {
    readonly label: string
    readonly hint?: string
    readonly children: ReactNode
}

export function Field({ label, hint, children }: FieldProps) {
    return (
        <label className="field">
            <span>{label}</span>
            {children}
            {hint && <small>{hint}</small>}
        </label>
    )
}
