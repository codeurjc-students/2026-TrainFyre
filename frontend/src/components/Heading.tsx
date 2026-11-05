import type { ReactNode } from 'react'

interface HeadingProps {
    readonly title: string
    readonly children?: ReactNode
    readonly actions?: ReactNode
}

export function Heading({ title, children, actions }: HeadingProps) {
    return (
        <div className="page-heading">
            <div>
                <span className="eyebrow">TRAINFYRE / MOVILIDAD INFORMADA</span>
                <h1>{title}</h1>
                {children && <p className="muted">{children}</p>}
            </div>
            {actions && <div className="actions">{actions}</div>}
        </div>
    )
}
