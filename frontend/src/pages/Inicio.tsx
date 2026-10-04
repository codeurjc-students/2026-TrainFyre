import {Avatar, AvatarFallback, AvatarImage} from "@/components/ui/avatar"

export function Inicio() {
    return (
        <section className="flex w-full flex-1 items-center justify-center" aria-label="Inicio">
            <Avatar className="size-40" aria-hidden="true">
                <AvatarImage src = "logo.jpg" alt="" className="object-contain" />
                <AvatarFallback>APP</AvatarFallback>
            </Avatar>
        </section>
    )
}
