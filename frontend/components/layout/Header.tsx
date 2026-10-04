import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"

export type OptionMenu = {
    id: string
    name: string
}

type HeaderProps = {
    options: readonly OptionMenu[]
    currentPage: string
    onChangePage: (id: string) => void
}

export function Header({options, currentPage, onChangePage,}: Readonly<HeaderProps>) {
    return (
        <header className="w-full border-b bg-background">
            <div className="flex h-16 w-full items-center justify-between px-6">
                <Button
                    variant="ghost"
                    size="icon"
                    aria-label="Ir a inicio"
                    onClick={() => onChangePage("inicio")}
                >
                    <span aria-hidden="true">◆</span>
                </Button>

                <nav className="flex items-center gap-2" aria-label="Menú principal">
                    {options.map((option) => (
                        <Button
                            key={option.id}
                            variant={currentPage === option.id ? "secondary" : "ghost"}
                            aria-pressed={currentPage === option.id}
                            onClick={() => onChangePage(option.id)}
                        >
                            {option.name}
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
