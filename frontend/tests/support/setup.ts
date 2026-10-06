import '@testing-library/jest-dom/vitest'
import { Temporal } from '@js-temporal/polyfill'

// Incidences.tsx uses the global `Temporal` API, which Node (and jsdom) do not
// provide yet. Polyfill it for the tests only if the runtime has no native one.
if (!('Temporal' in globalThis)) {
  Object.defineProperty(globalThis, 'Temporal', {
    value: Temporal,
    configurable: true,
    writable: true,
  })
}
