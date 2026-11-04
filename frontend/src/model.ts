export const severityLabels = {
    CRITICAL: 'Crítica',
    HIGH: 'Alta',
    MODERATE: 'Moderada',
    LOW: 'Baja',
    INFORMATIONAL: 'Informativa',
} as const

export const causeLabels = {
    WEATHER: 'Meteorología',
    MAINTENANCE: 'Mantenimiento',
    STRIKE: 'Huelga',
    TECHNICAL_PROBLEM: 'Problema técnico',
    MEDICAL_EMERGENCY: 'Emergencia médica',
    POLICE_ACTION: 'Intervención policial',
    HOLIDAY: 'Festivo',
    OTHER: 'Otra causa',
} as const

export const dayLabels = {
    MONDAY: 'Lunes',
    TUESDAY: 'Martes',
    WEDNESDAY: 'Miércoles',
    THURSDAY: 'Jueves',
    FRIDAY: 'Viernes',
    SATURDAY: 'Sábado',
    SUNDAY: 'Domingo',
} as const

export type Severity = keyof typeof severityLabels
export type Cause = keyof typeof causeLabels
export type Day = keyof typeof dayLabels
export type Role = 'USER' | 'ADMIN'

export interface IncidenceDetails {
    affectedNetwork: {
        mapId: number
        lineIds: number[]
    }
    occurrence: {
        timestamp: string
        duration: string
        instantaneous?: boolean
    }
    description: {
        name: string
        summary: string
    }
    classification: {
        severity: Severity
        cause: Cause
    }
}

export interface Incidence extends IncidenceDetails {
    id: string
}

export interface Subscription {
    lines: number[]
    startTime: string
    endTime: string
    daysOfTheWeek: Day[]
    expiration: string
}

export interface UserDetails {
    username: string
    alertSubscriptions: Subscription[]
    // Compatibilidad temporal con el backend actual.
    roles?: Role[]
}

export interface UpdateUser {
    username?: string
    password?: string
    alertSubscriptionListToAdd?: Subscription[]
    alertSubscriptionListToDelete?: Subscription[]
}

export interface TokenResponse {
    accessToken: string
    tokenType: string
    expiresIn: number
}

export interface Page<T> {
    content: T[]
    page: number
    size: number
    totalElements: number
}

export interface Filters {
    text: string
    map: string
    line: string
    severity: string
    cause: string
    from: string
    to: string
}

export const emptyFilters: Filters = {
    text: '',
    map: '',
    line: '',
    severity: '',
    cause: '',
    from: '',
    to: '',
}

export function messageOf(error: unknown): string {
    return error instanceof Error ? error.message : 'Ha ocurrido un error inesperado.'
}

export function text(data: FormData, name: string): string {
    return String(data.get(name) ?? '')
}

export function isUuid(value: string): boolean {
    return /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(value)
}

export function parseId(value: string): number {
    if (!/^-?\d+$/.test(value.trim())) {
        throw new Error('Los IDs deben ser números enteros.')
    }

    const number = Number(value)

    if (!Number.isSafeInteger(number)) {
        throw new Error('El ID excede el rango de enteros seguro del navegador.')
    }

    return number
}

export function parseIds(value: string): number[] {
    if (!value.trim()) {
        throw new Error('Indica al menos una línea.')
    }

    return [...new Set(value.split(',').map(parseId))]
}

export function validateUsername(value: string): string {
    if (!/^[A-Za-z0-9._-]{3,32}$/.test(value)) {
        throw new Error(
            'El usuario debe tener entre 3 y 32 caracteres: letras, números, punto, guion o guion bajo.',
        )
    }

    return value
}

export function validatePassword(value: string): string {
    if (!value.trim() || value.length < 12 || value.length > 128) {
        throw new Error(
            'La contraseña debe tener entre 12 y 128 caracteres y no puede contener solo espacios.',
        )
    }

    return value
}

