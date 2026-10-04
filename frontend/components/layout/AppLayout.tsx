import type { ReactNode } from "react"
import { Footer } from "./Footer"
import { Header, type Pagina } from "./Header"

type AppLayoutProps = {
    children: ReactNode
    pagina: Pagina
    onCambiarPagina: (pagina: Pagina) => void
}

export function AppLayout({
                              children,
                              pagina,
                              onCambiarPagina,
                          }: Readonly<AppLayoutProps>) {
    return (
        <div className="flex min-h-svh w-full flex-col bg-background text-foreground">
            <Header pagina={pagina} onCambiarPagina={onCambiarPagina} />

            <main className="flex w-full flex-1">{children}</main>

            <Footer />
        </div>
    )
}
