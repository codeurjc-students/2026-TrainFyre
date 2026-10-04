import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"

export type OpcionMenu = {
    id: string
    nombre: string
}

type HeaderProps = {
    opciones: readonly OpcionMenu[]
    paginaActual: string
    onCambiarPagina: (id: string) => void
}

export function Header({opciones, paginaActual, onCambiarPagina,}: Readonly<HeaderProps>) {
    return (
        <header className="w-full border-b bg-background">
            <div className="flex h-16 w-full items-center justify-between px-6">
                <Button
                    variant="ghost"
                    size="icon"
                    aria-label="Ir a inicio"
                    onClick={() => onCambiarPagina("inicio")}
                >
                    <span aria-hidden="true">◆</span>
                </Button>

                <nav className="flex items-center gap-2" aria-label="Menú principal">
                    {opciones.map((opcion) => (
                        <Button
                            key={opcion.id}
                            variant={paginaActual === opcion.id ? "secondary" : "ghost"}
                            aria-pressed={paginaActual === opcion.id}
                            onClick={() => onCambiarPagina(opcion.id)}
                        >
                            {opcion.nombre}
                        </Button>
                    ))}

                    <Avatar className="ml-3" aria-label="Perfil">
                        <AvatarFallback aria-hidden="true">👤</AvatarFallback>
                    </Avatar>
                </nav>
            </div>
        </header>
    )
}
