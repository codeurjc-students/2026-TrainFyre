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

    it('rechaza si el lector no devuelve texto', async () => {
        class BinaryReader {
            result = new ArrayBuffer(1)
            error = null
            onload: (() => void) | null = null
            onerror: (() => void) | null = null

            readAsDataURL() {
                this.onload?.()
            }
        }

        vi.stubGlobal('FileReader', BinaryReader)

        await expect(blobToDataUrl(new Blob(['x']))).rejects.toThrow('No se pudo leer la imagen.')
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