export function durationSeconds(value: string): number {
    // java.time.Duration: días, horas, minutos y segundos; no meses o años.
    const match =
        /^P(?:(\d+)D)?(?:T(?:(\d+)H)?(?:(\d+)M)?(?:(\d+(?:\.\d{1,9})?)S)?)?$/.exec(value)

    if (
        !match ||
        !match.slice(1).some(Boolean) ||
        value.endsWith('T')
    ) {
        throw new Error(
            'Duración no válida. Ejemplos: PT0S, PT15M, PT1H30M o P1D.',
        )
    }

    const seconds =
        Number(match[1] || 0) * 86400 +
        Number(match[2] || 0) * 3600 +
        Number(match[3] || 0) * 60 +
        Number(match[4] || 0)

    if (!Number.isFinite(seconds)) {
        throw new Error('Duración fuera de rango.')
    }

    return seconds
}

export function durationLabel(value: string): string {
    try {
        const seconds = durationSeconds(value)

        if (seconds === 0) return 'Sin retraso'

        const hours = Math.floor(seconds / 3600)
        const minutes = Math.floor((seconds % 3600) / 60)
        const rest = Number((seconds % 60).toFixed(9))

        return [
            hours ? `${hours} h` : '',
            minutes ? `${minutes} min` : '',
            rest ? `${rest} s` : '',
        ].filter(Boolean).join(' ')
    } catch {
        return value
    }
}

export function stripZone(value: string): string {
    return value.replace(/\[[^\]]+\]$/, '')
}

export function localInput(value: string): string {
    const date = new Date(stripZone(value))

    if (Number.isNaN(date.getTime())) return ''

    const pad = (number: number) => String(number).padStart(2, '0')

    return (
        `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
        `T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
    )
}

export function localToIso(value: string): string {
    const date = new Date(value)

    if (!value || Number.isNaN(date.getTime())) {
        throw new Error('Introduce una fecha y hora válidas.')
    }

    // Detecta horas inexistentes durante el cambio al horario de verano.
    const normalizedInput = value.length === 16 ? `${value}:00` : value

    if (localInput(date.toISOString()) !== normalizedInput) {
        throw new Error(
            'Esta fecha u hora local no es válida, posiblemente por el cambio horario.',
        )
    }

    return date.toISOString()
}

export function displayDate(value: string): string {
    const date = new Date(stripZone(value))

    if (Number.isNaN(date.getTime())) return value

    return date.toLocaleString('es-ES', {
        dateStyle: 'medium',
        timeStyle: 'short',
    })
}

export function defaultExpiration(now = new Date()): string {
    return new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000).toISOString()
}

export function clock(value: string): string {
    return value.match(/T(\d{2}:\d{2}:\d{2})/)?.[1] ?? '00:00:00'
}

function normalizeClock(value: string): string {
    const result = value.length === 5 ? `${value}:00` : value

    if (!/^\d{2}:\d{2}:\d{2}$/.test(result)) {
        throw new Error('Introduce una hora válida.')
    }

    const [hour, minute, second] = result.split(':').map(Number)

    if (hour > 23 || minute > 59 || second > 59) {
        throw new Error('Introduce una hora válida.')
    }

    return result
}

function recurringTime(value: string, original?: string): string {
    const time = normalizeClock(value)

    // Mantener el valor original evita perder precisión o información de zona.
    if (original && clock(original) === time) return original

    if (original) {
        const offset = stripZone(original).match(/(Z|[+-]\d{2}:\d{2})$/)?.[1]
        const region = original.match(/\[[^\]]+\]$/)?.[0] ?? ''

        if (!offset) {
            throw new Error('La suscripción original no contiene una zona horaria válida.')
        }

        return `${original.slice(0, 10)}T${time}${offset}${region}`
    }

    const now = new Date()
    const offset = -now.getTimezoneOffset()
    const absolute = Math.abs(offset)
    const pad = (number: number) => String(number).padStart(2, '0')
    const suffix =
        `${offset >= 0 ? '+' : '-'}` +
        `${pad(Math.floor(absolute / 60))}:${pad(absolute % 60)}`

    return `${localInput(now.toISOString()).slice(0, 10)}T${time}${suffix}`
}

