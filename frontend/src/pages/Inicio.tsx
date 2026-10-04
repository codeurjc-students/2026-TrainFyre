import { Avatar, AvatarFallback } from "@/components/ui/avatar"

export function Inicio() {
    return (
        <section
            className="flex w-full flex-1 items-center justify-center"
            aria-label="Inicio"
        >
            <Avatar className="size-20">
                <AvatarFallback className="text-4xl" aria-label="Logo de la aplicación">
                    ◆
                </AvatarFallback>
            </Avatar>
        </section>
    )
}
