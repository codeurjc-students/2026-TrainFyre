export function NotFound() {
    return (
        <section className="card narrow error-page">
            <p className="eyebrow">ERROR 404</p>
            <h1>Página no encontrada</h1>
            <p>La dirección no existe o contiene un identificador no válido.</p>
            <a className="button primary" href="#/">Volver al inicio</a>
        </section>
    )
}
