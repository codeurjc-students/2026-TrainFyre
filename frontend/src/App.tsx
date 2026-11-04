import {
    useEffect,
    useRef,
    useState,
    useSyncExternalStore,
} from 'react'

import type { FormEvent, ReactNode } from 'react'

import {
    api,
    checkSession,
    getAuthState,
    loadIncidence,
    loadIncidences,
    login,
    logout,
    notify,
    subscribeAuth,
    updateCurrentUser,
} from './api'

import {
    causeLabels,
    clock,
    dayLabels,
    displayDate,
    durationLabel,
    durationSeconds,
    emptyFilters,
    filterIncidences,
    isUuid,
    localInput,
    localToIso,
    makeSubscription,
    messageOf,
    parseId,
    parseIds,
    sameSubscription,
    severityLabels,
    subscriptionPatch,
    text,
    validatePassword,
    validateUsername,
} from './model'

import type {
    Filters,
    Incidence,
    IncidenceDetails,
    Severity,
    Subscription,
    UpdateUser,
} from './model'

const LOCAL_ZONE = Intl.DateTimeFormat().resolvedOptions().timeZone

function go(path: string) {
    window.location.hash = path
}

function useAuth() {
    return useSyncExternalStore(subscribeAuth, getAuthState, getAuthState)
}

function useResource<T>(
    key: string,
    loader: (signal: AbortSignal) => Promise<T>,
) {
    const loaderRef = useRef(loader)
    loaderRef.current = loader

    const [revision, setRevision] = useState(0)
    const [data, setData] = useState<T>()
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        const controller = new AbortController()

        setLoading(true)
        setError('')
        setData(undefined)

        loaderRef.current(controller.signal)
            .then(result => {
                if (!controller.signal.aborted) setData(result)
            })
            .catch(reason => {
                if (!controller.signal.aborted) setError(messageOf(reason))
            })
            .finally(() => {
                if (!controller.signal.aborted) setLoading(false)
            })

        return () => controller.abort()
    }, [key, revision])

    return {
        data,
        error,
        loading,
        reload: () => setRevision(value => value + 1),
    }
}

function Notice({
                    children,
                    error = false,
                }: {
    children: ReactNode
    error?: boolean
}) {
    if (!children) return null

    return (
        <div className={`notice ${error ? 'error' : ''}`} role={error ? 'alert' : 'status'}>
            {children}
        </div>
    )
}

function Field({
                   label,
                   hint,
                   children,
               }: {
    label: string
    hint?: string
    children: ReactNode
}) {
    return (
        <label className="field">
            <span>{label}</span>
            {children}
            {hint && <small>{hint}</small>}
        </label>
    )
}

function Form({
                  children,
                  label = 'Guardar cambios',
                  onSubmit,
              }: {
    children: ReactNode
    label?: string
    onSubmit: (data: FormData) => Promise<void>
}) {
    const locked = useRef(false)
    const [busy, setBusy] = useState(false)
    const [error, setError] = useState('')

    async function submit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()

        if (locked.current) return

        const data = new FormData(event.currentTarget)
        locked.current = true
        setBusy(true)
        setError('')

        try {
            await onSubmit(data)
        } catch (reason) {
            setError(messageOf(reason))
        } finally {
            locked.current = false
            setBusy(false)
        }
    }

    return (
        <form onSubmit={submit}>
            <fieldset disabled={busy}>
                {children}
                <button className="button primary" type="submit">
                    {busy ? 'Procesando…' : label}
                </button>
            </fieldset>
            <Notice error>{error}</Notice>
        </form>
    )
}

function Action({
                    children,
                    danger = false,
                    onClick,
                }: {
    children: ReactNode
    danger?: boolean
    onClick: () => Promise<void>
}) {
    const locked = useRef(false)
    const [busy, setBusy] = useState(false)
    const [error, setError] = useState('')

    return (
        <div>
            <button
                type="button"
                className={`button ${danger ? 'danger' : 'secondary'}`}
                disabled={busy}
                onClick={async () => {
                    if (locked.current) return

                    locked.current = true
                    setBusy(true)
                    setError('')

                    try {
                        await onClick()
                    } catch (reason) {
                        setError(messageOf(reason))
                    } finally {
                        locked.current = false
                        setBusy(false)
                    }
                }}
            >
                {busy ? 'Procesando…' : children}
            </button>
            <Notice error>{error}</Notice>
        </div>
    )
}

