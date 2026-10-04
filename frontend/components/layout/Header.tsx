import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"

export type Pagina = "inicio" | "incidencias"

type HeaderProps = {
    pagina: Pagina
    onCambiarPagina: (pagina: Pagina) => void
}

export function Header({ pagina, onCambiarPagina }: Readonly<HeaderProps>) {
    return (
        <header className="w-full border-b bg-background">
            <div className="flex h-16 w-full items-center justify-between px-6">
                <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => onCambiarPagina("inicio")}
                    aria-label="Ir a inicio"
                >
                    <span aria-hidden="true" className="text-xl">◆</span>
                </Button>

                <nav className="flex items-center gap-2" aria-label="Menú principal">
                    <Button
                        variant={pagina === "inicio" ? "secondary" : "ghost"}
                        onClick={() => onCambiarPagina("inicio")}
                    >
                        Inicio
                    </Button>

                    <Button
                        variant={pagina === "incidencias" ? "secondary" : "ghost"}
                        onClick={() => onCambiarPagina("incidencias")}
                    >
                        Incidencias
                    </Button>

                    <Avatar className="ml-3" aria-label="Perfil">
                        <AvatarFallback aria-hidden="true">👤</AvatarFallback>
                    </Avatar>
                </nav>
            </div>
        </header>
    )
}
