import { Component, type ErrorInfo, type ReactNode } from 'react'

import { ErrorPage } from '../pages/ErrorPage'

interface ErrorBoundaryProps {
    readonly children: ReactNode
}

interface ErrorBoundaryState {
    readonly error: Error | null
}

/** Evita que un fallo inesperado de una página deje la aplicación en blanco. */
export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
    state: ErrorBoundaryState = { error: null }

    static getDerivedStateFromError(error: Error): ErrorBoundaryState {
        return { error }
    }

    componentDidCatch(error: Error, errorInfo: ErrorInfo) {
        // oxlint-disable-next-line no-console
        console.error('Error inesperado al renderizar la página.', error, errorInfo)
    }

    render() {
        return this.state.error
            ? <ErrorPage />
            : this.props.children
    }
}
