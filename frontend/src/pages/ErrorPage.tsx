export function ErrorPage() {
    return (
        <section className="card narrow error-page" role="alert">
            <p className="eyebrow">ERROR 500</p>
            <h1>Algo no ha salido bien</h1>
            <p>Se ha producido un error inesperado. Vuelve al inicio o recarga la página para intentarlo de nuevo.</p>
            <div className="actions">
                <a className="button primary" href="#/">Volver al inicio</a>
                <button className="button secondary" onClick={() => globalThis.location.reload()}>
                    Recargar página
                </button>
            </div>
        </section>
    )
}