function Heading({
                     title,
                     children,
                     actions,
                 }: {
    title: string
    children?: ReactNode
    actions?: ReactNode
}) {
    return (
        <div className="page-heading">
            <div>
                <span className="eyebrow">TRAINFYRE / MOVILIDAD INFORMADA</span>
                <h1>{title}</h1>
                {children && <p className="muted">{children}</p>}
            </div>
            {actions && <div className="actions">{actions}</div>}
        </div>
    )
}

function Retry({
                   error,
                   retry,
               }: {
    error: string
    retry: () => void
}) {
    return (
        <div className="card">
            <Notice error>{error}</Notice>
            <button className="button secondary" type="button" onClick={retry}>
                Volver a cargar
            </button>
        </div>
    )
}

function Require({
                     children,
                     admin = false,
                 }: {
    children: ReactNode
    admin?: boolean
}) {
    const { session } = useAuth()

    if (!session) {
        return (
            <div className="card narrow">
                <h1>Inicia sesión</h1>
                <p>Necesitas una cuenta para acceder a esta sección.</p>
                <a className="button primary" href="#/acceso">Iniciar sesión</a>
            </div>
        )
    }

    if (admin && !session.user.roles?.includes('ADMIN')) {
        return (
            <div className="card narrow">
                <h1>Acceso restringido</h1>
                <p>Esta sección requiere el rol de administrador.</p>
                <a className="button secondary" href="#/">Volver al inicio</a>
            </div>
        )
    }

    return children
}

