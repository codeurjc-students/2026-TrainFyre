export function Footer() {
    return (
        <footer className="w-full border-t bg-background">
            <div className="px-6 py-5 text-center text-sm text-muted-foreground">
                © {new Date().getFullYear()} Mi aplicación
            </div>
        </footer>
    )
}
