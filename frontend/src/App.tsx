import { useState } from "react"
import { AppLayout } from "@/components/layout/AppLayout"
import { Inicio } from "./pages/Inicio"
import { Incidencias } from "./pages/Incidencias"

const pages = [
    { id: "inicio", name: "Inicio", components: Inicio },
    { id: "incidencias", name: "Incidencias", components: Incidencias },
]

export default function App() {
    const [currentPage, setCurrentPage] = useState("inicio")

    const page = pages.find((item) => item.id === currentPage) ?? pages[0]
    const PageComponent = page.components

    return (
        <AppLayout
            options={pages}
            currentPage={page.id}
            onChangePage={setCurrentPage}
        >
            <PageComponent />
        </AppLayout>
    )
}
