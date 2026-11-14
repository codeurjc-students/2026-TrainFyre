// @vitest-environment jsdom
import { afterEach, describe, expect, it, vi } from 'vitest'

import { blobToDataUrl } from '../../src/lib/browser'

afterEach(() => {
    vi.unstubAllGlobals()
})

describe('blobToDataUrl', () => {
    it('convierte un Blob en data URI conservando el tipo', async () => {
        const blob = new Blob(['hola'], { type: 'image/png' })

        await expect(blobToDataUrl(blob)).resolves.toBe('data:image/png;base64,aG9sYQ==')
    })

    it.each([
        ['el error original del lector', new DOMException('lectura fallida'), 'lectura fallida'],
        ['un error genérico si no hay detalle', null, 'No se pudo leer la imagen.'],
    ])('rechaza con %s', async (_name, error, message) => {
        class FailingReader {
            error = error
            onerror: (() => void) | null = null

            readAsDataURL() {
                this.onerror?.()
            }
        }

        vi.stubGlobal('FileReader', FailingReader)

        await expect(blobToDataUrl(new Blob(['x']))).rejects.toThrow(message)
    })
})
