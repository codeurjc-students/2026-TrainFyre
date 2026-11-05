import type { causeLabels, dayLabels, severityLabels } from './labels'

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
