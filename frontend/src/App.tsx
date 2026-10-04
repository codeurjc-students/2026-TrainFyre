import { useState } from "react"
import { AppLayout } from "@/components/layout/AppLayout"
import type { Pagina } from "@/components/layout/Header"
import { Inicio } from "./pages/Inicio"
import { Incidencias } from "./pages/Incidencias.tsx"

export default function App() {
    const [pagina, setPagina] = useState<Pagina>("inicio")

    return (
        <AppLayout pagina={pagina} onCambiarPagina={setPagina}>
            {pagina === "inicio" ? <Inicio /> : <Incidencias />}
        </AppLayout>
    )
}
