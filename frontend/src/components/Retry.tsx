import { Notice } from './Notice'

interface RetryProps {
    readonly error: string
    readonly retry: () => void
}

export function Retry({ error, retry }: RetryProps) {
    return (
        <div className="card">
            <Notice error>{error}</Notice>
            <button className="button secondary" type="button" onClick={retry}>
                Volver a cargar
            </button>
        </div>
    )
}
