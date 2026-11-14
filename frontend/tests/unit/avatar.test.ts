import { describe, expect, it } from 'vitest'

import {
    AVATAR_MAX_BYTES,
    avatarFromForm,
    detectAvatarType,
    isAvatarType,
    readAvatarBlob,
    validateAvatar,
} from '../../src/model'

function image(type = 'image/png', size = 10) {
    return new File([new Uint8Array(size)], 'avatar', { type })
}

describe('Tipos de avatar', () => {
    it.each([
        ['image/png', true],
        ['image/jpeg', true],
        ['image/webp', true],
        ['IMAGE/PNG; charset=binary', true],
        ['image/gif', false],
        ['image/svg+xml', false],
        ['text/html', false],
        ['', false],
    ])('isAvatarType(%j) → %s', (type, expected) => {
        expect(isAvatarType(type)).toBe(expected)
    })
})

describe('Validación del archivo a subir', () => {
    it('acepta una imagen admitida dentro del tamaño máximo', () => {
        const file = image('image/webp', AVATAR_MAX_BYTES)

        expect(validateAvatar(file)).toBe(file)
    })

    it('rechaza archivos vacíos, de otro tipo o demasiado grandes', () => {
        expect(() => validateAvatar(image('image/png', 0))).toThrow('Selecciona una imagen.')
        expect(() => validateAvatar(image('image/gif'))).toThrow('PNG, JPEG o WebP')
        expect(() => validateAvatar(image('image/png', AVATAR_MAX_BYTES + 1))).toThrow('2 MB')
    })

    it('lee el archivo del formulario', () => {
        const file = image()
        const data = new FormData()

        data.set('file', file)

        expect(avatarFromForm(data)).toBeInstanceOf(File)
    })

    it('rechaza formularios sin archivo', () => {
        const data = new FormData()

        expect(() => avatarFromForm(data)).toThrow('Selecciona una imagen.')

        data.set('file', 'texto')

        expect(() => avatarFromForm(data)).toThrow('Selecciona una imagen.')
    })
})

const PNG_BYTES = [0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0]
const JPEG_BYTES = [0xFF, 0xD8, 0xFF, 0xE0, 0, 0, 0, 0, 0, 0, 0, 0]
const WEBP_BYTES = [0x52, 0x49, 0x46, 0x46, 1, 2, 3, 4, 0x57, 0x45, 0x42, 0x50]

describe('Detección del formato de imagen', () => {
    it.each([
        ['PNG', PNG_BYTES, 'image/png'],
        ['JPEG', JPEG_BYTES, 'image/jpeg'],
        ['WebP', WEBP_BYTES, 'image/webp'],
        ['RIFF que no es WebP', [0x52, 0x49, 0x46, 0x46, 1, 2, 3, 4, 0x41, 0x56, 0x49, 0x20], undefined],
        ['texto', [...new TextEncoder().encode('<svg xmlns="">')], undefined],
        ['vacío', [], undefined],
    ])('%s', (_name, bytes, expected) => {
        expect(detectAvatarType(new Uint8Array(bytes))).toBe(expected)
    })
})

describe('Lectura de la imagen recibida', () => {
    it('trata una respuesta vacía como "sin avatar"', async () => {
        await expect(readAvatarBlob(new Blob([]))).resolves.toBeNull()
    })

    it('conserva el Blob si el tipo ya es correcto', async () => {
        const png = new Blob([new Uint8Array(PNG_BYTES)], { type: 'image/png' })

        await expect(readAvatarBlob(png)).resolves.toBe(png)
    })

    it.each(['', 'application/octet-stream', 'application/json'])(
        'corrige el tipo si el backend envía %j',
        async type => {
            const result = await readAvatarBlob(new Blob([new Uint8Array(JPEG_BYTES)], { type }))

            expect(result?.type).toBe('image/jpeg')
            expect(result?.size).toBe(JPEG_BYTES.length)
        },
    )

    it('rechaza contenidos que no son una imagen admitida aunque declaren serlo', async () => {
        const fake = new Blob(['<svg/>'], { type: 'image/png' })

        await expect(readAvatarBlob(fake)).rejects.toThrow('imagen de perfil no válida')
    })
})
