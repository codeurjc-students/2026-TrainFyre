const PAD_LENGTH = 2
const SECONDS_PER_MINUTE = 60
const MINUTES_PER_HOUR = 60
const SECONDS_PER_HOUR = 3600
const SECONDS_PER_DAY = 86_400
const DEFAULT_EXPIRATION_MS = 30 * 24 * 60 * 60 * 1000

// java.time.Duration: días, horas, minutos y segundos; no meses o años.
const DURATION_DATE_PART = /^(?:(\d+)D)?$/
const DURATION_TIME_PART = /^(?:(\d+)H)?(?:(\d+)M)?(?:(\d+(?:\.\d{1,9})?)S)?$/
const INVALID_DURATION = 'Duración no válida. Ejemplos: PT0S, PT15M, PT1H30M o P1D.'

function pad(number: number): string {
    return String(number).padStart(PAD_LENGTH, '0')
}

function splitDuration(value: string): [datePart: string, timePart: string] {
    if (!value.startsWith('P')) throw new Error(INVALID_DURATION)

    const body = value.slice(1)
    const separator = body.indexOf('T')

    if (separator === -1) return [body, '']

    const timePart = body.slice(separator + 1)

    // "PT" y "P1DT" no son duraciones válidas.
    if (!timePart) throw new Error(INVALID_DURATION)

    return [body.slice(0, separator), timePart]
}

export function durationSeconds(value: string): number {
    const [datePart, timePart] = splitDuration(value)
    const date = DURATION_DATE_PART.exec(datePart)
    const time = DURATION_TIME_PART.exec(timePart)

    if (!date || !time) throw new Error(INVALID_DURATION)

    const [days, hours, minutes, seconds] = [date[1], time[1], time[2], time[3]]

    if (![days, hours, minutes, seconds].some(Boolean)) {
        throw new Error(INVALID_DURATION)
    }

    const total =
        Number(days || 0) * SECONDS_PER_DAY +
        Number(hours || 0) * SECONDS_PER_HOUR +
        Number(minutes || 0) * SECONDS_PER_MINUTE +
        Number(seconds || 0)

    if (!Number.isFinite(total)) {
        throw new Error('Duración fuera de rango.')
    }

    return total
}

export function durationLabel(value: string): string {
    try {
        const seconds = durationSeconds(value)

        if (seconds === 0) return 'Sin retraso'

        const hours = Math.floor(seconds / SECONDS_PER_HOUR)
        const minutes = Math.floor((seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE)
        const rest = Number((seconds % SECONDS_PER_MINUTE).toFixed(9))

        return [
            hours ? `${hours} h` : '',
            minutes ? `${minutes} min` : '',
            rest ? `${rest} s` : '',
        ].filter(Boolean).join(' ')
    } catch {
        return value
    }
}

/**
 * Elimina el sufijo de región `[Europe/Madrid]` de un ZonedDateTime de Java.
 * Se evita una expresión regular con retroceso para que el coste sea lineal.
 */
export function stripZone(value: string): string {
    const close = value.length - 1

    if (value[close] !== ']') return value

    const open = value.indexOf('[', value.lastIndexOf(']', close - 1) + 1)

    return open !== -1 && open < close - 1 ? value.slice(0, open) : value
}

export function localInput(value: string): string {
    const date = new Date(stripZone(value))

    if (Number.isNaN(date.getTime())) return ''

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
    return new Date(now.getTime() + DEFAULT_EXPIRATION_MS).toISOString()
}

export function clock(value: string): string {
    return /T(\d{2}:\d{2}:\d{2})/.exec(value)?.[1] ?? '00:00:00'
}

export function normalizeClock(value: string): string {
    const result = value.length === 5 ? `${value}:00` : value
    const invalid = 'Introduce una hora válida.'

    if (!/^\d{2}:\d{2}:\d{2}$/.test(result)) {
        throw new Error(invalid)
    }

    const [hour, minute, second] = result.split(':').map(Number)

    if (hour > 23 || minute > 59 || second > 59) {
        throw new Error(invalid)
    }

    return result
}

/** Desfase local actual con el formato `+01:00`. */
export function localOffset(now = new Date()): string {
    const offset = -now.getTimezoneOffset()
    const absolute = Math.abs(offset)

    return (
        `${offset >= 0 ? '+' : '-'}` +
        `${pad(Math.floor(absolute / MINUTES_PER_HOUR))}:${pad(absolute % MINUTES_PER_HOUR)}`
    )
}
