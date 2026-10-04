import { useState } from "react"
import { AppLayout } from "@/components/layout/AppLayout"
import { Inicio } from "./pages/Inicio"
import { Incidencias } from "./pages/Incidencias"

const paginas = [
    { id: "inicio", nombre: "Inicio", componente: Inicio },
    { id: "incidencias", nombre: "Incidencias", componente: Incidencias },
]

export default function App() {
    const [paginaActual, setPaginaActual] = useState("inicio")

    const pagina = paginas.find((item) => item.id === paginaActual) ?? paginas[0]
    const ComponentePagina = pagina.componente

    return (
        <AppLayout
            opciones={paginas}
            paginaActual={pagina.id}
            onCambiarPagina={setPaginaActual}
        >
            <ComponentePagina />
        </AppLayout>
    )
}