function Home() {
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

function Access({ register = false }: { register?: boolean }) {
    const { session } = useAuth()
    const [created, setCreated] = useState(false)

    if (session) {
        return (
            <div className="card narrow">
                <h1>Ya has iniciado sesión</h1>
                <p>Estás conectado como {session.user.username}.</p>
                <a className="button primary" href="#/cuenta">Ir a mi cuenta</a>
            </div>
        )
    }

    return (
        <section className="auth-layout">
            <div className="auth-intro">
                <span className="eyebrow">BIENVENIDO A TRAINFYRE</span>
                <h1>{register ? 'Tu próximo viaje empieza aquí.' : 'Qué bueno verte de nuevo.'}</h1>
                <p>Gestiona tu cuenta y configura tus preferencias de incidencias.</p>
                <p className="muted">
                    La sesión se mantiene únicamente mientras esta página permanezca abierta.
                    Al recargar o caducar tendrás que identificarte nuevamente.
                </p>
            </div>

            <div className="card">
                <h2>{register ? 'Crear una cuenta' : 'Iniciar sesión'}</h2>

                {created && (
                    <Notice>
                        Tu cuenta ya está creada. Puedes volver a intentar el inicio de sesión
                        sin repetir el registro.
                    </Notice>
                )}

                <Form
                    label={register && !created ? 'Crear cuenta y entrar' : 'Iniciar sesión'}
                    onSubmit={async data => {
                        const username = text(data, 'username')
                        const password = text(data, 'password')
                        let justCreated = false

                        if (register && !created) {
                            validateUsername(username)
                            validatePassword(password)

                            if (password !== text(data, 'confirmation')) {
                                throw new Error('Las contraseñas no coinciden.')
                            }

                            await api.register(username, password)
                            setCreated(true)
                            justCreated = true
                        }

                        try {
                            await login(username, password)
                            go('/cuenta')
                        } catch (error) {
                            throw new Error(
                                justCreated
                                    ? `Cuenta creada, pero no se pudo iniciar sesión. ${messageOf(error)}`
                                    : messageOf(error),
                            )
                        }
                    }}
                >
                    <Field
                        label="Usuario"
                        hint={register ? 'Entre 3 y 32 caracteres: letras, números, punto, guion o guion bajo.' : undefined}
                    >
                        <input
                            name="username"
                            required
                            minLength={register && !created ? 3 : 1}
                            maxLength={32}
                            autoComplete="username"
                            autoCapitalize="none"
                            spellCheck={false}
                        />
                    </Field>

                    <Field
                        label="Contraseña"
                        hint={register && !created ? 'Entre 12 y 128 caracteres.' : undefined}
                    >
                        <input
                            name="password"
                            type="password"
                            required
                            minLength={register && !created ? 12 : 1}
                            maxLength={128}
                            autoComplete={register && !created ? 'new-password' : 'current-password'}
                        />
                    </Field>

                    {register && !created && (
                        <Field label="Repetir contraseña">
                            <input
                                name="confirmation"
                                type="password"
                                required
                                minLength={12}
                                maxLength={128}
                                autoComplete="new-password"
                            />
                        </Field>
                    )}
                </Form>

                <p className="muted">
                    {register ? '¿Ya tienes una cuenta? ' : '¿Todavía no tienes cuenta? '}
                    <a href={register ? '#/acceso' : '#/registro'}>
                        {register ? 'Inicia sesión' : 'Regístrate'}
                    </a>
                </p>
            </div>
        </section>
    )
}

function IncidenceList() {
    const { session } = useAuth()
    const admin = !!session?.user.roles?.includes('ADMIN')
    const resource = useResource('incidences', loadIncidences)
    const [filters, setFilters] = useState<Filters>({ ...emptyFilters })
    const [page, setPage] = useState(0)

    const filtered = filterIncidences(resource.data ?? [], filters)
    const size = 10
    const lastPage = Math.max(0, Math.ceil(filtered.length / size) - 1)
    const currentPage = Math.min(page, lastPage)
    const visible = filtered.slice(currentPage * size, (currentPage + 1) * size)

    function change(key: keyof Filters, value: string) {
        setFilters(previous => ({ ...previous, [key]: value }))
        setPage(0)
    }

    return (
        <>
            <Heading
                title="Incidencias"
                actions={
                    <>
                        <button className="button secondary" onClick={resource.reload}>
                            Actualizar
                        </button>
                        {admin && (
                            <a className="button primary" href="#/incidencias/nueva">
                                Nueva incidencia
                            </a>
                        )}
                    </>
                }
            >
                Consulta el estado de la red. Fechas mostradas en {LOCAL_ZONE}.
            </Heading>

            <div className="card filters">
                <Field label="Buscar">
                    <input
                        type="search"
                        placeholder="Nombre o descripción"
                        value={filters.text}
                        onChange={event => change('text', event.target.value)}
                    />
                </Field>
                <Field label="ID del mapa">
                    <input value={filters.map} onChange={event => change('map', event.target.value)} />
                </Field>
                <Field label="ID de línea">
                    <input value={filters.line} onChange={event => change('line', event.target.value)} />
                </Field>
                <Field label="Gravedad">
                    <select value={filters.severity} onChange={event => change('severity', event.target.value)}>
                        <option value="">Todas</option>
                        {Object.entries(severityLabels).map(([value, label]) => (
                            <option key={value} value={value}>{label}</option>
                        ))}
                    </select>
                </Field>
                <Field label="Causa">
                    <select value={filters.cause} onChange={event => change('cause', event.target.value)}>
                        <option value="">Todas</option>
                        {Object.entries(causeLabels).map(([value, label]) => (
                            <option key={value} value={value}>{label}</option>
                        ))}
                    </select>
                </Field>
                <Field label="Desde">
                    <input type="date" value={filters.from} onChange={event => change('from', event.target.value)} />
                </Field>
                <Field label="Hasta">
                    <input type="date" value={filters.to} onChange={event => change('to', event.target.value)} />
                </Field>
                <button
                    className="button secondary"
                    onClick={() => { setFilters({ ...emptyFilters }); setPage(0) }}
                >
                    Limpiar filtros
                </button>
            </div>

            {filters.from && filters.to && filters.from > filters.to && (
                <Notice error>La fecha inicial es posterior a la final.</Notice>
            )}

            {resource.loading ? (
                <div className="card" role="status">Cargando todas las páginas de incidencias…</div>
            ) : resource.error ? (
                <Retry error={resource.error} retry={resource.reload} />
            ) : (
                <div className="card table-card">
                    <div className="table-heading">
                        <h2>Estado de la red</h2>
                        <span className="badge neutral">
              {filtered.length} de {resource.data?.length ?? 0} incidencias
            </span>
                    </div>

                    {filtered.length === 0 ? (
                        <div className="empty">
                            <h3>No hay incidencias para esta consulta</h3>
                            <p>Prueba otros filtros o actualiza el listado.</p>
                        </div>
                    ) : (
                        <>
                            <div className="table-scroll">
                                <table>
                                    <thead>
                                    <tr>
                                        <th>Incidencia</th>
                                        <th>Red afectada</th>
                                        <th>Gravedad</th>
                                        <th>Duración / retraso</th>
                                        <th>Fecha</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {visible.map(row => (
                                        <tr key={row.id}>
                                            <td>
                                                <a className="table-link" href={`#/incidencias/${row.id}`}>
                                                    {row.description.name}
                                                </a>
                                                <small>{causeLabels[row.classification.cause]}</small>
                                            </td>
                                            <td>
                                                Mapa {row.affectedNetwork.mapId}
                                                <small>Líneas {row.affectedNetwork.lineIds.join(', ')}</small>
                                            </td>
                                            <td>
                          <span className={`badge severity-${row.classification.severity}`}>
                            {severityLabels[row.classification.severity]}
                          </span>
                                            </td>
                                            <td>{durationLabel(row.occurrence.duration)}</td>
                                            <td>{displayDate(row.occurrence.timestamp)}</td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>

                            <nav className="pagination" aria-label="Paginación">
                                <button
                                    className="button secondary"
                                    disabled={currentPage === 0}
                                    onClick={() => setPage(currentPage - 1)}
                                >
                                    Anterior
                                </button>
                                <span>Página {currentPage + 1} de {lastPage + 1}</span>
                                <button
                                    className="button secondary"
                                    disabled={currentPage === lastPage}
                                    onClick={() => setPage(currentPage + 1)}
                                >
                                    Siguiente
                                </button>
                            </nav>
                        </>
                    )}
                </div>
            )}

            <p className="muted small">
                MVP: se consultan todas las páginas para aplicar filtros globales en el navegador.
                Los catálogos de mapas y líneas todavía no están disponibles.
            </p>
        </>
    )
}

function IncidenceEditor({ initial }: { initial?: Incidence }) {
    const [severity, setSeverity] = useState<Severity>(
        initial?.classification.severity ?? 'LOW',
    )
    const [instant, setInstant] = useState(
        initial ? durationSeconds(initial.occurrence.duration) === 0 : false,
    )

    return (
        <div className="card">
            <Form
                label={initial ? 'Guardar incidencia' : 'Crear incidencia'}
                onSubmit={async data => {
                    const name = text(data, 'name').trim()
                    const summary = text(data, 'summary').trim()

                    if (!name || !summary) {
                        throw new Error('El nombre y la descripción son obligatorios.')
                    }

                    const duration =
                        instant || severity === 'INFORMATIONAL'
                            ? 'PT0S'
                            : text(data, 'duration').trim()

                    const seconds = durationSeconds(duration)
                    const timestampInput = text(data, 'timestamp')
                    const timestamp =
                        initial && timestampInput === localInput(initial.occurrence.timestamp)
                            ? initial.occurrence.timestamp
                            : localToIso(timestampInput)

                    if (
                        initial?.classification.severity === 'INFORMATIONAL' &&
                        severity !== 'INFORMATIONAL' &&
                        seconds > 0
                    ) {
                        throw new Error(
                            'Por el orden de actualización del backend, guarda primero la nueva gravedad con duración cero. Después podrás editar su duración.',
                        )
                    }

                    const payload: IncidenceDetails = {
                        affectedNetwork: {
                            mapId: parseId(text(data, 'mapId')),
                            lineIds: parseIds(text(data, 'lineIds')),
                        },
                        occurrence: {
                            timestamp,
                            duration,
                            // instantaneous es calculado por Java a partir de duration.
                        },
                        description: { name, summary },
                        classification: {
                            severity,
                            cause: text(data, 'cause') as IncidenceDetails['classification']['cause'],
                        },
                    }

                    if (initial) {
                        await api.updateIncidence(initial.id, payload)
                        notify('Incidencia actualizada.')
                        go(`/incidencias/${initial.id}`)
                    } else {
                        const id = await api.createIncidence(payload)
                        notify('Incidencia creada.')
                        go(isUuid(id) ? `/incidencias/${id}` : '/incidencias')
                    }
                }}
            >
                <Field label="Nombre">
                    <input name="name" required defaultValue={initial?.description.name ?? ''} />
                </Field>

                <Field label="Descripción">
                    <textarea name="summary" required rows={5} defaultValue={initial?.description.summary ?? ''} />
                </Field>

                <div className="form-grid">
                    <Field label="ID del mapa">
                        <input name="mapId" required defaultValue={initial?.affectedNetwork.mapId ?? ''} />
                    </Field>
                    <Field label="IDs de líneas" hint="Separados por comas. Ejemplo: 1, 4, 7.">
                        <input name="lineIds" required defaultValue={initial?.affectedNetwork.lineIds.join(', ') ?? ''} />
                    </Field>
                    <Field label="Gravedad">
                        <select
                            value={severity}
                            onChange={event => {
                                const value = event.target.value as Severity
                                setSeverity(value)
                                if (value === 'INFORMATIONAL') setInstant(true)
                            }}
                        >
                            {Object.entries(severityLabels).map(([value, label]) => (
                                <option key={value} value={value}>{label}</option>
                            ))}
                        </select>
                    </Field>
                    <Field label="Causa">
                        <select name="cause" defaultValue={initial?.classification.cause ?? 'OTHER'}>
                            {Object.entries(causeLabels).map(([value, label]) => (
                                <option key={value} value={value}>{label}</option>
                            ))}
                        </select>
                    </Field>
                    <Field label="Fecha y hora" hint={`Zona horaria del navegador: ${LOCAL_ZONE}.`}>
                        <input
                            name="timestamp"
                            type="datetime-local"
                            step="1"
                            required
                            defaultValue={localInput(initial?.occurrence.timestamp ?? new Date().toISOString())}
                        />
                    </Field>
                    <Field label="Duración ISO 8601" hint="PT15M = 15 minutos; PT1H30M = 1 hora y 30 minutos.">
                        <input
                            name="duration"
                            required={!instant && severity !== 'INFORMATIONAL'}
                            disabled={instant || severity === 'INFORMATIONAL'}
                            defaultValue={initial?.occurrence.duration ?? 'PT15M'}
                        />
                    </Field>
                </div>

                <label className="check">
                    <input
                        type="checkbox"
                        checked={instant}
                        disabled={severity === 'INFORMATIONAL'}
                        onChange={event => setInstant(event.target.checked)}
                    />
                    Sin retraso / instantánea
                </label>

                {severity === 'INFORMATIONAL' && (
                    <Notice>Las incidencias informativas se guardan con duración PT0S.</Notice>
                )}

                <p className="muted small">
                    Para incidencias pasadas, indica cuánto duraron; para futuras, el retraso previsto.
                </p>
            </Form>
        </div>
    )
}

function IncidencePage({ id, edit = false }: { id: string; edit?: boolean }) {
    const { session } = useAuth()
    const admin = !!session?.user.roles?.includes('ADMIN')
    const resource = useResource(
        `incidence:${id}:${admin}`,
        signal => loadIncidence(id, admin, signal),
    )

    if (resource.loading) return <p role="status">Cargando incidencia…</p>
    if (resource.error) return <Retry error={resource.error} retry={resource.reload} />
    if (!resource.data) return null

    const item = resource.data

    return (
        <>
            <a className="back-link" href="#/incidencias">← Volver a incidencias</a>

            <Heading
                title={edit ? 'Editar incidencia' : item.description.name}
                actions={!edit && admin ? (
                    <>
                        <a className="button primary" href={`#/incidencias/${id}/editar`}>Editar</a>
                        <Action danger onClick={async () => {
                            if (!window.confirm(`¿Eliminar "${item.description.name}"? Esta acción no se puede deshacer.`)) return
                            await api.deleteIncidence(id)
                            notify('Incidencia eliminada.')
                            go('/incidencias')
                        }}>
                            Eliminar
                        </Action>
                    </>
                ) : undefined}
            >
                {edit ? 'Actualiza los datos publicados.' : `Identificador: ${id}`}
            </Heading>

            {edit ? (
                <IncidenceEditor initial={item} />
            ) : (
                <div className="detail-grid">
                    <article className="card">
            <span className={`badge severity-${item.classification.severity}`}>
              {severityLabels[item.classification.severity]}
            </span>
                        <h2>Qué está ocurriendo</h2>
                        <p className="preserve">{item.description.summary}</p>
                        <dl>
                            <dt>Causa</dt><dd>{causeLabels[item.classification.cause]}</dd>
                            <dt>Fecha</dt><dd>{displayDate(item.occurrence.timestamp)}</dd>
                            <dt>Duración / retraso previsto</dt><dd>{durationLabel(item.occurrence.duration)}</dd>
                        </dl>
                    </article>
                    <aside className="card">
                        <h2>Red afectada</h2>
                        <p className="map-title">Mapa {item.affectedNetwork.mapId}</p>
                        <div className="actions">
                            {item.affectedNetwork.lineIds.map(line => (
                                <span className="line-badge" key={line}>Línea {line}</span>
                            ))}
                        </div>
                        <p className="muted small">Los nombres de mapas y líneas estarán disponibles cuando exista su catálogo.</p>
                    </aside>
                </div>
            )}
        </>
    )
}

function SubscriptionEditor({
                                initial,
                                onSave,
                            }: {
    initial?: Subscription
    onSave: (next: Subscription) => Promise<void>
}) {
    return (
        <Form
            label={initial ? 'Guardar alerta' : 'Añadir alerta'}
            onSubmit={async data => onSave(makeSubscription(data, initial))}
        >
            <Field label="IDs de líneas" hint="Separados por comas. Por ahora la API no permite indicar el mapa.">
                <input name="lines" required defaultValue={initial?.lines.join(', ') ?? ''} />
            </Field>

            <div className="form-grid">
                <Field label="Desde las">
                    <input name="startTime" type="time" step="1" required defaultValue={initial ? clock(initial.startTime) : '07:00:00'} />
                </Field>
                <Field label="Hasta las">
                    <input name="endTime" type="time" step="1" required defaultValue={initial ? clock(initial.endTime) : '09:00:00'} />
                </Field>
            </div>

            <fieldset className="days">
                <legend>Días de la semana</legend>
                {Object.entries(dayLabels).map(([day, label]) => (
                    <label className="check" key={day}>
                        <input
                            name="days"
                            type="checkbox"
                            value={day}
                            defaultChecked={initial
                                ? initial.daysOfTheWeek.includes(day as keyof typeof dayLabels)
                                : day !== 'SATURDAY' && day !== 'SUNDAY'}
                        />
                        {label}
                    </label>
                ))}
            </fieldset>

            <Field
                label="Expiración opcional"
                hint={`Si la dejas vacía, se enviará una expiración de 30 días desde el guardado. Se muestra en ${LOCAL_ZONE}.`}
            >
                <input
                    name="expiration"
                    type="datetime-local"
                    step="1"
                    defaultValue={initial ? localInput(initial.expiration) : ''}
                />
            </Field>

            <Notice>
                El backend compara las horas literales, sin convertir zonas horarias.
                Utiliza la misma referencia horaria con la que se generan las incidencias.
                Las franjas no pueden cruzar medianoche.
            </Notice>
        </Form>
    )
}

function UserPage({ id = 'me' }: { id?: string }) {
    const { session } = useAuth()
    const own = id === 'me' || id === session?.id
    const targetId = id === 'me' ? session?.id ?? '' : id
    const [editing, setEditing] = useState<Subscription>()

    const resource = useResource(
        `user:${id}:${session?.id}`,
        async signal => {
            const user = await api.user(id, signal)
            if (!signal.aborted && own) updateCurrentUser(targetId, user)
            return user
        },
    )

    if (resource.loading) return <p role="status">Cargando cuenta…</p>
    if (resource.error) return <Retry error={resource.error} retry={resource.reload} />
    if (!resource.data) return null

    const user = resource.data

    return (
        <>
            <Heading title={own ? 'Mi cuenta' : `Cuenta de ${user.username}`}>
                Gestiona los datos de acceso y las suscripciones.
            </Heading>

            <div className="detail-grid">
                <section className="card">
                    <h2>Datos de acceso</h2>
                    <p className="muted small">UUID: {targetId}</p>
                    <p className="muted small">
                        Roles: {user.roles?.join(', ') || (own ? session?.user.roles?.join(', ') : 'No proporcionados por el backend')}
                    </p>

                    <Form onSubmit={async data => {
                        const username = validateUsername(text(data, 'username'))
                        const password = text(data, 'password')
                        const changes: UpdateUser = {}

                        if (username !== user.username) changes.username = username
                        if (password) changes.password = validatePassword(password)

                        if (!Object.keys(changes).length) {
                            throw new Error('No hay cambios que guardar.')
                        }

                        await api.updateUser(id, changes)

                        if (own && password) {
                            logout('Contraseña actualizada. Vuelve a iniciar sesión.')
                            go('/acceso')
                            return
                        }

                        notify('Datos de la cuenta actualizados.')
                        resource.reload()
                    }}>
                        <Field label="Nombre de usuario" hint="Entre 3 y 32 caracteres.">
                            <input name="username" required minLength={3} maxLength={32} defaultValue={user.username} autoComplete="username" />
                        </Field>
                        <Field label="Nueva contraseña" hint="Déjala vacía para conservarla. Si la cambias: entre 12 y 128 caracteres.">
                            <input name="password" type="password" minLength={12} maxLength={128} autoComplete="new-password" />
                        </Field>
                    </Form>
                </section>

                <aside className="card">
                    <span className="badge">MVP</span>
                    <h2>Alertas en fase de prueba</h2>
                    <p>Las reglas se guardan realmente en el backend.</p>
                    <p>
                        El envío de correo y avisos al móvil todavía no está implementado.
                        Guardar una alerta no significa que vayas a recibir notificaciones.
                    </p>
                    <Notice>
                        La API aún no incluye el mapa en las alertas. Si dos mapas usan el
                        mismo ID de línea, la regla no puede distinguirlos.
                    </Notice>
                    <p className="muted small">Los roles se muestran únicamente para consulta: la API no permite modificarlos.</p>
                </aside>
            </div>

            <section className="card">
                <div className="table-heading">
                    <h2>Suscripciones ({user.alertSubscriptions.length})</h2>
                    {editing && (
                        <button className="button secondary" onClick={() => setEditing(undefined)}>
                            Cancelar edición
                        </button>
                    )}
                </div>

                {user.alertSubscriptions.length === 0 && (
                    <p className="muted">Todavía no hay alertas configuradas.</p>
                )}

                <div className="subscription-grid">
                    {user.alertSubscriptions.map((subscription, index) => (
                        <article className="subscription" key={index}>
                            <h3>Líneas {subscription.lines.join(', ')}</h3>
                            <p>{clock(subscription.startTime)} – {clock(subscription.endTime)}</p>
                            <p>{subscription.daysOfTheWeek.map(day => dayLabels[day]).join(', ')}</p>
                            <p className="muted small">Expira: {displayDate(subscription.expiration)}</p>
                            <div className="actions">
                                <button
                                    className="button secondary"
                                    onClick={() => {
                                        setEditing(subscription)
                                        document.getElementById('alert-editor')?.scrollIntoView({ behavior: 'smooth' })
                                    }}
                                >
                                    Editar
                                </button>
                                <Action danger onClick={async () => {
                                    if (!window.confirm('¿Eliminar esta suscripción?')) return

                                    await api.updateUser(id, {
                                        // Se devuelve exactamente el objeto recibido.
                                        alertSubscriptionListToDelete: [subscription],
                                    })

                                    setEditing(undefined)
                                    notify('Alerta eliminada.')
                                    resource.reload()
                                }}>
                                    Eliminar
                                </Action>
                            </div>
                        </article>
                    ))}
                </div>

                <div className="editor-section" id="alert-editor">
                    <h2>{editing ? 'Editar alerta' : 'Nueva alerta'}</h2>
                    <SubscriptionEditor
                        key={editing ? JSON.stringify(editing) : 'new'}
                        initial={editing}
                        onSave={async next => {
                            const changes = subscriptionPatch(editing, next)

                            if (!changes) {
                                notify('La alerta no tiene cambios.')
                                return
                            }

                            if (user.alertSubscriptions.some(
                                item => item !== editing && sameSubscription(item, next),
                            )) {
                                throw new Error('Ya existe una suscripción idéntica.')
                            }

                            // Edición en una única petición: añadir el nuevo valor y borrar el original.
                            await api.updateUser(id, changes)
                            setEditing(undefined)
                            notify('Alerta guardada. El envío de notificaciones sigue en fase de prueba.')
                            resource.reload()
                        }}
                    />
                </div>
            </section>

            <section className="card danger-zone">
                <h2>Eliminar cuenta</h2>
                <p>
                    Se eliminará la cuenta y sus suscripciones. Esta acción no se puede deshacer.
                </p>
                <Action danger onClick={async () => {
                    if (!window.confirm(`¿Eliminar permanentemente la cuenta "${user.username}"?`)) return
                    await api.deleteUser(targetId)

                    if (own) {
                        logout('Tu cuenta se ha eliminado.')
                        go('/')
                    } else {
                        notify('Cuenta eliminada.')
                        go('/admin/usuarios')
                    }
                }}>
                    {own ? 'Eliminar mi cuenta' : 'Eliminar esta cuenta'}
                </Action>
            </section>
        </>
    )
}

function AdminUsers() {
    return (
        <>
            <Heading title="Administrar usuarios">
                Busca una cuenta por UUID o crea una nueva.
            </Heading>

            <Notice>
                El backend recibido solo autoriza al propietario en UserAccessPolicy.
                La gestión de otras cuentas requiere habilitar también el rol ADMIN en esa política.
            </Notice>

            <div className="detail-grid">
                <section className="card">
                    <h2>Buscar por UUID</h2>
                    <Form label="Abrir cuenta" onSubmit={async data => {
                        const id = text(data, 'id').trim()
                        if (!isUuid(id)) throw new Error('Introduce un UUID válido.')
                        go(`/admin/usuarios/${id}`)
                    }}>
                        <Field label="UUID del usuario">
                            <input name="id" required placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" />
                        </Field>
                    </Form>
                    <p className="muted small">No existe un endpoint para listar todos los usuarios.</p>
                </section>

                <section className="card">
                    <h2>Crear usuario</h2>
                    <Form label="Crear usuario" onSubmit={async data => {
                        const username = validateUsername(text(data, 'username'))
                        const password = validatePassword(text(data, 'password'))

                        if (password !== text(data, 'confirmation')) {
                            throw new Error('Las contraseñas no coinciden.')
                        }

                        const id = await api.register(username, password)
                        notify(`Usuario creado${isUuid(id) ? `: ${id}` : ''}. Se mantiene tu sesión de administrador.`)
                        if (isUuid(id)) go(`/admin/usuarios/${id}`)
                    }}>
                        <Field label="Usuario">
                            <input name="username" required minLength={3} maxLength={32} autoComplete="off" />
                        </Field>
                        <Field label="Contraseña">
                            <input name="password" required type="password" minLength={12} maxLength={128} autoComplete="new-password" />
                        </Field>
                        <Field label="Repetir contraseña">
                            <input name="confirmation" required type="password" minLength={12} maxLength={128} autoComplete="new-password" />
                        </Field>
                    </Form>
                    <p className="muted small">El backend asigna USER a las cuentas nuevas.</p>
                </section>
            </div>
        </>
    )
}

function Routes({ path }: { path: string }) {
    if (path === '/') return <Home />
    if (path === '/acceso') return <Access />
    if (path === '/registro') return <Access register />
    if (path === '/incidencias') return <IncidenceList />
    if (path === '/cuenta') return <Require><UserPage /></Require>

    if (path === '/incidencias/nueva') {
        return (
            <Require admin>
                <Heading title="Nueva incidencia">Publica una actualización de la red.</Heading>
                <IncidenceEditor />
            </Require>
        )
    }

    if (path === '/admin/usuarios') {
        return <Require admin><AdminUsers /></Require>
    }

    const userMatch = /^\/admin\/usuarios\/([^/]+)$/.exec(path)

    if (userMatch && isUuid(userMatch[1])) {
        return <Require admin><UserPage id={userMatch[1]} /></Require>
    }

    const incidenceMatch = /^\/incidencias\/([^/]+)(\/editar)?$/.exec(path)

    if (incidenceMatch && isUuid(incidenceMatch[1])) {
        const page = <IncidencePage id={incidenceMatch[1]} edit={!!incidenceMatch[2]} />
        return incidenceMatch[2] ? <Require admin>{page}</Require> : page
    }

    return (
        <section className="card narrow">
            <h1>Página no encontrada</h1>
            <p>La dirección no existe o contiene un identificador no válido.</p>
            <a className="button primary" href="#/">Volver al inicio</a>
        </section>
    )
}

export default function App() {
    const [path, setPath] = useState(window.location.hash.slice(1) || '/')
    const { session, notice } = useAuth()
    const admin = !!session?.user.roles?.includes('ADMIN')

    useEffect(() => {
        const route = () => setPath(window.location.hash.slice(1) || '/')
        const check = () => checkSession()

        window.addEventListener('hashchange', route)
        window.addEventListener('focus', check)
        document.addEventListener('visibilitychange', check)

        return () => {
            window.removeEventListener('hashchange', route)
            window.removeEventListener('focus', check)
            document.removeEventListener('visibilitychange', check)
        }
    }, [])

    useEffect(() => {
        window.scrollTo({ top: 0 })
        document.getElementById('main')?.focus()
        document.title = `${path === '/' ? 'Inicio' : path.split('/').filter(Boolean)[0]} · TrainFyre`
    }, [path])

    return (
        <div className="app">
            <a className="skip-link" href="#main" onClick={event => {
                event.preventDefault()
                document.getElementById('main')?.focus()
            }}>
                Saltar al contenido
            </a>

            <header className="site-header">
                <div className="header-inner">
                    <a className="brand" href="#/" aria-label="TrainFyre, inicio">
                        <img src="/logo.jpg" alt="" />
                        <span>Train<span>Fyre</span></span>
                    </a>

                    <nav aria-label="Navegación principal">
                        <a href="#/" aria-current={path === '/' ? 'page' : undefined}>Inicio</a>
                        <a href="#/incidencias" aria-current={path.startsWith('/incidencias') ? 'page' : undefined}>
                            Incidencias
                        </a>
                        {admin && (
                            <a href="#/admin/usuarios" aria-current={path.startsWith('/admin') ? 'page' : undefined}>
                                Administración
                            </a>
                        )}
                    </nav>

                    <div className="account-nav">
                        {session ? (
                            <>
                                <a className="user-link" href="#/cuenta">{session.user.username}</a>
                                <button className="button secondary compact" onClick={() => {
                                    logout()
                                    go('/')
                                }}>
                                    Salir
                                </button>
                            </>
                        ) : (
                            <a className="button primary compact" href="#/acceso">Acceder</a>
                        )}
                    </div>
                </div>
            </header>

            <main id="main" className="container" tabIndex={-1}>
                {notice && (
                    <div className="global-notice">
                        <Notice>{notice}</Notice>
                        <button aria-label="Cerrar aviso" onClick={() => notify('')}>×</button>
                    </div>
                )}
                <Routes key={`${path}:${session?.id ?? 'guest'}`} path={path} />
            </main>

            <footer className="site-footer">
                <div>
                    <strong>TrainFyre</strong>
                    <span>Información para moverte mejor.</span>
                </div>
                <span>© {new Date().getFullYear()} · Versión MVP</span>
            </footer>
        </div>
    )
}
