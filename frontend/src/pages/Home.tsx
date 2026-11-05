import { useAuth } from '../hooks/useAuth'

export function Home() {
    const { session } = useAuth()

    return (
        <>
            <section className="hero">
                <div>
                    <span className="pill">TU RED, AL DÍA</span>
                    <h1>Menos incertidumbre.<br /><span>Mejores trayectos.</span></h1>
                    <p>
                        Consulta las incidencias ferroviarias, identifica las líneas afectadas
                        y prepara tus próximos desplazamientos.
                    </p>
                    <div className="actions">
                        <a className="button primary" href="#/incidencias">
                            Consultar incidencias →
                        </a>
                        <a
                            className="button light"
                            href={session ? '#/cuenta' : '#/registro'}
                        >
                            {session ? 'Gestionar mis alertas' : 'Crear una cuenta'}
                        </a>
                    </div>
                </div>
                <div className="rail-art" aria-hidden="true">
                    <div className="rail-line">
                        <i /><i /><i /><i />
                    </div>
                    <div className="rail-label">TrainFyre</div>
                    <p>Información para tu próximo viaje</p>
                </div>
            </section>

            <section className="feature-grid" aria-label="Funcionalidades">
                <article className="card">
                    <span className="feature-number">01</span>
                    <h2>Consulta abierta</h2>
                    <p>Incidencias, causas, gravedad y duración sin necesidad de registrarte.</p>
                    <a href="#/incidencias">Explorar incidencias →</a>
                </article>

                <article className="card">
                    <span className="feature-number">02</span>
                    <h2>Tus preferencias</h2>
                    <p>Guarda reglas por línea, días y franja horaria desde tu cuenta.</p>
                    <span className="badge">Alertas en fase de prueba</span>
                </article>

                <article className="card">
                    <span className="feature-number">03</span>
                    <h2>Una red con contexto</h2>
                    <p>Las estadísticas llegarán en una próxima versión. Aún no están disponibles.</p>
                    <span className="badge neutral">Próximamente</span>
                </article>
            </section>
        </>
    )
}
