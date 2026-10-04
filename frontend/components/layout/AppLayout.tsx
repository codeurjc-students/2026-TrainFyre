import type { ReactNode } from "react"
import { Footer } from "./Footer"
import { Header, type OpcionMenu } from "./Header"

type AppLayoutProps = {
    children: ReactNode
    opciones: readonly OpcionMenu[]
    paginaActual: string
    onCambiarPagina: (id: string) => void
}

export function AppLayout({children, opciones, paginaActual, onCambiarPagina,}: Readonly<AppLayoutProps>) {
    return (
        <div className="flex min-h-svh w-full flex-col bg-background text-foreground">
            <Header
                opciones={opciones}
                paginaActual={paginaActual}
                onCambiarPagina={onCambiarPagina}
            />

            <main className="flex w-full flex-1">{children}</main>

            <Footer />
        </div>
    )
}
