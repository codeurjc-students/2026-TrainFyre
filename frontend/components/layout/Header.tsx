import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
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

export function Header({ options, currentPage, onChangePage }: Readonly<HeaderProps>) {
    return (
        <header className="w-full border-b bg-background">
            <div className="flex h-22 w-full items-center justify-between px-4 sm:px-8 md:h-28 lg:px-12">
            <Button
                    variant="ghost"
                    size="icon"
                    className="h-16 w-16 shrink-0 p-0 md:h-20 md:w-20"
                    aria-label="Ir a inicio"
                    onClick={() => onChangePage("inicio")}
                >
                    <Avatar className="h-16 w-16 md:h-20 md:w-20" aria-hidden="true">
                        <AvatarImage src = "logo.jpg" alt="" className="object-contain" />
                        <AvatarFallback>APP</AvatarFallback>
                    </Avatar>
                </Button>


                <nav className="flex items-center gap-2 md:gap-5" aria-label="Menú principal">

                    {options.map((option) => (
                        <Button
                            key={option.id}
                            variant={currentPage === option.id ? "secondary" : "ghost"}
                            className="h-11 px-4 text-base md:h-12 md:px-6"
                            aria-pressed={currentPage === option.id}
                            onClick={() => onChangePage(option.id)}
                        >
                            {option.name}
                        </Button>
                    ))}

                    <Avatar className="ml-2 h-10 w-10 shrink-0 md:ml-4 md:h-12 md:w-12" aria-label="Perfil">
                        <AvatarFallback aria-hidden="true">👤</AvatarFallback>
                    </Avatar>
                </nav>
            </div>
        </header>
    )
}
