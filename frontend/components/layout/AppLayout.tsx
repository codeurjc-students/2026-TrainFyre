import type { ReactNode } from "react"
import { Footer } from "./Footer"
import { Header, type OptionMenu } from "./Header"

type AppLayoutProps = {
    children: ReactNode
    options: readonly OptionMenu[]
    currentPage: string
    onChangePage: (id: string) => void
}

export function AppLayout({children, options, currentPage, onChangePage,}: Readonly<AppLayoutProps>) {
    return (
        <div className="flex min-h-svh w-full flex-col bg-background text-foreground">
            <Header
                options={options}
                currentPage={currentPage}
                onChangePage={onChangePage}
            />

            <main className="flex w-full flex-1">{children}</main>

            <Footer />
        </div>
    )
}
