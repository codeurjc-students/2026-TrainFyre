import type { Incidence } from '@/src/types/incidences'

let counter = 0

/** Builds a valid Incidence; any field can be overridden. */
export function makeIncidence(overrides: Partial<Incidence> = {}): Incidence {
  counter += 1
  const base: Incidence = {
    id: `00000000-0000-0000-0000-${String(counter).padStart(12, '0')}`,
    affectedNetwork: { mapId: 1, lineIds: [101] },
    occurrence: { timestamp: '2026-09-03T05:10:00', duration: 'PT20M', instantaneous: false },
    description: { name: `Incidence ${counter}`, summary: 'Summary' },
    classification: { severity: 'HIGH', cause: 'TECHNICAL_PROBLEM' },
    incidenceDetails: {
      affectedNetwork: { mapId: 1, lineIds: [101] },
      occurrence: { timestamp: '2026-09-03T05:10:00', duration: 'PT20M', instantaneous: false },
      description: { name: `Incidence ${counter}`, summary: 'Summary' },
      classification: { severity: 'HIGH', cause: 'TECHNICAL_PROBLEM' },
    },
  }
  return { ...base, ...overrides }
}

/** Builds the paged envelope returned by GET /incidence. */
export function makePage(content: Incidence[], totalElements: number, page = 0, size = 100) {
  return { content, page, size, totalElements }
}
