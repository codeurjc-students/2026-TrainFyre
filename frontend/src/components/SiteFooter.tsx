const CURRENT_YEAR = new Date().getFullYear()

export function SiteFooter() {
    return (
        <footer className="site-footer">
            <div>
                <strong>TrainFyre</strong>
                <span>Información para moverte mejor.</span>
            </div>
            <span>© {CURRENT_YEAR} · Versión MVP</span>
        </footer>
    )
}