export function makeSubscription(
    data: FormData,
    original?: Subscription,
): Subscription {
    const start = normalizeClock(text(data, 'startTime'))
    const end = normalizeClock(text(data, 'endTime'))

    if (end < start) {
        throw new Error(
            'El backend actual no admite franjas que crucen medianoche. Crea dos suscripciones.',
        )
    }

    const days = data.getAll('days').map(String) as Day[]

    if (!days.length || days.some(day => !(day in dayLabels))) {
        throw new Error('Selecciona al menos un día válido.')
    }

    const expirationInput = text(data, 'expiration')
    const expiration =
        original && expirationInput === localInput(original.expiration)
            ? original.expiration
            : expirationInput
                ? localToIso(expirationInput)
                : defaultExpiration()

    if (new Date(stripZone(expiration)).getTime() <= Date.now()) {
        throw new Error('La expiración debe estar en el futuro.')
    }

    return {
        lines: parseIds(text(data, 'lines')),
        startTime: recurringTime(start, original?.startTime),
        endTime: recurringTime(end, original?.endTime),
        daysOfTheWeek: [...new Set(days)],
        expiration,
    }
}

export function sameSubscription(a: Subscription, b: Subscription): boolean {
    const canonical = (value: Subscription) => ({
        ...value,
        lines: [...value.lines].sort((x, y) => x - y),
        daysOfTheWeek: [...value.daysOfTheWeek].sort(),
    })

    return JSON.stringify(canonical(a)) === JSON.stringify(canonical(b))
}

export function subscriptionPatch(
    original: Subscription | undefined,
    next: Subscription,
): UpdateUser | null {
    if (original && sameSubscription(original, next)) return null

    return {
        alertSubscriptionListToAdd: [next],
        ...(original ? { alertSubscriptionListToDelete: [original] } : {}),
    }
}

export function readIdentity(token: string): {
    id: string
    roles: Role[]
    exp?: number
} {
    try {
        const payload = token.split('.')[1]
        const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
        const bytes = Uint8Array.from(atob(base64), character => character.charCodeAt(0))
        const data = JSON.parse(new TextDecoder().decode(bytes))

        if (!isUuid(data.sub)) throw new Error()

        return {
            id: data.sub,
            roles: Array.isArray(data.roles)
                ? data.roles.filter((role: unknown) => role === 'USER' || role === 'ADMIN')
                : [],
            exp: typeof data.exp === 'number' ? data.exp : undefined,
        }
    } catch {
        throw new Error('El backend devolvió un JWT con un identificador no válido.')
    }
}

export function tokenDeadline(response: TokenResponse, now = Date.now()): number {
    if (
        response.tokenType?.toLowerCase() !== 'bearer' ||
        !Number.isFinite(response.expiresIn) ||
        response.expiresIn <= 0
    ) {
        throw new Error('La respuesta de autenticación no es válida.')
    }

    const identity = readIdentity(response.accessToken)
    const deadline = Math.min(
        now + response.expiresIn * 1000,
        identity.exp === undefined ? Infinity : identity.exp * 1000,
    )

    if (deadline <= now) throw new Error('El token recibido ya ha caducado.')

    return deadline
}

export function normalizeIncidence(
    raw: Incidence & { incidenceDetails?: IncidenceDetails },
): Incidence {
    const details = raw.incidenceDetails ?? raw

    if (
        !isUuid(raw.id) ||
        !details.affectedNetwork ||
        !details.occurrence ||
        !details.description ||
        !details.classification ||
        !Array.isArray(details.affectedNetwork.lineIds)
    ) {
        throw new Error('La API devolvió una incidencia incompleta.')
    }

    if (
        !Number.isSafeInteger(details.affectedNetwork.mapId) ||
        !details.affectedNetwork.lineIds.every(Number.isSafeInteger)
    ) {
        throw new Error('La API devolvió IDs fuera del rango numérico seguro del navegador.')
    }

    return { ...details, id: raw.id }
}

export function filterIncidences(rows: Incidence[], filters: Filters): Incidence[] {
    return rows.filter(row => {
        const date = localInput(row.occurrence.timestamp).slice(0, 10)
        const description =
            `${row.description.name} ${row.description.summary}`.toLocaleLowerCase('es')

        return (
            (!filters.text || description.includes(filters.text.toLocaleLowerCase('es'))) &&
            (!filters.map || String(row.affectedNetwork.mapId) === filters.map.trim()) &&
            (!filters.line || row.affectedNetwork.lineIds.some(
                id => String(id) === filters.line.trim(),
            )) &&
            (!filters.severity || row.classification.severity === filters.severity) &&
            (!filters.cause || row.classification.cause === filters.cause) &&
            (!filters.from || date >= filters.from) &&
            (!filters.to || date <= filters.to)
        )
    })
}
